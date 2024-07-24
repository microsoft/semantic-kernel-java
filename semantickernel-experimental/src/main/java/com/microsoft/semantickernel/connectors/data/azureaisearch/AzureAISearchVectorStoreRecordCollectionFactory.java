// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.data.azureaisearch;

import com.azure.search.documents.indexes.SearchIndexAsyncClient;

/**
 * Factory for creating Azure AI Search vector store record collections.
 *
 */
public interface AzureAISearchVectorStoreRecordCollectionFactory {

    /**
     * Creates a new Azure AI Search vector store record collection.
     *
     * @param client The Azure AI Search client.
     * @param collectionName The name of the collection.
     * @param options The options for the collection.
     * @return The new Azure AI Search vector store record collection.
     */
    <Record> AzureAISearchVectorStoreRecordCollection<Record> createVectorStoreRecordCollection(
        SearchIndexAsyncClient client,
        String collectionName,
        AzureAISearchVectorStoreRecordCollectionOptions<Record> options);
}
