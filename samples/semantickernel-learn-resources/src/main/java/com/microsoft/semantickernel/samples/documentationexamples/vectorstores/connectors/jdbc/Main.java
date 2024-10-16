package com.microsoft.semantickernel.samples.documentationexamples.vectorstores.connectors.jdbc;

import com.microsoft.semantickernel.connectors.data.jdbc.JDBCVectorStore;
import com.microsoft.semantickernel.connectors.data.jdbc.JDBCVectorStoreOptions;
import com.microsoft.semantickernel.connectors.data.jdbc.JDBCVectorStoreRecordCollection;
import com.microsoft.semantickernel.connectors.data.jdbc.JDBCVectorStoreRecordCollectionOptions;
import com.microsoft.semantickernel.connectors.data.mysql.MySQLVectorStoreQueryProvider;
import com.microsoft.semantickernel.samples.documentationexamples.vectorstores.index.Hotel;
import com.mysql.cj.jdbc.MysqlDataSource;

public class Main {
    public static void main(String[] args) {
        // Configure the data source
        var dataSource = new MysqlDataSource();
        dataSource.setUrl("jdbc:mysql://localhost:3306/sk");
        dataSource.setPassword("root");
        dataSource.setUser("root");

        // Build a query provider
        // Other available query providers are PostgreSQLVectorStoreQueryProvider, SQLiteVectorStoreQueryProvider
        // and HSQDBVectorStoreQueryProvider
        var queryProvider = MySQLVectorStoreQueryProvider.builder()
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
                .build()
        );
    }
}
