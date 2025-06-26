// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.samples.documentationexamples.data.vectorstores.oracle;

import com.microsoft.semantickernel.data.jdbc.JDBCVectorStore;
import com.microsoft.semantickernel.data.jdbc.JDBCVectorStoreOptions;
import com.microsoft.semantickernel.data.jdbc.JDBCVectorStoreRecordCollection;
import com.microsoft.semantickernel.data.jdbc.JDBCVectorStoreRecordCollectionOptions;
import com.microsoft.semantickernel.data.jdbc.postgres.PostgreSQLVectorStoreQueryProvider;
import com.microsoft.semantickernel.samples.documentationexamples.data.index.Hotel;

public class Main {
    public static void main(String[] args) {
        // Configure the data source
        OracleDataSource dataSource = new OracleDataSource();
        dataSource.setURL("jdbc:oracle:thin:@localhost:1521/FREEPDB1");
        dataSource.setUser("scott");
        dataSource.setPassword("tiger");

        // Build a query provider
        OracleVectorStoreQueryProvider queryProvider = OracleVectorStoreQueryProvider.builder()
            .withDataSource(dataSource)
            .build();

        // Build a vector store
        JDBCVectorStore vectorStore = JDBCVectorStore.builder()
            .withDataSource(dataSource)
            .withOptions(JDBCVectorStoreOptions.builder()
                .withQueryProvider(queryProvider)
                .build())
            .build();

        VectorStoreRecordCollection<String, Hotel> collection = vectorStore.getCollection(
            "skhotels",
            JDBCVectorStoreRecordCollectionOptions.<Hotel>builder()
                .withRecordClass(Hotel.class)
                .build());

        // Create the collection if it doesn't exist yet.
        collection.createCollectionIfNotExistsAsync().block();

        collection.upsertBatchAsync(getHotels(), null).block();

        // Retrieve the upserted record.
        var retrievedHotel = collection.getAsync("1", null).block();

        // Generate a vector for your search text, using your chosen embedding generation implementation.
        // Just showing a placeholder method here for brevity.
        // var searchVector = generateEmbeddingsAsync(
        // "I'm looking for a hotel where customer happiness is the priority.").block();

        // Do the search.
        // var searchResult = collection.searchAsync(searchVector, VectorSearchOptions.builder()
        // .withTop(1).build()).block();

        // Hotel record = searchResult.getResults().get(0).getRecord();
        // System.out.printf("Found hotel description: %s\n", record.getDescription());

    }

    private static List<Hotel> getHotels() {
        return Arrays.asList(
            new Hotel("id_1", "Hotel 1", 1, "Hotel 1 description",
                Arrays.asList(0.5f, 3.2f, 7.1f, -4.0f, 2.8f, 10.0f, -1.3f, 5.5f), null, null, null,
                4.0),
            new Hotel("id_2", "Hotel 2", 2, "Hotel 2 description",
                Arrays.asList(-2.0f, 8.1f, 0.9f, 5.4f, -3.3f, 2.2f, 9.9f, -4.5f), null, null, null,
                4.0),
            new Hotel("id_3", "Hotel 3", 3, "Hotel 3 description",
                Arrays.asList(4.5f, -6.2f, 3.1f, 7.7f, -0.8f, 1.1f, -2.2f, 8.3f), null, null, null,
                5.0),
            new Hotel("id_4", "Hotel 4", 4, "Hotel 4 description",
                Arrays.asList(7.0f, 1.2f, -5.3f, 2.5f, 6.6f, -7.8f, 3.9f, -0.1f), null, null, null,
                4.0),
            new Hotel("id_5", "Hotel 5", 5, "Hotel 5 description",
                Arrays.asList(-3.5f, 4.4f, -1.2f, 9.9f, 5.7f, -6.1f, 7.8f, -2.0f), null, null, null,
                4.0));
    }
}