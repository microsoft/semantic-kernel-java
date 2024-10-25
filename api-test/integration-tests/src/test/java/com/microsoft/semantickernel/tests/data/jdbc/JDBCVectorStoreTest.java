package com.microsoft.semantickernel.tests.data.jdbc;

import com.microsoft.semantickernel.data.jdbc.hsqldb.HSQLDBVectorStoreQueryProvider;
import com.microsoft.semantickernel.data.jdbc.JDBCVectorStore;
import com.microsoft.semantickernel.data.jdbc.JDBCVectorStoreOptions;
import com.microsoft.semantickernel.data.jdbc.SQLVectorStoreQueryProvider;
import com.microsoft.semantickernel.data.jdbc.JDBCVectorStoreRecordCollectionOptions;
import com.microsoft.semantickernel.data.jdbc.mysql.MySQLVectorStoreQueryProvider;
import com.microsoft.semantickernel.data.jdbc.postgres.PostgreSQLVectorStoreQueryProvider;
import com.microsoft.semantickernel.data.jdbc.sqlite.SQLiteVectorStoreQueryProvider;
import com.mysql.cj.jdbc.MysqlDataSource;
import org.hsqldb.jdbc.JDBCDataSourceFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.postgresql.ds.PGSimpleDataSource;
import org.sqlite.SQLiteDataSource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.microsoft.semantickernel.tests.data.jdbc.JDBCVectorStoreRecordCollectionTest.QueryProvider;

import static com.microsoft.semantickernel.tests.data.jdbc.JDBCVectorStoreRecordCollectionTest.createTempDbFile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
public class JDBCVectorStoreTest {

    @Container
    private static final MySQLContainer<?> MYSQL_CONTAINER = new MySQLContainer<>("mysql:5.7.34");

    private static final DockerImageName PGVECTOR = DockerImageName.parse("pgvector/pgvector:pg16")
        .asCompatibleSubstituteFor("postgres");
    @Container
    private static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER = new PostgreSQLContainer<>(
        PGVECTOR);

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
            case SQLite:
                Path sqliteDb = createTempDbFile("testSQLite");
                SQLiteDataSource sqliteDataSource = new SQLiteDataSource();
                sqliteDataSource.setUrl("jdbc:sqlite:file:" + sqliteDb.toFile().getAbsolutePath());
                dataSource = sqliteDataSource;

                queryProvider = SQLiteVectorStoreQueryProvider.builder()
                    .withDataSource(dataSource)
                    .build();
                break;
            case HSQLDB:
                try {
                    Path file = createTempDbFile("testHSQLDB");

                    Properties properties = new Properties();
                    properties.putAll(
                        Map.of(
                            "url", "jdbc:hsqldb:file:" + file.toFile().getAbsolutePath()
                                + ";sql.syntax_mys=true",
                            "user", "SA",
                            "password", ""
                        )
                    );

                    dataSource = JDBCDataSourceFactory.createDataSource(properties);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                queryProvider = HSQLDBVectorStoreQueryProvider.builder()
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
