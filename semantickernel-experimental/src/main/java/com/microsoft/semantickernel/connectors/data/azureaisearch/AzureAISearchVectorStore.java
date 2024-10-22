// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.data.azureaisearch;

import com.azure.search.documents.indexes.SearchIndexAsyncClient;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.microsoft.semantickernel.data.vectorstorage.VectorStore;
import com.microsoft.semantickernel.data.vectorstorage.VectorStoreRecordCollection;
import com.microsoft.semantickernel.data.vectorstorage.VectorStoreRecordCollectionOptions;
import com.microsoft.semantickernel.exceptions.SKException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import reactor.core.publisher.Mono;

/**
 * Represents an Azure AI Search vector store.
 */
public class AzureAISearchVectorStore implements VectorStore {

    private final SearchIndexAsyncClient searchIndexAsyncClient;
    private final AzureAISearchVectorStoreOptions options;

    /**
     * Creates a new instance of {@link AzureAISearchVectorStore}.
     *
     * @param searchIndexAsyncClient  The Azure AI Search client.
     * @param options The options for the vector store.
     */
    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public AzureAISearchVectorStore(@Nonnull SearchIndexAsyncClient searchIndexAsyncClient,
        @Nonnull AzureAISearchVectorStoreOptions options) {
        this.searchIndexAsyncClient = searchIndexAsyncClient;
        this.options = options;
    }

    /**
     * Gets a new instance of {@link AzureAISearchVectorStoreRecordCollection}
     *
     * @param collectionName   The name of the collection.
     * @param options          The options for the collection.
     * @return The collection.
     */
    @Override
    public final <Key, Record> VectorStoreRecordCollection<Key, Record> getCollection(
        @Nonnull String collectionName,
        @Nonnull VectorStoreRecordCollectionOptions<Key, Record> options) {
        if (!options.getKeyClass().equals(String.class)) {
            throw new SKException("Azure AI Search only supports string keys");
        }
        if (options.getRecordClass() == null) {
            throw new SKException("Record class is required");
        }

        if (this.options.getVectorStoreRecordCollectionFactory() != null) {
            return (VectorStoreRecordCollection<Key, Record>) this.options
                .getVectorStoreRecordCollectionFactory()
                .createVectorStoreRecordCollection(
                    searchIndexAsyncClient,
                    collectionName,
                    options.getRecordClass(),
                    options.getRecordDefinition());
        }

        return (VectorStoreRecordCollection<Key, Record>) new AzureAISearchVectorStoreRecordCollection<>(
            searchIndexAsyncClient,
            collectionName,
            (AzureAISearchVectorStoreRecordCollectionOptions<Record>) options);
    }

    /**
     * Gets the names of all collections in the Azure AI Search vector store.
     *
     * @return A list of collection names.
     */
    @Override
    public Mono<List<String>> getCollectionNamesAsync() {
        return searchIndexAsyncClient.listIndexes().map(SearchIndex::getName).collectList();
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
        private SearchIndexAsyncClient searchIndexAsyncClient;
        @Nullable
        private AzureAISearchVectorStoreOptions options;

        /**
         * Sets the Azure AI Search searchIndexClient.
         *
         * @param searchIndexAsyncClient The Azure AI Search searchIndexClient.
         * @return The updated builder instance.
         */
        @SuppressFBWarnings("EI_EXPOSE_REP2")
        public Builder withSearchIndexAsyncClient(
            @Nonnull SearchIndexAsyncClient searchIndexAsyncClient) {
            this.searchIndexAsyncClient = searchIndexAsyncClient;
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
            if (searchIndexAsyncClient == null) {
                throw new SKException("searchIndexAsyncClient is required");
            }
            if (options == null) {
                throw new SKException("options is required");
            }

            return new AzureAISearchVectorStore(searchIndexAsyncClient, options);
        }
    }
}
