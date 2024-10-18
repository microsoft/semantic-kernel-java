// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.data.azureaisearch;

import com.azure.search.documents.indexes.SearchIndexAsyncClient;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.microsoft.semantickernel.data.VectorStore;
import com.microsoft.semantickernel.data.VectorStoreRecordCollection;
import com.microsoft.semantickernel.data.recorddefinition.VectorStoreRecordDefinition;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import reactor.core.publisher.Mono;

/**
 * Represents an Azure AI Search vector store.
 */
public class AzureAISearchVectorStore implements VectorStore {

    private final SearchIndexAsyncClient client;
    private final AzureAISearchVectorStoreOptions options;

    /**
     * Creates a new instance of {@link AzureAISearchVectorStore}.
     *
     * @param client  The Azure AI Search client.
     * @param options The options for the vector store.
     */
    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public AzureAISearchVectorStore(@Nonnull SearchIndexAsyncClient client,
        @Nonnull AzureAISearchVectorStoreOptions options) {
        this.client = client;
        this.options = options;
    }

    /**
     * Gets a new instance of {@link AzureAISearchVectorStoreRecordCollection}
     *
     * @param collectionName   The name of the collection.
     * @param recordClass      The class type of the record.
     * @param recordDefinition The record definition.
     * @return The collection.
     */
    @Override
    public final <Key, Record> VectorStoreRecordCollection<Key, Record> getCollection(
        @Nonnull String collectionName,
        @Nonnull Class<Key> keyClass,
        @Nonnull Class<Record> recordClass,
        @Nullable VectorStoreRecordDefinition recordDefinition) {
        if (!keyClass.equals(String.class)) {
            throw new IllegalArgumentException("Azure AI Search only supports string keys");
        }

        return (VectorStoreRecordCollection<Key, Record>) getCollection(
            collectionName, recordClass, recordDefinition);
    }

    /**
     * Gets a new instance of {@link AzureAISearchVectorStoreRecordCollection}
     *
     * @param collectionName   The name of the collection.
     * @param recordClass      The class type of the record.
     * @param recordDefinition The record definition.
     * @param <Record>         The type of record in the collection.
     * @return The collection.
     */
    public <Record> AzureAISearchVectorStoreRecordCollection<Record> getCollection(
        @Nonnull String collectionName,
        @Nonnull Class<Record> recordClass,
        @Nullable VectorStoreRecordDefinition recordDefinition) {
        if (options.getVectorStoreRecordCollectionFactory() != null) {
            return options.getVectorStoreRecordCollectionFactory()
                .createVectorStoreRecordCollection(
                    client,
                    collectionName,
                    AzureAISearchVectorStoreRecordCollectionOptions.<Record>builder()
                        .withRecordClass(recordClass)
                        .withRecordDefinition(recordDefinition)
                        .build());
        }

        return new AzureAISearchVectorStoreRecordCollection<>(
            client,
            collectionName,
            AzureAISearchVectorStoreRecordCollectionOptions.<Record>builder()
                .withRecordClass(recordClass)
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

    /**
     * Creates a new {@link Builder} instance.
     *
     * @return The new builder instance.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for {@link AzureAISearchVectorStore}.
     */
    public static class Builder {

        @Nullable
        private SearchIndexAsyncClient client;
        @Nullable
        private AzureAISearchVectorStoreOptions options;

        /**
         * Sets the Azure AI Search client.
         *
         * @param client The Azure AI Search client.
         * @return The updated builder instance.
         */
        @SuppressFBWarnings("EI_EXPOSE_REP2")
        public Builder withClient(@Nonnull SearchIndexAsyncClient client) {
            this.client = client;
            return this;
        }

        /**
         * Sets the options for the Azure AI Search vector store.
         *
         * @param options The options for the Azure AI Search vector store.
         * @return The updated builder instance.
         */
        public Builder withOptions(
            @Nonnull AzureAISearchVectorStoreOptions options) {
            this.options = options;
            return this;
        }

        /**
         * Builds the Azure AI Search vector store.
         *
         * @return The Azure AI Search vector store.
         */
        public AzureAISearchVectorStore build() {
            if (client == null) {
                throw new IllegalStateException("client is required");
            }
            if (options == null) {
                throw new IllegalStateException("options is required");
            }

            return new AzureAISearchVectorStore(client, options);
        }
    }
}
