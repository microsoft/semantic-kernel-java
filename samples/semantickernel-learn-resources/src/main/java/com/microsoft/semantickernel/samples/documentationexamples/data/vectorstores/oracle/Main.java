// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.samples.documentationexamples.data.vectorstores.oracle;

import com.microsoft.semantickernel.data.jdbc.JDBCVectorStore;
import com.microsoft.semantickernel.data.jdbc.JDBCVectorStoreOptions;
import com.microsoft.semantickernel.data.jdbc.JDBCVectorStoreRecordCollectionOptions;
import com.microsoft.semantickernel.data.jdbc.oracle.OracleVectorStoreQueryProvider;
import com.microsoft.semantickernel.data.vectorstorage.VectorStoreRecordCollection;
import com.microsoft.semantickernel.samples.documentationexamples.data.index.Hotel;
import oracle.jdbc.datasource.impl.OracleDataSource;
import java.sql.SQLException;
import java.util.Collections;

public class Main {
    public static void main(String[] args) throws SQLException {

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

        // Get a collection from the vector store
        VectorStoreRecordCollection<String, Hotel> collection =
            vectorStore.getCollection("skhotels",
                JDBCVectorStoreRecordCollectionOptions.<Hotel>builder()
                .withRecordClass(Hotel.class)
                .build());

        // Create the collection if it doesn't exist yet.
        // TODO Override implementation to map TEXT to VARCHAR
        // Craeted manually for the moment
        //collection.createCollectionAsync().block();

        // Upsert a record.
        collection.upsertAsync(new Hotel("1",
                    "HotelOne",
                    "My Description for HotelOne",
                    Collections.emptyList(), Collections.emptyList()),
                null)
            .block();

        // Retrieve the upserted record.
//        var retrievedHotel = collection.getAsync("1", null).block();

        // Generate a vector for your search text, using your chosen embedding generation implementation.
        // Just showing a placeholder method here for brevity.
//        var searchVector = generateEmbeddingsAsync(
//            "I'm looking for a hotel where customer happiness is the priority.").block();

        // Do the search.
//        var searchResult = collection.searchAsync(searchVector, VectorSearchOptions.builder()
//            .withTop(1).build()).block();

//        Hotel record = searchResult.getResults().get(0).getRecord();
//        System.out.printf("Found hotel description: %s\n", record.getDescription());

    }
}
