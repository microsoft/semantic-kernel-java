// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.samples.documentationexamples.data.vectorstores.oracle;

import com.microsoft.semantickernel.data.jdbc.JDBCVectorStore;
import com.microsoft.semantickernel.data.jdbc.JDBCVectorStoreOptions;
import com.microsoft.semantickernel.data.jdbc.JDBCVectorStoreRecordCollectionOptions;
import com.microsoft.semantickernel.data.jdbc.oracle.OracleVectorStoreQueryProvider;
import com.microsoft.semantickernel.data.vectorstorage.VectorStoreRecordCollection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import oracle.jdbc.datasource.impl.OracleDataSource;

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

        VectorStoreRecordCollection<String, Book> collection = vectorStore.getCollection(
            "books",
            JDBCVectorStoreRecordCollectionOptions.<Book>builder()
                .withRecordClass(Book.class)
                .build());

        // Create the collection if it doesn't exist yet.
        collection.createCollectionIfNotExistsAsync().block();

        collection.upsertBatchAsync(books, null).block();

        // Retrieve the upserted record.
        //var retrievedBook = collection.getAsync("1", null).block();

        // Generate a vector for your search text, using your chosen embedding generation implementation.
        // Just showing a placeholder method here for brevity.
        // var searchVector = generateEmbeddingsAsync(
        // "I'm looking for a Book where customer happiness is the priority.").block();

        // Do the search.
        // var searchResult = collection.searchAsync(searchVector, VectorSearchOptions.builder()
        // .withTop(1).build()).block();

        // Book record = searchResult.getResults().get(0).getRecord();
        // System.out.printf("Found Book description: %s\n", record.getDescription());

    }

    static List<Book> books = Arrays.asList(
        new Book("1", "one", "sking", 0, null, "sum", null));

}