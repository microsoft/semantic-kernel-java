// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.data.azureaisearch;

import com.azure.search.documents.SearchAsyncClient;
import com.azure.search.documents.SearchDocument;
import com.azure.search.documents.indexes.SearchIndexAsyncClient;
import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.indexes.models.VectorSearchAlgorithmConfiguration;
import com.azure.search.documents.indexes.models.VectorSearchProfile;
import com.azure.search.documents.models.IndexDocumentsResult;
import com.azure.search.documents.models.IndexingResult;
import com.azure.search.documents.models.SearchOptions;
import com.azure.search.documents.models.VectorQuery;
import com.azure.search.documents.models.VectorizableTextQuery;
import com.azure.search.documents.models.VectorizedQuery;
import com.microsoft.semantickernel.data.vectorsearch.VectorizableSearch;
import com.microsoft.semantickernel.data.vectorsearch.VectorSearchResult;
import com.microsoft.semantickernel.data.vectorsearch.VectorizedSearch;
import com.microsoft.semantickernel.data.vectorstorage.VectorStoreRecordCollection;
import com.microsoft.semantickernel.data.vectorstorage.VectorStoreRecordMapper;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordDataField;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordDefinition;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordField;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordKeyField;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordVectorField;
import com.microsoft.semantickernel.data.vectorstorage.options.DeleteRecordOptions;
import com.microsoft.semantickernel.data.vectorstorage.options.GetRecordOptions;
import com.microsoft.semantickernel.data.vectorstorage.options.UpsertRecordOptions;
import com.microsoft.semantickernel.data.vectorstorage.options.VectorSearchOptions;
import com.microsoft.semantickernel.data.vectorsearch.queries.VectorSearchQuery;
import com.microsoft.semantickernel.data.vectorsearch.queries.VectorizableTextSearchQuery;
import com.microsoft.semantickernel.data.vectorsearch.queries.VectorizedSearchQuery;
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
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class AzureAISearchVectorStoreRecordCollection<Record> implements
    VectorStoreRecordCollection<String, Record>,
    VectorizedSearch<Record>,
    VectorizableSearch<Record> {

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

    private final SearchIndexAsyncClient searchIndexAsyncClient;
    private final SearchAsyncClient searchAsyncClient;
    private final String collectionName;
    private final AzureAISearchVectorStoreRecordCollectionOptions<Record> options;
    private final VectorStoreRecordDefinition recordDefinition;

    // List of non-vector fields. Used to fetch only non-vector fields when vectors are not requested
    private final List<String> nonVectorFields = new ArrayList<>();
    private final Map<String, String> storageNames;
    private final String firstVectorFieldName;

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public AzureAISearchVectorStoreRecordCollection(
        @Nonnull SearchIndexAsyncClient searchIndexAsyncClient,
        @Nonnull String collectionName,
        @Nonnull AzureAISearchVectorStoreRecordCollectionOptions<Record> options) {
        this.searchIndexAsyncClient = searchIndexAsyncClient;
        this.collectionName = collectionName;
        this.searchAsyncClient = searchIndexAsyncClient.getSearchAsyncClient(collectionName);
        this.options = options;

        // If record definition is not provided, create one from the record class
        this.recordDefinition = options.getRecordDefinition() == null
            ? VectorStoreRecordDefinition.fromRecordClass(options.getRecordClass())
            : options.getRecordDefinition();

        // Validate supported types
        VectorStoreRecordDefinition.validateSupportedTypes(
            Collections.singletonList(recordDefinition.getKeyField()),
            supportedKeyTypes);
        VectorStoreRecordDefinition.validateSupportedTypes(
            new ArrayList<>(recordDefinition.getDataFields()),
            supportedDataTypes);
        VectorStoreRecordDefinition.validateSupportedTypes(
            new ArrayList<>(recordDefinition.getVectorFields()),
            supportedVectorTypes);

        // Add non-vector fields to the list
        nonVectorFields.add(this.recordDefinition.getKeyField().getEffectiveStorageName());
        nonVectorFields.addAll(this.recordDefinition.getDataFields().stream()
            .map(VectorStoreRecordDataField::getEffectiveStorageName)
            .collect(Collectors.toList()));

        storageNames = recordDefinition.getFieldStorageNames();
        firstVectorFieldName = recordDefinition.getVectorFields().isEmpty() ? null
            : recordDefinition.getVectorFields().get(0).getName();
    }

    @Override
    public String getCollectionName() {
        return collectionName;
    }

    private Mono<List<String>> getIndexesAsync() {
        return searchIndexAsyncClient.listIndexes().map(SearchIndex::getName)
            .collect(Collectors.toList());
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
            .setVectorSearch(new com.azure.search.documents.indexes.models.VectorSearch()
                .setAlgorithms(algorithms)
                .setProfiles(profiles));

        return searchIndexAsyncClient.createIndex(newIndex).then(Mono.just(this));
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
        return searchIndexAsyncClient.deleteIndex(this.collectionName).then();
    }

    @Override
    public Mono<Record> getAsync(
        @Nonnull String key, GetRecordOptions options) {
        // If vectors are not requested, only fetch non-vector fields
        List<String> selectedFields = null;
        if (options == null || !options.isIncludeVectors()) {
            selectedFields = Collections.unmodifiableList(nonVectorFields);
        }

        VectorStoreRecordMapper<Record, SearchDocument> mapper = this.options
            .getVectorStoreRecordMapper();

        // Use custom mapper if available
        if (mapper != null && mapper.getStorageModelToRecordMapper() != null) {
            return searchAsyncClient.getDocument(key, SearchDocument.class)
                .map(record -> mapper.mapStorageModelToRecord(record, options));
        }

        return searchAsyncClient
            .getDocumentWithResponse(key, this.options.getRecordClass(), selectedFields)
            .flatMap(response -> {
                int statusCode = response.getStatusCode();
                if (statusCode >= 200 && statusCode < 300) {
                    return Mono.just(response.getValue());
                }
                if (response.getStatusCode() == 404) {
                    return Mono.error(new SKException("Record not found: " + key));
                }
                return Mono.error(new SKException("Failed to get record: " + key + ". Status code: "
                    + statusCode));
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

        return searchAsyncClient.uploadDocuments(documents)
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
        return searchAsyncClient.deleteDocuments(keys.stream().map(key -> {
            SearchDocument document = new SearchDocument();
            document.put(this.recordDefinition.getKeyField().getEffectiveStorageName(), key);
            return document;
        }).collect(Collectors.toList())).then();
    }

    private Mono<List<VectorSearchResult<Record>>> searchAndMapAsync(SearchOptions searchOptions,
        GetRecordOptions getRecordOptions) {
        VectorStoreRecordMapper<Record, SearchDocument> mapper = this.options
            .getVectorStoreRecordMapper();

        return this.searchAsyncClient.search(null, searchOptions)
            .flatMap(response -> {
                Record record;

                // Use custom mapper if available
                if (mapper != null && mapper.getStorageModelToRecordMapper() != null) {
                    record = mapper
                        .mapStorageModelToRecord(response.getDocument(SearchDocument.class),
                            getRecordOptions);
                } else {
                    record = response.getDocument(this.options.getRecordClass());
                }

                return Mono.just(new VectorSearchResult<>(record, response.getScore()));
            }).collectList();
    }

    /**
     * Search the vector store for records that match the given embedding and filter.
     *
     * @param query The vector search query.
     * @return A list of search results.
     */
    @Override
    public Mono<List<VectorSearchResult<Record>>> searchAsync(VectorSearchQuery query) {
        if (firstVectorFieldName == null) {
            throw new SKException("No vector fields defined. Cannot perform vector search");
        }

        VectorSearchOptions options = query.getSearchOptions();
        if (options == null) {
            options = VectorSearchOptions.createDefault(firstVectorFieldName);
        }

        List<VectorQuery> vectorQueries = new ArrayList<>();

        if (query instanceof VectorizedSearchQuery) {
            vectorQueries.add(new VectorizedQuery(((VectorizedSearchQuery) query).getVector())
                .setFields(
                    storageNames
                        .get(options.getVectorFieldName() != null ? options.getVectorFieldName()
                            : firstVectorFieldName))
                .setKNearestNeighborsCount(options.getLimit()));
        } else if (query instanceof VectorizableTextSearchQuery) {
            vectorQueries
                .add(new VectorizableTextQuery(((VectorizableTextSearchQuery) query).getQueryText())
                    .setFields(
                        storageNames
                            .get(options.getVectorFieldName() != null ? options.getVectorFieldName()
                                : firstVectorFieldName))
                    .setKNearestNeighborsCount(options.getLimit()));
        } else {
            throw new SKException("Unsupported query type: " + query.getQueryType());
        }

        String filter = AzureAISearchVectorStoreCollectionSearchMapping
            .buildFilterString(options.getBasicVectorSearchFilter(), storageNames);

        SearchOptions searchOptions = new SearchOptions()
            .setFilter(filter)
            .setTop(options.getLimit())
            .setSkip(options.getOffset())
            .setScoringParameters()
            .setVectorSearchOptions(new com.azure.search.documents.models.VectorSearchOptions()
                .setQueries(vectorQueries));

        if (!options.isIncludeVectors()) {
            searchOptions.setSelect(nonVectorFields.toArray(new String[0]));
        }

        return searchAndMapAsync(searchOptions, new GetRecordOptions(options.isIncludeVectors()));
    }

    /**
     * Vectorizable text search. This method searches for records that are similar to the given text.
     *
     * @param searchText The text to search with.
     * @param options    The options to use for the search.
     * @return A list of search results.
     */
    @Override
    public Mono<List<VectorSearchResult<Record>>> searchAsync(String searchText,
        VectorSearchOptions options) {
        return searchAsync(VectorSearchQuery.createQuery(searchText, options));
    }

    /**
     * Vectorized search. This method searches for records that are similar to the given vector.
     *
     * @param vector  The vector to search with.
     * @param options The options to use for the search.
     * @return A list of search results.
     */
    @Override
    public Mono<List<VectorSearchResult<Record>>> searchAsync(List<Float> vector,
        VectorSearchOptions options) {
        return searchAsync(VectorSearchQuery.createQuery(vector, options));
    }
}
