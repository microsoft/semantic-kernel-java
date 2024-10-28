package com.microsoft.semantickernel.tests.data.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.microsoft.semantickernel.data.jdbc.hsqldb.HSQLDBVectorStoreQueryProvider;
import com.microsoft.semantickernel.data.jdbc.JDBCVectorStoreRecordCollection;
import com.microsoft.semantickernel.data.jdbc.JDBCVectorStoreRecordCollectionOptions;
import com.microsoft.semantickernel.data.jdbc.SQLVectorStoreQueryProvider;
import com.microsoft.semantickernel.data.jdbc.mysql.MySQLVectorStoreQueryProvider;
import com.microsoft.semantickernel.data.jdbc.postgres.PostgreSQLVectorStoreQueryProvider;
import com.microsoft.semantickernel.data.jdbc.sqlite.SQLiteVectorStoreQueryProvider;
import com.microsoft.semantickernel.data.vectorsearch.VectorSearchFilter;
import com.microsoft.semantickernel.data.vectorsearch.VectorSearchResult;
import com.microsoft.semantickernel.data.vectorstorage.options.GetRecordOptions;
import com.microsoft.semantickernel.data.vectorstorage.options.VectorSearchOptions;
import com.mysql.cj.jdbc.MysqlDataSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.sql.DataSource;
import org.hsqldb.jdbc.JDBCDataSourceFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.postgresql.ds.PGSimpleDataSource;
import org.sqlite.SQLiteDataSource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;


@Testcontainers
public class JDBCVectorStoreRecordCollectionTest {

    @Container
    private static final MySQLContainer<?> MYSQL_CONTAINER = new MySQLContainer<>("mysql:5.7.34");

    private static final DockerImageName PGVECTOR = DockerImageName.parse("pgvector/pgvector:pg16")
        .asCompatibleSubstituteFor("postgres");
    @Container
    private static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER = new PostgreSQLContainer<>(
        PGVECTOR);

    public enum QueryProvider {
        MySQL,
        PostgreSQL,
        SQLite,
        HSQLDB
    }

