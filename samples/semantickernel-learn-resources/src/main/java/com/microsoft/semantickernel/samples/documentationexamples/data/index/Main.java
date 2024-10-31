// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.samples.documentationexamples.data.index;

import com.microsoft.semantickernel.data.jdbc.JDBCVectorStore;
import com.microsoft.semantickernel.data.jdbc.JDBCVectorStoreOptions;
import com.microsoft.semantickernel.data.jdbc.JDBCVectorStoreRecordCollectionOptions;
import com.microsoft.semantickernel.data.jdbc.postgres.PostgreSQLVectorStoreQueryProvider;
import com.microsoft.semantickernel.data.vectorstorage.options.VectorSearchOptions;
import org.postgresql.ds.PGSimpleDataSource;
import reactor.core.publisher.Mono;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        // Create a PostgreSQL data source
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUrl("jdbc:postgresql://localhost:5432/sk");
        dataSource.setUser("postgres");
        dataSource.setPassword("root");

        // Create a JDBC vector store
        var vectorStore = JDBCVectorStore.builder()
            .withDataSource(dataSource)
            .withOptions(
                JDBCVectorStoreOptions.builder()
                    .withQueryProvider(PostgreSQLVectorStoreQueryProvider.builder()
                        .withDataSource(dataSource)
                        .build())
                    .build())
            .build();

        // Get a collection from the vector store
        var collection = vectorStore.getCollection("skhotels",
            JDBCVectorStoreRecordCollectionOptions.<Hotel>builder()
                .withRecordClass(Hotel.class)
                .build());

        // Create the collection if it doesn't exist yet.
        collection.createCollectionAsync().block();

        // Upsert a record.
        var description = "A place where everyone can be happy";
        var hotelId = "1";
        var hotel = new Hotel(
            hotelId,
            "Hotel Happy",
            description,
            generateEmbeddingsAsync(description).block(),
            List.of("luxury", "pool"));

        collection.upsertAsync(hotel, null).block();

        // Retrieve the upserted record.
        var retrievedHotel = collection.getAsync(hotelId, null).block();

        // Generate a vector for your search text, using your chosen embedding generation implementation.
        // Just showing a placeholder method here for brevity.
        var searchVector = generateEmbeddingsAsync(
            "I'm looking for a hotel where customer happiness is the priority.").block();

        // Do the search.
        var searchResult = collection.searchAsync(searchVector, VectorSearchOptions.builder()
            .withTop(1).build()).block();

        Hotel record = searchResult.getResults().get(0).getRecord();
        System.out.printf("Found hotel description: %s\n", record.getDescription());
    }

    private static Mono<List<Float>> generateEmbeddingsAsync(String text) {
        return Mono.just(List.of(1.0f, 2.0f, 3.0f, 4.0f));
    }
}
