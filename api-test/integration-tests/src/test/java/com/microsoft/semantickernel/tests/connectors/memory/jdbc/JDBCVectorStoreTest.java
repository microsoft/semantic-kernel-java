package com.microsoft.semantickernel.tests.connectors.memory.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.semantickernel.connectors.data.jdbc.JDBCVectorStore;
import com.microsoft.semantickernel.connectors.data.jdbc.JDBCVectorStoreOptions;
import com.microsoft.semantickernel.connectors.data.jdbc.MySQLVectorStoreQueryProvider;
import com.microsoft.semantickernel.tests.connectors.memory.Hotel;
import com.mysql.cj.jdbc.MysqlDataSource;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class JDBCVectorStoreTest {
    @Container
    private static final MySQLContainer<?> CONTAINER = new MySQLContainer<>("mysql:5.7.34");
    private static final String MYSQL_USER = "test";
    private static final String MYSQL_PASSWORD = "test";
    private static MysqlDataSource dataSource;

    @BeforeAll
    static void setup() {
        dataSource = new MysqlDataSource();
        dataSource.setUrl(CONTAINER.getJdbcUrl());
        dataSource.setUser(MYSQL_USER);
        dataSource.setPassword(MYSQL_PASSWORD);
    }

    @Test
    public void getCollectionNamesAsync() {
        MySQLVectorStoreQueryProvider queryProvider = MySQLVectorStoreQueryProvider.builder()
                .withDataSource(dataSource)
                .build();

        JDBCVectorStore  vectorStore = JDBCVectorStore.builder()
                .withDataSource(dataSource)
                .withOptions(
                        JDBCVectorStoreOptions.builder()
                                .withQueryProvider(queryProvider)
                                .build()
                )
                .build();

        vectorStore.getCollectionNamesAsync().block();

        List<String> collectionNames = Arrays.asList("collection1", "collection2", "collection3");

        for (String collectionName : collectionNames) {
            vectorStore.getCollection(collectionName, Hotel.class, null).createCollectionAsync().block();
        }

        List<String> retrievedCollectionNames = vectorStore.getCollectionNamesAsync().block();
        assertNotNull(retrievedCollectionNames);
        assertEquals(collectionNames.size(), retrievedCollectionNames.size());
        for (String collectionName : collectionNames) {
            assertTrue(retrievedCollectionNames.contains(collectionName));
        }
    }
}
