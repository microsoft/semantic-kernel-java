package com.microsoft.semantickernel.data.jdbc.oracle;

import com.microsoft.semantickernel.data.VolatileVectorStoreRecordCollection;
import com.microsoft.semantickernel.data.VolatileVectorStoreRecordCollectionOptions;
import com.microsoft.semantickernel.data.jdbc.JDBCVectorStore;
import com.microsoft.semantickernel.data.jdbc.JDBCVectorStoreOptions;
import com.microsoft.semantickernel.data.jdbc.JDBCVectorStoreRecordCollectionOptions;
import com.microsoft.semantickernel.data.vectorsearch.VectorSearchFilter;
import com.microsoft.semantickernel.data.vectorsearch.VectorSearchResult;
import com.microsoft.semantickernel.data.vectorstorage.VectorStoreRecordCollection;
import com.microsoft.semantickernel.data.vectorstorage.definition.DistanceFunction;
import com.microsoft.semantickernel.data.vectorstorage.options.VectorSearchOptions;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.datasource.impl.OracleDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.testcontainers.oracle.OracleContainer;
import org.testcontainers.utility.MountableFile;

public class OracleVectorStoreRecordCollectionTest {
    private static VectorStoreRecordCollection<String, Hotel> recordCollection;

    private static final String ORACLE_IMAGE_NAME = "gvenzl/oracle-free:23.7-slim-faststart";
    private static final OracleDataSource DATA_SOURCE;
    private static final OracleDataSource SYSDBA_DATA_SOURCE;


    static {

        try {
            DATA_SOURCE = new oracle.jdbc.datasource.impl.OracleDataSource();
            SYSDBA_DATA_SOURCE = new oracle.jdbc.datasource.impl.OracleDataSource();
            String urlFromEnv = System.getenv("ORACLE_JDBC_URL");

            if (urlFromEnv == null) {
                // The Ryuk component is relied upon to stop this container.
                OracleContainer oracleContainer = new OracleContainer(ORACLE_IMAGE_NAME)
                    .withCopyFileToContainer(MountableFile.forClasspathResource("/initialize.sql"),
                        "/container-entrypoint-initdb.d/initialize.sql")
                    .withStartupTimeout(Duration.ofSeconds(600))
                    .withConnectTimeoutSeconds(600)
                    .withDatabaseName("pdb1")
                    .withUsername("testuser")
                    .withPassword("testpwd");
                oracleContainer.start();

                initDataSource(
                    DATA_SOURCE,
                    oracleContainer.getJdbcUrl(),
                    oracleContainer.getUsername(),
                    oracleContainer.getPassword());
                initDataSource(SYSDBA_DATA_SOURCE, oracleContainer.getJdbcUrl(), "sys", oracleContainer.getPassword());
            } else {
                initDataSource(
                    DATA_SOURCE,
                    urlFromEnv,
                    System.getenv("ORACLE_JDBC_USER"),
                    System.getenv("ORACLE_JDBC_PASSWORD"));
                initDataSource(
                    SYSDBA_DATA_SOURCE,
                    urlFromEnv,
                    System.getenv("ORACLE_JDBC_USER"),
                    System.getenv("ORACLE_JDBC_PASSWORD"));
            }
            SYSDBA_DATA_SOURCE.setConnectionProperty(OracleConnection.CONNECTION_PROPERTY_INTERNAL_LOGON, "SYSDBA");

        } catch (SQLException sqlException) {
            throw new AssertionError(sqlException);
        }
    }

    static void initDataSource(OracleDataSource dataSource, String url, String username, String password) {
        dataSource.setURL(url);
        dataSource.setUser(username);
        dataSource.setPassword(password);
    }

    @BeforeAll
    public static void setup() throws Exception {

        // Build a query provider
        OracleVectorStoreQueryProvider queryProvider = OracleVectorStoreQueryProvider.builder()
            .withDataSource(DATA_SOURCE)
            .build();

        // Build a vector store
        JDBCVectorStore vectorStore = JDBCVectorStore.builder()
            .withDataSource(DATA_SOURCE)
            .withOptions(JDBCVectorStoreOptions.builder()
                .withQueryProvider(queryProvider)
                .build())
            .build();

        // Get a collection from the vector store
        recordCollection =
            vectorStore.getCollection("skhotels",
                JDBCVectorStoreRecordCollectionOptions.<Hotel>builder()
                    .withRecordClass(Hotel.class)
                    .build());

        recordCollection.createCollectionIfNotExistsAsync().block();
    }

    @BeforeEach
    public void clearCollection() {
        recordCollection.deleteCollectionAsync().block();
        recordCollection.createCollectionAsync().block();
    }

