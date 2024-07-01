// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.memory.azureaisearch;

import com.azure.search.documents.SearchAsyncClient;
import com.azure.search.documents.SearchDocument;
import com.azure.search.documents.indexes.SearchIndexAsyncClient;
import com.azure.search.documents.models.IndexDocumentsResult;
import com.azure.search.documents.models.IndexingResult;
import com.microsoft.semantickernel.exceptions.SKException;
import com.microsoft.semantickernel.memory.VectorRecordStore;
import com.microsoft.semantickernel.memory.VectorStoreRecordMapper;
import com.microsoft.semantickernel.memory.recorddefinition.VectorStoreRecordDataField;
import com.microsoft.semantickernel.memory.recorddefinition.VectorStoreRecordDefinition;
import com.microsoft.semantickernel.memory.recordoptions.DeleteRecordOptions;
import com.microsoft.semantickernel.memory.recordoptions.GetRecordOptions;
import com.microsoft.semantickernel.memory.recordoptions.UpsertRecordOptions;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class AzureAISearchVectorRecordStore<Record> implements VectorRecordStore<String, Record> {
    private final SearchIndexAsyncClient client;
    private final Map<String, SearchAsyncClient> clientsByIndex = new ConcurrentHashMap<>();
    private final AzureAISearchVectorStoreOptions<Record> options;
    private final List<String> nonVectorFields = new ArrayList<>();

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public AzureAISearchVectorRecordStore(
        @Nonnull SearchIndexAsyncClient client,
        @Nonnull AzureAISearchVectorStoreOptions<Record> options) {
        this.client = client;

        // If record definition is not provided, create one from the record class
        if (options.getRecordDefinition() == null) {
            this.options = AzureAISearchVectorStoreOptions.<Record>builder()
                .withRecordClass(options.getRecordClass())
                .withVectorStoreRecordMapper(options.getVectorStoreRecordMapper())
                .withRecordDefinition(VectorStoreRecordDefinition.create(options.getRecordClass()))
                .build();
        } else {
            this.options = options;
        }

        // Add non-vector fields to the list
        nonVectorFields.add(this.options.getRecordDefinition().getKeyField().getName());
        nonVectorFields.addAll(this.options.getRecordDefinition().getDataFields().stream()
            .map(VectorStoreRecordDataField::getName)
            .collect(Collectors.toList()));
    }

    private String resolveCollectionName(@Nullable String collectionName) {
        if (collectionName != null) {
            return collectionName;
        }
        if (options.getDefaultCollectionName() != null) {
            return options.getDefaultCollectionName();
        }
        throw new SKException("A collection name is required");
    }

    @Override
    public Mono<Record> getAsync(
        @Nonnull String key, GetRecordOptions options) {
        String indexName = resolveCollectionName(
            options != null ? options.getCollectionName() : null);
        SearchAsyncClient client = this.getSearchClient(indexName);

        // If vectors are not requested, only fetch non-vector fields
        List<String> selectedFields = null;
        if (options != null && !options.includeVectors()) {
            selectedFields = Collections.unmodifiableList(nonVectorFields);
        }

        VectorStoreRecordMapper<Record, SearchDocument> mapper = this.options
            .getVectorStoreRecordMapper();

        // Use custom mapper if available
        if (mapper != null && mapper.getToRecordMapper() != null) {
            return client.getDocument(key, SearchDocument.class)
                .map(this.options.getVectorStoreRecordMapper()::toRecord);
        }

        return client.getDocumentWithResponse(key, this.options.getRecordClass(), selectedFields)
            .map(response -> {
                if (response.getStatusCode() == 404) {
                    throw new SKException("Record not found: " + key);
                }
                return response.getValue();
            });

    }

    @Override
    public Mono<Collection<Record>> getBatchAsync(
        @Nonnull Collection<String> keys,
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
    public Mono<Collection<String>> upsertBatchAsync(
        @Nonnull Collection<Record> records, UpsertRecordOptions options) {
        if (records.isEmpty()) {
            return Mono.just(Collections.emptyList());
        }

        String indexName = resolveCollectionName(
            options != null ? options.getCollectionName() : null);
        SearchAsyncClient client = this.getSearchClient(indexName);

        VectorStoreRecordMapper<Record, SearchDocument> mapper = this.options
            .getVectorStoreRecordMapper();
        Iterable<?> documents;

        // Use custom mapper if available
        if (mapper != null && mapper.getToStorageModelMapper() != null) {
            documents = records.stream()
                .map(this.options.getVectorStoreRecordMapper()::toStorageModel)
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
    public Mono<Void> deleteBatchAsync(Collection<String> keys, DeleteRecordOptions options) {
        String indexName = resolveCollectionName(
            options != null ? options.getCollectionName() : null);
        SearchAsyncClient client = this.getSearchClient(indexName);

        return client.deleteDocuments(keys.stream().map(key -> {
            SearchDocument document = new SearchDocument();
            document.put(this.options.getRecordDefinition().getKeyField().getName(), key);
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
