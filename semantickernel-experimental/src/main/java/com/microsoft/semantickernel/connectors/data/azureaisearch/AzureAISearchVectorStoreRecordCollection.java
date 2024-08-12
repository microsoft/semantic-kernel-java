// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.data.azureaisearch;

import com.azure.search.documents.SearchAsyncClient;
import com.azure.search.documents.SearchDocument;
import com.azure.search.documents.indexes.SearchIndexAsyncClient;
import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.indexes.models.VectorSearch;
import com.azure.search.documents.indexes.models.VectorSearchAlgorithmConfiguration;
import com.azure.search.documents.indexes.models.VectorSearchProfile;
import com.azure.search.documents.models.IndexDocumentsResult;
import com.azure.search.documents.models.IndexingResult;
import com.microsoft.semantickernel.data.VectorStoreRecordCollection;
import com.microsoft.semantickernel.data.VectorStoreRecordMapper;
import com.microsoft.semantickernel.data.recorddefinition.VectorStoreRecordDataField;
import com.microsoft.semantickernel.data.recorddefinition.VectorStoreRecordDefinition;
import com.microsoft.semantickernel.data.recorddefinition.VectorStoreRecordField;
import com.microsoft.semantickernel.data.recorddefinition.VectorStoreRecordKeyField;
import com.microsoft.semantickernel.data.recorddefinition.VectorStoreRecordVectorField;
import com.microsoft.semantickernel.data.recordoptions.DeleteRecordOptions;
import com.microsoft.semantickernel.data.recordoptions.GetRecordOptions;
import com.microsoft.semantickernel.data.recordoptions.UpsertRecordOptions;
import com.microsoft.semantickernel.exceptions.SKException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class AzureAISearchVectorStoreRecordCollection<Record> implements
    VectorStoreRecordCollection<String, Record> {

    private static final HashSet<Class<?>> supportedKeyTypes = new HashSet<>(
        Collections.singletonList(
            String.class));

    private static final HashSet<Class<?>> supportedDataTypes = new HashSet<>(
        Arrays.asList(
            String.class,
            Integer.class,
            int.class,
            Long.class,
            long.class,
            Float.class,
            float.class,
            Double.class,
            double.class,
            Boolean.class,
            boolean.class,
            OffsetDateTime.class));

    private static final HashSet<Class<?>> supportedVectorTypes = new HashSet<>(
        Arrays.asList(
            List.class,
            Collection.class));

    private final SearchIndexAsyncClient client;
    private final String collectionName;
    private final Map<String, SearchAsyncClient> clientsByIndex = new ConcurrentHashMap<>();
    private final AzureAISearchVectorStoreRecordCollectionOptions<Record> options;
    private final VectorStoreRecordDefinition recordDefinition;

    // List of non-vector fields. Used to fetch only non-vector fields when vectors are not requested
    private final List<String> nonVectorFields = new ArrayList<>();

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public AzureAISearchVectorStoreRecordCollection(
        @Nonnull SearchIndexAsyncClient client,
        @Nonnull String collectionName,
        @Nonnull AzureAISearchVectorStoreRecordCollectionOptions<Record> options) {
        this.client = client;
        this.collectionName = collectionName;
        this.options = options;

        // If record definition is not provided, create one from the record class
        this.recordDefinition = options.getRecordDefinition() == null
            ? VectorStoreRecordDefinition.fromRecordClass(options.getRecordClass())
            : options.getRecordDefinition();

        // Validate supported types
        VectorStoreRecordDefinition.validateSupportedTypes(
            Collections
                .singletonList(recordDefinition.getKeyDeclaredField(this.options.getRecordClass())),
            supportedKeyTypes);
        VectorStoreRecordDefinition.validateSupportedTypes(
            recordDefinition.getDataDeclaredFields(this.options.getRecordClass()),
            supportedDataTypes);
        VectorStoreRecordDefinition.validateSupportedTypes(
            recordDefinition.getVectorDeclaredFields(this.options.getRecordClass()),
            supportedVectorTypes);

        // Add non-vector fields to the list
        nonVectorFields.add(this.recordDefinition.getKeyField().getName());
        nonVectorFields.addAll(this.recordDefinition.getDataFields().stream()
            .map(VectorStoreRecordDataField::getName)
            .collect(Collectors.toList()));
    }

    @Override
    public String getCollectionName() {
        return collectionName;
    }

    private Mono<List<String>> getIndexesAsync() {
        return client.listIndexes().map(SearchIndex::getName).collect(Collectors.toList());
    }

    @Override
    public Mono<Boolean> collectionExistsAsync() {
        return getIndexesAsync()
            .map(list -> list.stream().anyMatch(name -> name.equalsIgnoreCase(collectionName)));
    }

    @Override
    public Mono<VectorStoreRecordCollection<String, Record>> createCollectionAsync() {
        List<SearchField> searchFields = new ArrayList<>();
        List<VectorSearchAlgorithmConfiguration> algorithms = new ArrayList<>();
        List<VectorSearchProfile> profiles = new ArrayList<>();

        for (VectorStoreRecordField field : this.recordDefinition.getAllFields()) {
            if (field instanceof VectorStoreRecordKeyField) {
                searchFields.add(AzureAISearchVectorStoreCollectionCreateMapping
                    .mapKeyField((VectorStoreRecordKeyField) field));
            } else if (field instanceof VectorStoreRecordDataField) {
                searchFields.add(AzureAISearchVectorStoreCollectionCreateMapping
                    .mapDataField((VectorStoreRecordDataField) field));
            } else {
                searchFields.add(AzureAISearchVectorStoreCollectionCreateMapping
                    .mapVectorField((VectorStoreRecordVectorField) field));
                AzureAISearchVectorStoreCollectionCreateMapping
                    .updateVectorSearchParameters(algorithms, profiles,
                        (VectorStoreRecordVectorField) field);
            }
        }

        SearchIndex newIndex = new SearchIndex(collectionName)
            .setFields(searchFields)
            .setVectorSearch(new VectorSearch()
                .setAlgorithms(algorithms)
                .setProfiles(profiles));

        return client.createIndex(newIndex).then(Mono.just(this));
    }

    @Override
    public Mono<VectorStoreRecordCollection<String, Record>> createCollectionIfNotExistsAsync() {
        return collectionExistsAsync().flatMap(
            exists -> {
                if (!exists) {
                    return createCollectionAsync();
                }
                return Mono.empty();
            })
            .then(Mono.just(this));
    }

    @Override
    public Mono<Void> deleteCollectionAsync() {
        return client.deleteIndex(this.collectionName).then();
    }

    @Override
    public Mono<Record> getAsync(
        @Nonnull String key, GetRecordOptions options) {
        SearchAsyncClient client = this.getSearchClient(this.collectionName);

        // If vectors are not requested, only fetch non-vector fields
        List<String> selectedFields = null;
        if (options != null && !options.includeVectors()) {
            selectedFields = Collections.unmodifiableList(nonVectorFields);
        }

        VectorStoreRecordMapper<Record, SearchDocument> mapper = this.options
            .getVectorStoreRecordMapper();

        // Use custom mapper if available
        if (mapper != null && mapper.getStorageModelToRecordMapper() != null) {
            return client.getDocument(key, SearchDocument.class)
                .map(this.options.getVectorStoreRecordMapper()::mapStorageModeltoRecord);
        }

        return client.getDocumentWithResponse(key, this.options.getRecordClass(), selectedFields)
            .flatMap(response -> {
                if (response.getStatusCode() == 404) {
                    return Mono.error(new SKException("Record not found: " + key));
                }
                return Mono.just(response.getValue());
            });

    }

    @Override
    public Mono<List<Record>> getBatchAsync(
        @Nonnull List<String> keys,
        GetRecordOptions options) {
        return Flux.fromIterable(keys)
            .flatMap(key -> getAsync(key, options).flux())
            .collect(Collectors.toList());
    }

    @Override
    public Mono<String> upsertAsync(@Nonnull Record record, UpsertRecordOptions options) {
        return upsertBatchAsync(Collections.singletonList(record), options)
            .map(Collection::iterator)
            .map(Iterator::next);
    }

    @Override
    public Mono<List<String>> upsertBatchAsync(
        @Nonnull List<Record> records, UpsertRecordOptions options) {
        if (records.isEmpty()) {
            return Mono.just(Collections.emptyList());
        }
        SearchAsyncClient client = this.getSearchClient(this.collectionName);

        VectorStoreRecordMapper<Record, SearchDocument> mapper = this.options
            .getVectorStoreRecordMapper();
        Iterable<?> documents;

        // Use custom mapper if available
        if (mapper != null && mapper.getRecordToStorageModelMapper() != null) {
            documents = records.stream()
                .map(this.options.getVectorStoreRecordMapper()::mapRecordToStorageModel)
                .collect(Collectors.toList());
        } else {
            documents = records;
        }

        return client.uploadDocuments(documents)
            .map(IndexDocumentsResult::getResults)
            .map(
                results -> results.stream()
                    .map(IndexingResult::getKey)
                    .collect(Collectors.toList()));
    }

    @Override
    public Mono<Void> deleteAsync(String key, DeleteRecordOptions options) {
        return deleteBatchAsync(Collections.singletonList(key), options);
    }

    @Override
    public Mono<Void> deleteBatchAsync(List<String> keys, DeleteRecordOptions options) {
        SearchAsyncClient client = this.getSearchClient(this.collectionName);

        return client.deleteDocuments(keys.stream().map(key -> {
            SearchDocument document = new SearchDocument();
            document.put(this.recordDefinition.getKeyField().getName(), key);
            return document;
        }).collect(Collectors.toList())).then();
    }

    /**
     * Get a search client for the index specified. Note: the index might not exist, but we avoid
     * checking everytime and the extra latency.
     *
     * @param indexName Index name
     * @return Search client ready to read/write
     */
    protected SearchAsyncClient getSearchClient(@Nonnull String indexName) {
        return clientsByIndex.computeIfAbsent(
            indexName, client::getSearchAsyncClient);
    }
}