    private static List<Hotel> getHotels() {
        return Arrays.asList(
            new Hotel("id_1", "Hotel 1", 1, 1.49d, null, "Hotel 1 description",
                Arrays.asList(0.5f, 3.2f, 7.1f, -4.0f, 2.8f, 10.0f, -1.3f, 5.5f), null, null, null,
                4.0),
            new Hotel("id_2", "Hotel 2", 2, 1.44d, null, "Hotel 2 description with free-text search",
                Arrays.asList(-2.0f, 8.1f, 0.9f, 5.4f, -3.3f, 2.2f, 9.9f, -4.5f), null, null, null,
                4.0),
            new Hotel("id_3", "Hotel 3", 3, 1.53d, null, "Hotel 3 description",
                Arrays.asList(4.5f, -6.2f, 3.1f, 7.7f, -0.8f, 1.1f, -2.2f, 8.3f), null, null, null,
                5.0),
            new Hotel("id_4", "Hotel 4", 4, 1.35d, null, "Hotel 4 description",
                Arrays.asList(7.0f, 1.2f, -5.3f, 2.5f, 6.6f, -7.8f, 3.9f, -0.1f), null, null, null,
                4.0),
            new Hotel("id_5", "Hotel 5", 5, 1.89d, null,"Hotel 5 description",
                Arrays.asList(-3.5f, 4.4f, -1.2f, 9.9f, 5.7f, -6.1f, 7.8f, -2.0f), null, null, null,
                4.0));
    }

    /**
     * Search embeddings similar to the third hotel embeddings.
     * In order of similarity:
     * 1. Hotel 3
     * 2. Hotel 1
     * 3. Hotel 4
     */
    private static final List<Float> SEARCH_EMBEDDINGS = Arrays.asList(4.5f, -6.2f, 3.1f, 7.7f,
        -0.8f, 1.1f, -2.2f, 8.2f);

    @Test
    public void createAndDeleteCollectionAsync() {
        assertEquals(true, recordCollection.collectionExistsAsync().block());

        recordCollection.deleteCollectionAsync().block();
        assertEquals(false, recordCollection.collectionExistsAsync().block());

        recordCollection.createCollectionAsync().block();
        assertEquals(true, recordCollection.collectionExistsAsync().block());
    }

    @Test
    public void upsertRecordAsync() {
        List<Hotel> hotels = getHotels();
        for (Hotel hotel : hotels) {
            recordCollection.upsertAsync(hotel, null).block();
        }

        for (Hotel hotel : hotels) {
            Hotel retrievedHotel = recordCollection.getAsync(hotel.getId(), null).block();
            assertNotNull(retrievedHotel);
            assertEquals(hotel.getId(), retrievedHotel.getId());
            assertEquals(hotel.getName(), retrievedHotel.getName());
            assertEquals(hotel.getDescription(), retrievedHotel.getDescription());
        }
    }

    @Test
    public void upsertBatchAsync() {
        List<Hotel> hotels = getHotels();
        recordCollection.upsertBatchAsync(hotels, null).block();

        for (Hotel hotel : hotels) {
            Hotel retrievedHotel = recordCollection.getAsync(hotel.getId(), null).block();
            assertNotNull(retrievedHotel);
            assertEquals(hotel.getId(), retrievedHotel.getId());
            assertEquals(hotel.getName(), retrievedHotel.getName());
            assertEquals(hotel.getDescription(), retrievedHotel.getDescription());
        }
    }

    @Test
    public void getBatchAsync() {
        List<Hotel> hotels = getHotels();
        recordCollection.upsertBatchAsync(hotels, null).block();

        List<String> keys = hotels.stream().map(Hotel::getId).collect(Collectors.toList());
        List<Hotel> retrievedHotels = recordCollection.getBatchAsync(keys, null).block();

        assertNotNull(retrievedHotels);
        assertEquals(keys.size(), retrievedHotels.size());
        for (Hotel hotel : retrievedHotels) {
            assertTrue(keys.contains(hotel.getId()));
        }
    }

    @Test
    public void deleteRecordAsync() {
        List<Hotel> hotels = getHotels();
        recordCollection.upsertBatchAsync(hotels, null).block();

        for (Hotel hotel : hotels) {
            recordCollection.deleteAsync(hotel.getId(), null).block();
            assertNull(recordCollection.getAsync(hotel.getId(), null).block());
        }
    }

    @Test
    public void deleteBatchAsync() {
        List<Hotel> hotels = getHotels();
        recordCollection.upsertBatchAsync(hotels, null).block();

        List<String> keys = hotels.stream().map(Hotel::getId).collect(Collectors.toList());
        recordCollection.deleteBatchAsync(keys, null).block();

        for (String key : keys) {
            assertNull(recordCollection.getAsync(key, null).block());
        }
    }

