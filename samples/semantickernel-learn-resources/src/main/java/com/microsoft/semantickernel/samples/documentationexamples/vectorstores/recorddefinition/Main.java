package com.microsoft.semantickernel.samples.documentationexamples.vectorstores.recorddefinition;

import com.microsoft.semantickernel.connectors.data.jdbc.JDBCVectorStore;
import com.microsoft.semantickernel.connectors.data.jdbc.JDBCVectorStoreOptions;
import com.microsoft.semantickernel.connectors.data.jdbc.JDBCVectorStoreRecordCollectionOptions;
import com.microsoft.semantickernel.connectors.data.mysql.MySQLVectorStoreQueryProvider;
import com.microsoft.semantickernel.data.vectorstorage.definition.*;
import com.mysql.cj.jdbc.MysqlDataSource;

import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // Create a MySQL data source
        var dataSource = new MysqlDataSource();
        dataSource.setUrl("jdbc:mysql://localhost:3306/sk");
        dataSource.setPassword("root");
        dataSource.setUser("root");

        // Create a JDBC vector store
        var vectorStore = JDBCVectorStore.builder()
                .withDataSource(dataSource)
                .withOptions(
                        JDBCVectorStoreOptions.builder()
                                .withQueryProvider(MySQLVectorStoreQueryProvider.builder()
                                        .withDataSource(dataSource)
                                        .build())
                                .build()
                )
                .build();

        var hotelDefinition = VectorStoreRecordDefinition.fromFields(
            Arrays.asList(
                VectorStoreRecordKeyField.builder().withName("hotelId").withFieldType(String.class).build(),
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
                    .withFieldType(List.class).build()
            )
        );

        var collection = vectorStore.getCollection("skhotels",
            JDBCVectorStoreRecordCollectionOptions.builder()
                .withRecordDefinition(hotelDefinition)
                .build()
        );
    }
}
