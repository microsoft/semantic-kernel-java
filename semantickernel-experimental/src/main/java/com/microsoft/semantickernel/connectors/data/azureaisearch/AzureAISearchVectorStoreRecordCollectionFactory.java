// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.data.azureaisearch;

import com.azure.search.documents.indexes.SearchIndexAsyncClient;
import com.microsoft.semantickernel.data.record.definition.VectorStoreRecordDefinition;

/**
 * Factory for creating Azure AI Search vector store record collections.
 */
public interface AzureAISearchVectorStoreRecordCollectionFactory {

    /**
     * Creates a new Azure AI Search vector store record collection.
     *
     * @param client         The Azure AI Search client.
     * @param collectionName The name of the collection.
     * @param recordClass    The class type of the record.
     * @param recordDefinition The record definition.
     * @return The new Azure AI Search vector store record collection.
     */
    <Record> AzureAISearchVectorStoreRecordCollection<Record> createVectorStoreRecordCollection(
        SearchIndexAsyncClient client,
        String collectionName,
        Class<Record> recordClass,
        VectorStoreRecordDefinition recordDefinition);
}