    @ParameterizedTest
    @MethodSource("parametersExactSearch")
    public void exactSearch(DistanceFunction distanceFunction, List<Double> expectedDistance) {
        List<Hotel> hotels = getHotels();
        recordCollection.upsertBatchAsync(hotels, null).block();

        VectorSearchOptions options = VectorSearchOptions.builder()
            .withVectorFieldName(distanceFunction.getValue())
            .withTop(3)
            .build();

        // Embeddings similar to the third hotel
        List<VectorSearchResult<Hotel>> results = recordCollection
            .searchAsync(SEARCH_EMBEDDINGS, options).block().getResults();
        assertNotNull(results);
        assertEquals(3, results.size());
        // The third hotel should be the most similar
        System.out.println(results.get(0).getScore());
        System.out.println(results.get(1).getScore());
        System.out.println(results.get(2).getScore());
        assertEquals(hotels.get(2).getId(), results.get(0).getRecord().getId());
        assertEquals(expectedDistance.get(0).doubleValue(), results.get(0).getScore(), 0.0001d);
        assertEquals(hotels.get(0).getId(), results.get(1).getRecord().getId());
        assertEquals(expectedDistance.get(1).doubleValue(), results.get(1).getScore(), 0.0001d);
        assertEquals(hotels.get(3).getId(), results.get(2).getRecord().getId());
        assertEquals(expectedDistance.get(2).doubleValue(), results.get(2).getScore(), 0.0001d);

        options = VectorSearchOptions.builder()
            .withVectorFieldName(distanceFunction.getValue())
            .withSkip(1)
            .withTop(-100)
            .build();

        // Skip the first result
        results = recordCollection.searchAsync(SEARCH_EMBEDDINGS, options).block().getResults();
        assertNotNull(results);
        assertEquals(1, results.size());
        // The first hotel should be the most similar
        assertEquals(hotels.get(0).getId(), results.get(0).getRecord().getId());
        assertEquals(results.get(0).getScore(), expectedDistance.get(1), 0.001d);
    }

    @ParameterizedTest
    @MethodSource("distanceFunctionAndDistance")
    public void searchWithFilter(DistanceFunction distanceFunction, double expectedDistance) {
        List<Hotel> hotels = getHotels();
        recordCollection.upsertBatchAsync(hotels, null).block();

        VectorSearchOptions options = VectorSearchOptions.builder()
            .withVectorFieldName(distanceFunction.getValue())
            .withTop(3)
            .withVectorSearchFilter(
                VectorSearchFilter.builder()
                    .equalTo("rating", 4.0).build())
            .build();

        // Embeddings similar to the third hotel, but as the filter is set to 4.0, the third hotel should not be returned
        List<VectorSearchResult<Hotel>> results = recordCollection
            .searchAsync(SEARCH_EMBEDDINGS, options).block().getResults();
        assertNotNull(results);
        assertEquals(3, results.size());
        // The first hotel should be the most similar
        assertEquals(hotels.get(0).getId(), results.get(0).getRecord().getId());
        assertEquals(results.get(0).getScore(), expectedDistance, 0.0001d);
    }

    private static Stream<Arguments> distanceFunctionAndDistance() {
        return Stream.of(
            Arguments.of (DistanceFunction.COSINE_DISTANCE, 0.8548d),
            Arguments.of (DistanceFunction.COSINE_SIMILARITY, 0.1451d),
            Arguments.of (DistanceFunction.DOT_PRODUCT, 30.3399d),
            Arguments.of (DistanceFunction.EUCLIDEAN_DISTANCE, 18.9081d),
            Arguments.of (DistanceFunction.UNDEFINED, 18.9081d)
        );
    }

    private static Stream<Arguments> parametersExactSearch() {
        return Stream.of(
            Arguments.of (DistanceFunction.COSINE_SIMILARITY, Arrays.asList(0.9999d, 0.1451d, 0.0178d)),
            Arguments.of (DistanceFunction.COSINE_DISTANCE, Arrays.asList(1.6422E-5d, 0.8548d, 0.9821d)),
            Arguments.of (DistanceFunction.DOT_PRODUCT, Arrays.asList(202.3399d, 30.3399d, 3.6199d)),
            Arguments.of (DistanceFunction.EUCLIDEAN_DISTANCE, Arrays.asList(0.1000d, 18.9081d, 19.9669d)),
            Arguments.of (DistanceFunction.UNDEFINED, Arrays.asList(0.1000d, 18.9081d, 19.9669d))
        );
    }
}
