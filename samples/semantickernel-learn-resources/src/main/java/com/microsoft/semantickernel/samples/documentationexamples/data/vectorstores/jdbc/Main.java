// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.samples.documentationexamples.data.vectorstores.jdbc;

import com.microsoft.semantickernel.data.jdbc.JDBCVectorStore;
import com.microsoft.semantickernel.data.jdbc.JDBCVectorStoreOptions;
import com.microsoft.semantickernel.data.jdbc.JDBCVectorStoreRecordCollection;
import com.microsoft.semantickernel.data.jdbc.JDBCVectorStoreRecordCollectionOptions;
import com.microsoft.semantickernel.data.jdbc.postgres.PostgreSQLVectorStoreQueryProvider;
import com.microsoft.semantickernel.samples.documentationexamples.data.index.Hotel;
import org.postgresql.ds.PGSimpleDataSource;

public class Main {
    public static void main(String[] args) {
        // Configure the data source
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUrl("jdbc:postgresql://localhost:5432/sk");
        dataSource.setUser("postgres");
        dataSource.setPassword("root");

        // Build a query provider
        // Other available query providers are MySQLVectorStoreQueryProvider, SQLiteVectorStoreQueryProvider
        // and HSQDBVectorStoreQueryProvider
        var queryProvider = PostgreSQLVectorStoreQueryProvider.builder()
            .withDataSource(dataSource)
            .build();

        // Build a vector store
        var vectorStore = JDBCVectorStore.builder()
            .withDataSource(dataSource)
            .withOptions(JDBCVectorStoreOptions.builder()
                .withQueryProvider(queryProvider)
                .build())
            .build();

        var collection = new JDBCVectorStoreRecordCollection<>(
            dataSource,
            "skhotels",
            JDBCVectorStoreRecordCollectionOptions.<Hotel>builder()
                .withRecordClass(Hotel.class)
                .build());
    }
}
