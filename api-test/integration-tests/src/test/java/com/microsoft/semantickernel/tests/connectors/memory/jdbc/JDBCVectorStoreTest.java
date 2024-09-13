package com.microsoft.semantickernel.tests.connectors.memory.jdbc;

import com.microsoft.semantickernel.connectors.data.jdbc.JDBCVectorStore;
import com.microsoft.semantickernel.connectors.data.jdbc.JDBCVectorStoreOptions;
import com.microsoft.semantickernel.connectors.data.jdbc.SQLVectorStoreQueryProvider;
import com.microsoft.semantickernel.connectors.data.jdbc.JDBCVectorStoreRecordCollectionOptions;
import com.microsoft.semantickernel.connectors.data.mysql.MySQLVectorStoreQueryProvider;
import com.microsoft.semantickernel.connectors.data.postgres.PostgreSQLVectorStoreQueryProvider;
import com.microsoft.semantickernel.tests.connectors.memory.Hotel;
import com.mysql.cj.jdbc.MysqlDataSource;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;

import com.microsoft.semantickernel.tests.connectors.memory.jdbc.JDBCVectorStoreRecordCollectionTest.QueryProvider;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
public class JDBCVectorStoreTest {
    @Container
    private static final MySQLContainer<?> MYSQL_CONTAINER = new MySQLContainer<>("mysql:5.7.34");

    private static final DockerImageName PGVECTOR = DockerImageName.parse("pgvector/pgvector:pg16").asCompatibleSubstituteFor("postgres");
    @Container
    private static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER = new PostgreSQLContainer<>(PGVECTOR);

    private JDBCVectorStore buildVectorStore(QueryProvider provider) {
        SQLVectorStoreQueryProvider queryProvider;
        DataSource dataSource;

        switch (provider) {
            case MySQL:
                MysqlDataSource mysqlDataSource = new MysqlDataSource();
                mysqlDataSource.setUrl(MYSQL_CONTAINER.getJdbcUrl());
                mysqlDataSource.setUser(MYSQL_CONTAINER.getUsername());
                mysqlDataSource.setPassword(MYSQL_CONTAINER.getPassword());
                dataSource = mysqlDataSource;
                queryProvider = MySQLVectorStoreQueryProvider.builder()
                        .withDataSource(dataSource)
                        .build();
                break;
            case PostgreSQL:
                PGSimpleDataSource pgSimpleDataSource = new PGSimpleDataSource();
                pgSimpleDataSource.setUrl(POSTGRESQL_CONTAINER.getJdbcUrl());
                pgSimpleDataSource.setUser(POSTGRESQL_CONTAINER.getUsername());
                pgSimpleDataSource.setPassword(POSTGRESQL_CONTAINER.getPassword());
                dataSource = pgSimpleDataSource;
                queryProvider = PostgreSQLVectorStoreQueryProvider.builder()
                        .withDataSource(dataSource)
                        .build();
                break;
            default:
                throw new IllegalArgumentException("Unknown query provider: " + provider);
        }


        JDBCVectorStore vectorStore = JDBCVectorStore.builder()
                .withDataSource(dataSource)
                .withOptions(
                        JDBCVectorStoreOptions.builder()
                                .withQueryProvider(queryProvider)
                                .build()
                )
                .build();

        vectorStore.prepareAsync().block();
        return vectorStore;
    }


    @ParameterizedTest
    @EnumSource(QueryProvider.class)
    public void getCollectionNamesAsync(QueryProvider provider) {
        JDBCVectorStore vectorStore = buildVectorStore(provider);

        vectorStore.getCollectionNamesAsync().block();

        List<String> collectionNames = Arrays.asList("collection1", "collection2", "collection3");

        for (String collectionName : collectionNames) {
            vectorStore.getCollection(collectionName,
                    JDBCVectorStoreRecordCollectionOptions.<Hotel>builder()
                    .withRecordClass(Hotel.class)
                    .build()).createCollectionAsync().block();
        }

        List<String> retrievedCollectionNames = vectorStore.getCollectionNamesAsync().block();
        assertNotNull(retrievedCollectionNames);
        assertEquals(collectionNames.size(), retrievedCollectionNames.size());
        for (String collectionName : collectionNames) {
            assertTrue(retrievedCollectionNames.contains(collectionName));
        }
    }
}
