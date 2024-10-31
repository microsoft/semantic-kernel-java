// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.samples.documentationexamples.data.vectorsearch.hotels;

import com.microsoft.semantickernel.data.jdbc.JDBCVectorStore;
import com.microsoft.semantickernel.data.jdbc.JDBCVectorStoreOptions;
import com.microsoft.semantickernel.data.jdbc.JDBCVectorStoreRecordCollectionOptions;
import com.microsoft.semantickernel.data.jdbc.postgres.PostgreSQLVectorStoreQueryProvider;
import com.microsoft.semantickernel.data.vectorstorage.options.VectorSearchOptions;
import com.microsoft.semantickernel.samples.documentationexamples.data.index.Hotel;
import org.postgresql.ds.PGSimpleDataSource;
import reactor.core.publisher.Mono;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        // Configure the data source
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUrl("jdbc:postgresql://localhost:5432/sk");
        dataSource.setUser("postgres");
        dataSource.setPassword("root");

        // Create a JDBC vector store and choose an existing collection that already contains records.
        var vectorStore = new JDBCVectorStore(dataSource, JDBCVectorStoreOptions.builder()
            .withQueryProvider(PostgreSQLVectorStoreQueryProvider.builder()
                .withDataSource(dataSource)
                .build())
            .build());
        var collection = vectorStore.getCollection("skhotels",
            JDBCVectorStoreRecordCollectionOptions.<Hotel>builder()
                .withRecordClass(Hotel.class)
                .build());

        // Generate a vector for your search text, using your chosen embedding generation implementation.
        // Just showing a placeholder method here for brevity.
        var searchVector = generateEmbeddingsAsync(
            "I'm looking for a hotel where customer happiness is the priority.").block();

        // Do the search, passing an options object with a Top value to limit results to the single top match.
        var searchResult = collection.searchAsync(searchVector, VectorSearchOptions.builder()
            .withTop(1).build()).block();

        // Inspect the returned hotel.
        Hotel hotel = searchResult.getResults().get(0).getRecord();
        System.out.printf("Found hotel description: %s\n", hotel.getDescription());
    }

    private static Mono<List<Float>> generateEmbeddingsAsync(String text) {
        return Mono.just(List.of(1.0f, 2.0f, 3.0f, 4.0f));
    }
}
