// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.memory.azureaisearch;

import com.azure.search.documents.indexes.SearchIndexAsyncClient;

/**
 * Factory for creating Azure AI Search vector store record collections.
 *
 * @param <Record> the record type
 */
public interface AzureAISearchVectorStoreRecordCollectionFactory<Record> {

    /**
     * Creates a new Azure AI Search vector store record collection.
     *
     * @param client The Azure AI Search client.
     * @param collectionName The name of the collection.
     * @param options The options for the collection.
     * @return The new Azure AI Search vector store record collection.
     */
    AzureAISearchVectorStoreRecordCollection<Record> createVectorStoreRecordCollection(
        SearchIndexAsyncClient client,
        String collectionName,
        AzureAISearchVectorStoreRecordCollectionOptions<Record> options);
}
