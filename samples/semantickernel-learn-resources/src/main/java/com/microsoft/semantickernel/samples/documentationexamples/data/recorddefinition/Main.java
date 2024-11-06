// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.samples.documentationexamples.data.recorddefinition;

import com.microsoft.semantickernel.data.jdbc.JDBCVectorStore;
import com.microsoft.semantickernel.data.jdbc.JDBCVectorStoreOptions;
import com.microsoft.semantickernel.data.jdbc.JDBCVectorStoreRecordCollectionOptions;
import com.microsoft.semantickernel.data.jdbc.postgres.PostgreSQLVectorStoreQueryProvider;
import com.microsoft.semantickernel.data.vectorstorage.definition.*;
import com.microsoft.semantickernel.samples.documentationexamples.data.index.Hotel;
import org.postgresql.ds.PGSimpleDataSource;

import java.util.Arrays;
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

        var hotelDefinition = VectorStoreRecordDefinition.fromFields(
            Arrays.asList(
                VectorStoreRecordKeyField.builder().withName("hotelId").withFieldType(String.class)
                    .build(),
                VectorStoreRecordDataField.builder()
                    .withName("name")
                    .withFieldType(String.class)
                    .isFilterable(true).build(),
                VectorStoreRecordDataField.builder()
                    .withName("description")
                    .withFieldType(String.class)
                    .isFullTextSearchable(true).build(),
                VectorStoreRecordVectorField.builder().withName("descriptionEmbedding")
                    .withDimensions(4)
                    .withIndexKind(IndexKind.HNSW)
                    .withDistanceFunction(DistanceFunction.COSINE_DISTANCE)
                    .withFieldType(List.class).build()));

        var collection = vectorStore.getCollection("skhotels",
            JDBCVectorStoreRecordCollectionOptions.<Hotel>builder()
                .withRecordDefinition(hotelDefinition)
                .withRecordClass(Hotel.class)
                .build());
    }
}
