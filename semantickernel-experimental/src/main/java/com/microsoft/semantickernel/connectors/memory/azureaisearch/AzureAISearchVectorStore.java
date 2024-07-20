// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.memory.azureaisearch;

import com.azure.search.documents.indexes.SearchIndexAsyncClient;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.microsoft.semantickernel.data.VectorStore;
import com.microsoft.semantickernel.data.VectorStoreRecordCollection;
import com.microsoft.semantickernel.data.recorddefinition.VectorStoreRecordDefinition;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.List;

public class AzureAISearchVectorStore<Record>
    implements VectorStore<String, Record, AzureAISearchVectorStoreRecordCollection<Record>> {

    private final SearchIndexAsyncClient client;
    private final AzureAISearchVectorStoreOptions<Record> options;

    /**
     * Creates a new instance of {@link AzureAISearchVectorStore}.
     *
     * @param client The Azure AI Search client.
     * @param options The options for the vector store.
     */
    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public AzureAISearchVectorStore(@Nonnull SearchIndexAsyncClient client,
        @Nonnull AzureAISearchVectorStoreOptions<Record> options) {
        this.client = client;
        this.options = options;
    }

    /**
     * Gets a new instance of {@link AzureAISearchVectorStoreRecordCollection}
     *
     * @param collectionName The name of the collection.
     * @param recordDefinition The record definition.
     * @return The collection.
     */
    @Override
    public AzureAISearchVectorStoreRecordCollection<Record> getCollection(
        @Nonnull String collectionName,
        VectorStoreRecordDefinition recordDefinition) {

        if (options.getVectorStoreRecordCollectionFactory() != null) {
            return options.getVectorStoreRecordCollectionFactory()
                .createVectorStoreRecordCollection(
                    client,
                    collectionName,
                    AzureAISearchVectorStoreRecordCollectionOptions.<Record>builder()
                        .withRecordClass(options.getRecordClass())
                        .withRecordDefinition(recordDefinition)
                        .build());
        }

        return new AzureAISearchVectorStoreRecordCollection<>(client, collectionName,
            AzureAISearchVectorStoreRecordCollectionOptions.<Record>builder()
                .withRecordClass(options.getRecordClass())
                .withRecordDefinition(recordDefinition)
                .build());
    }

    /**
     * Gets the names of all collections in the Azure AI Search vector store.
     *
     * @return A list of collection names.
     */
    @Override
    public Mono<List<String>> getCollectionNamesAsync() {
        return client.listIndexes().map(SearchIndex::getName).collectList();
    }
}
