// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.samples.syntaxexamples.memory;

import com.microsoft.semantickernel.data.jdbc.JDBCVectorStore;
import com.microsoft.semantickernel.data.jdbc.JDBCVectorStoreOptions;
import com.microsoft.semantickernel.data.jdbc.JDBCVectorStoreRecordCollection;
import com.microsoft.semantickernel.data.jdbc.JDBCVectorStoreRecordCollectionOptions;
import com.microsoft.semantickernel.data.jdbc.oracle.OracleVectorStoreQueryProvider;
import com.microsoft.semantickernel.data.vectorstorage.VectorStoreRecordCollection;
import com.microsoft.semantickernel.samples.documentationexamples.data.index.Hotel;
import java.sql.SQLException;
import java.util.Collections;
import oracle.jdbc.datasource.impl.OracleDataSource;

public class VectorStoreWithOracle {

    public static void main(String[] args) throws SQLException {
        System.out.println("==============================================================");
        System.out.println("============== Oracle Vector Store Example ===================");
        System.out.println("==============================================================");

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
        collection.createCollectionAsync().block();

        collection.upsertAsync(new Hotel("1",
                "HotelOne",
                "Desc for HotelOne",
                    Collections.emptyList(), Collections.emptyList()),
                null)
            .block();

    }

}