    static Path createTempDbFile(String prefix) {
        try {
            Path file = Files.createTempFile(prefix, ".db");
            file.toFile().deleteOnExit();
            return file;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private JDBCVectorStoreRecordCollection<Hotel> buildRecordCollection(QueryProvider provider,
        @Nonnull String collectionName) {
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
                Path sqliteDb = createTempDbFile("sqliteDb");
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

        JDBCVectorStoreRecordCollection<Hotel> recordCollection = new JDBCVectorStoreRecordCollection<>(
            dataSource,
            collectionName,
            JDBCVectorStoreRecordCollectionOptions.<Hotel>builder()
                .withRecordClass(Hotel.class)
                .withQueryProvider(queryProvider)
                .build());

        recordCollection.prepareAsync().block();
        recordCollection.createCollectionIfNotExistsAsync().block();
        return recordCollection;
    }

    @ParameterizedTest
    @EnumSource(QueryProvider.class)
    public void buildRecordCollection(QueryProvider provider) {
        assertNotNull(buildRecordCollection(provider, "buildTest"));
    }

    private List<Hotel> getHotels() {
        return Arrays.asList(
            new Hotel("id_1", "Hotel 1", 1, "Hotel 1 description",
                Arrays.asList(0.5f, 3.2f, 7.1f, -4.0f, 2.8f, 10.0f, -1.3f, 5.5f), null, null, null,
                4.0, Arrays.asList("luxury", "city")),
            new Hotel("id_2", "Hotel 2", 2, "Hotel 2 description",
                Arrays.asList(-2.0f, 8.1f, 0.9f, 5.4f, -3.3f, 2.2f, 9.9f, -4.5f), null, null, null,
                4.0, Arrays.asList("luxury", "city")),
            new Hotel("id_3", "Hotel 3", 3, "Hotel 3 description",
                Arrays.asList(4.5f, -6.2f, 3.1f, 7.7f, -0.8f, 1.1f, -2.2f, 8.3f), null, null, null,
                5.0, Arrays.asList("luxury", "beach")),
            new Hotel("id_4", "Hotel 4", 4, "Hotel 4 description",
                Arrays.asList(7.0f, 1.2f, -5.3f, 2.5f, 6.6f, -7.8f, 3.9f, -0.1f), null, null, null,
                4.0, Arrays.asList("luxury", "city")),
            new Hotel("id_5", "Hotel 5", 5, "Hotel 5 description",
                Arrays.asList(-3.5f, 4.4f, -1.2f, 9.9f, 5.7f, -6.1f, 7.8f, -2.0f), null, null, null,
                4.0, Arrays.asList("luxury", "city"))
        );
    }

    /**
     * Search embeddings similar to the third hotel embeddings. In order of similarity: 1. Hotel 3
     * 2. Hotel 1 3. Hotel 4
     */
    private static final List<Float> SEARCH_EMBEDDINGS = Arrays.asList(4.5f, -6.2f, 3.1f, 7.7f,
        -0.8f, 1.1f, -2.2f, 8.2f);

    @ParameterizedTest
    @EnumSource(QueryProvider.class)
    public void upsertAndGetRecordAsync(QueryProvider provider) {
        String collectionName = "upsertAndGetRecordAsync";
        JDBCVectorStoreRecordCollection<Hotel> recordCollection = buildRecordCollection(provider,
            collectionName);

        List<Hotel> hotels = getHotels();
        for (Hotel hotel : hotels) {
            recordCollection.upsertAsync(hotel, null).block();
        }

        // Upsert the first time
        for (Hotel hotel : hotels) {
            Hotel retrievedHotel = recordCollection.getAsync(hotel.getId(), null).block();
            assertNotNull(retrievedHotel);
            assertEquals(hotel.getId(), retrievedHotel.getId());
            assertEquals(hotel.getRating(), retrievedHotel.getRating());

            // Update the rating
            hotel.setRating(1.0);
        }

        // Upsert the second time with updated rating
        for (Hotel hotel : hotels) {
            recordCollection.upsertAsync(hotel, null).block();
        }

        for (Hotel hotel : hotels) {
            Hotel retrievedHotel = recordCollection.getAsync(hotel.getId(), null).block();
            assertNotNull(retrievedHotel);
            assertEquals(hotel.getId(), retrievedHotel.getId());
            assertEquals(1.0, retrievedHotel.getRating());
        }
    }

    @ParameterizedTest
    @EnumSource(QueryProvider.class)
    public void getBatchAsync(QueryProvider provider) {
        String collectionName = "getBatchAsync";
        JDBCVectorStoreRecordCollection<Hotel> recordCollection = buildRecordCollection(provider,
            collectionName);

        List<Hotel> hotels = getHotels();
        for (Hotel hotel : hotels) {
            recordCollection.upsertAsync(hotel, null).block();
        }

        List<String> keys = new ArrayList<>();
        for (Hotel hotel : hotels) {
            keys.add(hotel.getId());
        }

        List<Hotel> retrievedHotels = recordCollection.getBatchAsync(keys, null).block();
        assertNotNull(retrievedHotels);
        assertEquals(hotels.size(), retrievedHotels.size());
    }

    @ParameterizedTest
    @EnumSource(QueryProvider.class)
    public void upsertBatchAndGetBatchAsync(QueryProvider provider) {
        String collectionName = "upsertBatchAndGetBatchAsync";
        JDBCVectorStoreRecordCollection<Hotel> recordCollection = buildRecordCollection(provider,
            collectionName);

        List<Hotel> hotels = getHotels();
        recordCollection.upsertBatchAsync(hotels, null).block();

        List<String> keys = new ArrayList<>();
        for (Hotel hotel : hotels) {
            keys.add(hotel.getId());
        }

        List<Hotel> retrievedHotels = recordCollection.getBatchAsync(keys, null).block();
        assertNotNull(retrievedHotels);
        assertEquals(hotels.size(), retrievedHotels.size());
    }

    @ParameterizedTest
    @EnumSource(QueryProvider.class)
    public void insertAndReplaceAsync(QueryProvider provider) {
        String collectionName = "insertAndReplaceAsync";
        JDBCVectorStoreRecordCollection<Hotel> recordCollection = buildRecordCollection(provider,
            collectionName);

        List<Hotel> hotels = getHotels();
        recordCollection.upsertBatchAsync(hotels, null).block();
        recordCollection.upsertBatchAsync(hotels, null).block();
        recordCollection.upsertBatchAsync(hotels, null).block();

        List<String> keys = new ArrayList<>();
        for (Hotel hotel : hotels) {
            keys.add(hotel.getId());
        }

        List<Hotel> retrievedHotels = recordCollection.getBatchAsync(keys, null).block();
        assertNotNull(retrievedHotels);
        assertEquals(hotels.size(), retrievedHotels.size());
    }

    @ParameterizedTest
    @EnumSource(QueryProvider.class)
    public void deleteRecordAsync(QueryProvider provider) {
        String collectionName = "deleteRecordAsync";
        JDBCVectorStoreRecordCollection<Hotel> recordCollection = buildRecordCollection(provider,
            collectionName);

        List<Hotel> hotels = getHotels();
        recordCollection.upsertBatchAsync(hotels, null).block();

        for (Hotel hotel : hotels) {
            recordCollection.deleteAsync(hotel.getId(), null).block();
            Hotel retrievedHotel = recordCollection.getAsync(hotel.getId(), null).block();
            assertNull(retrievedHotel);
        }
    }

    @ParameterizedTest
    @EnumSource(QueryProvider.class)
    public void deleteBatchAsync(QueryProvider provider) {
        String collectionName = "deleteBatchAsync";
        JDBCVectorStoreRecordCollection<Hotel> recordCollection = buildRecordCollection(provider,
            collectionName);

        List<Hotel> hotels = getHotels();
        recordCollection.upsertBatchAsync(hotels, null).block();

        List<String> keys = new ArrayList<>();
        for (Hotel hotel : hotels) {
            keys.add(hotel.getId());
        }

        recordCollection.deleteBatchAsync(keys, null).block();

        for (String key : keys) {
            Hotel retrievedHotel = recordCollection.getAsync(key, null).block();
            assertNull(retrievedHotel);
        }
    }

    @ParameterizedTest
    @EnumSource(QueryProvider.class)
    public void getWithNoVectors(QueryProvider provider) {
        String collectionName = "getWithNoVectors";
        JDBCVectorStoreRecordCollection<Hotel> recordCollection = buildRecordCollection(provider,
            collectionName);

        List<Hotel> hotels = getHotels();
        recordCollection.upsertBatchAsync(hotels, null).block();

        GetRecordOptions options = GetRecordOptions.builder()
            .includeVectors(false)
            .build();

        for (Hotel hotel : hotels) {
            Hotel retrievedHotel = recordCollection.getAsync(hotel.getId(), options).block();
            assertNotNull(retrievedHotel);
            assertEquals(hotel.getId(), retrievedHotel.getId());
            assertNull(retrievedHotel.getEuclidean());
        }

        options = GetRecordOptions.builder()
            .includeVectors(true)
            .build();

        for (Hotel hotel : hotels) {
            Hotel retrievedHotel = recordCollection.getAsync(hotel.getId(), options).block();
            assertNotNull(retrievedHotel);
            assertEquals(hotel.getId(), retrievedHotel.getId());
            assertNotNull(retrievedHotel.getEuclidean());
        }
    }

    @ParameterizedTest
    @EnumSource(QueryProvider.class)
    public void getBatchWithNoVectors(QueryProvider provider) {
        String collectionName = "getBatchWithNoVectors";
        JDBCVectorStoreRecordCollection<Hotel> recordCollection = buildRecordCollection(provider,
            collectionName);

        List<Hotel> hotels = getHotels();
        recordCollection.upsertBatchAsync(hotels, null).block();

        GetRecordOptions options = GetRecordOptions.builder()
            .includeVectors(false)
            .build();

        List<String> keys = new ArrayList<>();
        for (Hotel hotel : hotels) {
            keys.add(hotel.getId());
        }

        List<Hotel> retrievedHotels = recordCollection.getBatchAsync(keys, options).block();
        assertNotNull(retrievedHotels);
        assertEquals(hotels.size(), retrievedHotels.size());

        for (Hotel hotel : retrievedHotels) {
            assertNull(hotel.getEuclidean());
        }

        options = GetRecordOptions.builder()
            .includeVectors(true)
            .build();

        retrievedHotels = recordCollection.getBatchAsync(keys, options).block();
        assertNotNull(retrievedHotels);
        assertEquals(hotels.size(), retrievedHotels.size());

        for (Hotel hotel : retrievedHotels) {
            assertNotNull(hotel.getEuclidean());
        }
    }

    private static Stream<Arguments> provideSearchParameters() {
        return Arrays.stream(QueryProvider.values()).map(provider ->
            Stream.of(
                Arguments.of(provider, "euclidean"),
                Arguments.of(provider, "cosineDistance"),
                Arguments.of(provider, "dotProduct")
            )
        ).flatMap(s -> s);
    }

    @ParameterizedTest
    @MethodSource("provideSearchParameters")
    public void exactSearch(QueryProvider provider, String embeddingName) {
        String collectionName = "search" + embeddingName;
        JDBCVectorStoreRecordCollection<Hotel> recordCollection = buildRecordCollection(provider,
            collectionName);

        List<Hotel> hotels = getHotels();
        recordCollection.upsertBatchAsync(hotels, null).block();

        VectorSearchOptions options = VectorSearchOptions.builder()
            .withVectorFieldName(embeddingName)
            .withTop(3)
            .build();

        // Embeddings similar to the third hotel
        List<VectorSearchResult<Hotel>> results = recordCollection.searchAsync(SEARCH_EMBEDDINGS,
            options).block().getResults();
        assertNotNull(results);
        assertEquals(3, results.size());
        // The third hotel should be the most similar
        assertEquals(hotels.get(2).getId(), results.get(0).getRecord().getId());

        options = VectorSearchOptions.builder()
            .withVectorFieldName(embeddingName)
            .withSkip(1)
            .withTop(-100)
            .build();

        // Skip the first result
        results = recordCollection.searchAsync(SEARCH_EMBEDDINGS, options).block().getResults();
        assertNotNull(results);
        assertEquals(1, results.size());
        // The first hotel should be the most similar
        assertEquals(hotels.get(0).getId(), results.get(0).getRecord().getId());
    }

    @ParameterizedTest
    @EnumSource(QueryProvider.class)
    public void approximateSearch(QueryProvider provider) {
        String collectionName = "searchWithIndex";
        JDBCVectorStoreRecordCollection<Hotel> recordCollection = buildRecordCollection(provider,
            collectionName);

        List<Hotel> hotels = getHotels();
        recordCollection.upsertBatchAsync(hotels, null).block();

        VectorSearchOptions options = VectorSearchOptions.builder()
            .withVectorFieldName("indexedEuclidean")
            .withTop(5)
            .build();

        // Embeddings similar to the third hotel
        List<VectorSearchResult<Hotel>> results = recordCollection.searchAsync(SEARCH_EMBEDDINGS,
            options).block().getResults();
        assertNotNull(results);
        assertEquals(5, results.size());
        // The third hotel should be the most similar
        assertEquals("id_3", results.get(0).getRecord().getId());
    }

    @ParameterizedTest
    @MethodSource("provideSearchParameters")
    public void searchWithFilterEqualToFilter(QueryProvider provider, String embeddingName) {
        String collectionName = "searchWithFilterEqualToFilter";
        JDBCVectorStoreRecordCollection<Hotel> recordCollection = buildRecordCollection(provider,
            collectionName);

        List<Hotel> hotels = getHotels();
        recordCollection.upsertBatchAsync(hotels, null).block();

        VectorSearchOptions options = VectorSearchOptions.builder()
            .withVectorFieldName(embeddingName)
            .withTop(3)
            .withVectorSearchFilter(
                VectorSearchFilter.builder()
                    .equalTo("rating", 4.0).build())
            .build();

        // Embeddings similar to the third hotel, but as the filter is set to 4.0, the third hotel should not be returned
        List<VectorSearchResult<Hotel>> results = recordCollection.searchAsync(SEARCH_EMBEDDINGS,
            options).block().getResults();
        assertNotNull(results);
        assertEquals(3, results.size());
        // The first hotel should be the most similar
        assertEquals("id_1", results.get(0).getRecord().getId());
    }

    @ParameterizedTest
    @MethodSource("provideSearchParameters")
    public void searchWithAnyTagEqualToFilter(QueryProvider provider, String embeddingName) {
        String collectionName = "searchWithAnyTagEqualToFilter";
        JDBCVectorStoreRecordCollection<Hotel> recordCollection = buildRecordCollection(provider,
                collectionName);

        List<Hotel> hotels = getHotels();
        recordCollection.upsertBatchAsync(hotels, null).block();

        VectorSearchOptions options = VectorSearchOptions.builder()
                .withVectorFieldName(embeddingName)
                .withTop(3)
                .withVectorSearchFilter(
                        VectorSearchFilter.builder()
                                .anyTagEqualTo("tags", "city").build())
                .build();

        // Embeddings similar to the third hotel, but as the filter is set to 4.0, the third hotel should not be returned
        List<VectorSearchResult<Hotel>> results = recordCollection.searchAsync(SEARCH_EMBEDDINGS,
                options).block().getResults();
        assertNotNull(results);
        assertEquals(3, results.size());
        // The first hotel should be the most similar
        assertEquals("id_1", results.get(0).getRecord().getId());
    }

    // MySQL will always return the vectors as they're needed to compute the distances
    @Test
    public void postgresSearchIncludeAndNotIncludeVectors() {
        String collectionName = "searchIncludeAndNotIncludeVectors";
        JDBCVectorStoreRecordCollection<Hotel> recordCollection = buildRecordCollection(
            QueryProvider.PostgreSQL, collectionName);

        List<Hotel> hotels = getHotels();
        recordCollection.upsertBatchAsync(hotels, null).block();

        List<VectorSearchResult<Hotel>> results = recordCollection.searchAsync(SEARCH_EMBEDDINGS,
            null).block().getResults();
        assertNotNull(results);
        assertEquals(3, results.size());
        // The third hotel should be the most similar
        assertEquals(hotels.get(2).getId(), results.get(0).getRecord().getId());
        assertNull(results.get(0).getRecord().getEuclidean());

        VectorSearchOptions options = VectorSearchOptions.builder()
            .withIncludeVectors(true)
            .build();

        results = recordCollection.searchAsync(SEARCH_EMBEDDINGS, options).block().getResults();
        assertNotNull(results);
        assertEquals(3, results.size());
        // The third hotel should be the most similar
        assertEquals("id_3", results.get(0).getRecord().getId());
        assertNotNull(results.get(0).getRecord().getEuclidean());
    }
}
