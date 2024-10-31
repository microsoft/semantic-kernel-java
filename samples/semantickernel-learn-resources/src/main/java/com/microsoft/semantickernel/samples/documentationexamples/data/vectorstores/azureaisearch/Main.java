// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.samples.documentationexamples.data.vectorstores.azureaisearch;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.microsoft.semantickernel.data.azureaisearch.AzureAISearchVectorStore;
import com.microsoft.semantickernel.data.azureaisearch.AzureAISearchVectorStoreOptions;
import com.microsoft.semantickernel.data.azureaisearch.AzureAISearchVectorStoreRecordCollection;
import com.microsoft.semantickernel.data.azureaisearch.AzureAISearchVectorStoreRecordCollectionOptions;
import com.microsoft.semantickernel.samples.documentationexamples.data.index.Hotel;

public class Main {
    public static void main(String[] args) {
        // Build the Azure AI Search client
        var searchClient = new SearchIndexClientBuilder()
            .endpoint("https://<your-search-service-name>.search.windows.net")
            .credential(new AzureKeyCredential("<your-search-service-key>"))
            .buildAsyncClient();

        // Build an Azure AI Search Vector Store
        var vectorStore = AzureAISearchVectorStore.builder()
            .withSearchIndexAsyncClient(searchClient)
            .withOptions(new AzureAISearchVectorStoreOptions())
            .build();

        var collection = new AzureAISearchVectorStoreRecordCollection<>(searchClient, "skhotels",
            AzureAISearchVectorStoreRecordCollectionOptions.<Hotel>builder()
                .withRecordClass(Hotel.class)
                .build());

    }
}
