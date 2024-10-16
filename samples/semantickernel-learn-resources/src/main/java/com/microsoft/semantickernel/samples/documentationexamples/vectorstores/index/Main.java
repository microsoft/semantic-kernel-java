package com.microsoft.semantickernel.samples.documentationexamples.vectorstores.index;

import com.microsoft.semantickernel.connectors.data.jdbc.JDBCVectorStore;
import com.microsoft.semantickernel.connectors.data.jdbc.JDBCVectorStoreOptions;
import com.microsoft.semantickernel.connectors.data.jdbc.JDBCVectorStoreRecordCollectionOptions;
import com.microsoft.semantickernel.connectors.data.mysql.MySQLVectorStoreQueryProvider;
import com.mysql.cj.jdbc.MysqlDataSource;

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

        // Get a collection from the vector store
        var collection = vectorStore.getCollection("skhotels",
            JDBCVectorStoreRecordCollectionOptions.<Hotel>builder()
                .withRecordClass(Hotel.class)
                .build()
        );

        // Create the collection if it doesn't exist yet.
        collection.createCollectionAsync().block();

        // Upsert a record.
        var description = "A place where everyone can be happy";
        var hotelId = "hotel1";
        var hotel = new Hotel(hotelId, "Hotel Happy", description, generateEmbeddings(description));

        collection.upsertAsync(hotel, null).block();

        // Retrieve the upserted record.
        var retrievedHotel = collection.getAsync(hotelId, null).block();
    }

    private static List<Float> generateEmbeddings(String text) {
        return List.of(1.0f, 2.0f, 3.0f, 4.0f);
    }
}
