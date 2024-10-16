package com.microsoft.semantickernel.samples.documentationexamples.vectorstores.connectors.inmemory;

import com.microsoft.semantickernel.data.VolatileVectorStore;
import com.microsoft.semantickernel.data.VolatileVectorStoreRecordCollection;
import com.microsoft.semantickernel.data.VolatileVectorStoreRecordCollectionOptions;
import com.microsoft.semantickernel.samples.documentationexamples.vectorstores.index.Hotel;

public class Main {
    public static void main(String[] args) {
        // Build an Azure AI Search Vector Store
        var vectorStore = new VolatileVectorStore();

        var collection = new VolatileVectorStoreRecordCollection<>("skhotels",
                VolatileVectorStoreRecordCollectionOptions.<Hotel>builder()
                        .withRecordClass(Hotel.class)
                        .build());
    }
}
