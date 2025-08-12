package com.microsoft.semantickernel.data.jdbc.oracle;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.semantickernel.data.jdbc.JDBCVectorStore;
import com.microsoft.semantickernel.data.jdbc.JDBCVectorStoreOptions;
import com.microsoft.semantickernel.data.jdbc.JDBCVectorStoreRecordCollectionOptions;
import com.microsoft.semantickernel.data.vectorsearch.VectorSearchFilter;
import com.microsoft.semantickernel.data.vectorsearch.VectorSearchResult;
import com.microsoft.semantickernel.data.vectorstorage.VectorStoreRecordCollection;
import com.microsoft.semantickernel.data.vectorstorage.definition.DistanceFunction;
import com.microsoft.semantickernel.data.vectorstorage.definition.IndexKind;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordDataField;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordDefinition;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordKeyField;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordVectorField;
import com.microsoft.semantickernel.data.vectorstorage.options.VectorSearchOptions;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OracleVectorStoreRecordCollectionTest extends OracleCommonVectorStoreRecordCollectionTest {
    private static VectorStoreRecordCollection<String, Hotel> recordCollection;

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
        List<Float> vec1 = Arrays.asList(0.5f, 3.2f, 7.1f, -4.0f, 2.8f, 10.0f, -1.3f, 5.5f);
        float[] arrayf1 = new float[] { 0.5f, 3.2f, 7.1f, -4.0f, 2.8f, 10.0f, -1.3f, 5.5f };
        Float[] arrayF1 = new Float[] { 0.5f, 3.2f, 7.1f, -4.0f, 2.8f, 10.0f, -1.3f, 5.5f };
        List<Float> vec2 = Arrays.asList(-2.0f, 8.1f, 0.9f, 5.4f, -3.3f, 2.2f, 9.9f, -4.5f);
        float[] arrayf2 = new float[] { -2.0f, 8.1f, 0.9f, 5.4f, -3.3f, 2.2f, 9.9f, -4.5f };
        Float[] arrayF2 = new Float[] { -2.0f, 8.1f, 0.9f, 5.4f, -3.3f, 2.2f, 9.9f, -4.5f };
        List<Float> vec3 = Arrays.asList(4.5f, -6.2f, 3.1f, 7.7f, -0.8f, 1.1f, -2.2f, 8.3f);
        float[] arrayf3 = new float[] { 4.5f, -6.2f, 3.1f, 7.7f, -0.8f, 1.1f, -2.2f, 8.3f };
        Float[] arrayF3 = new Float[] { 4.5f, -6.2f, 3.1f, 7.7f, -0.8f, 1.1f, -2.2f, 8.3f };
        List<Float> vec4 = Arrays.asList(7.0f, 1.2f, -5.3f, 2.5f, 6.6f, -7.8f, 3.9f, -0.1f);
        float[] arrayf4 = new float[] { 7.0f, 1.2f, -5.3f, 2.5f, 6.6f, -7.8f, 3.9f, -0.1f };
        Float[] arrayF4 = new Float[] { 7.0f, 1.2f, -5.3f, 2.5f, 6.6f, -7.8f, 3.9f, -0.1f };
        List<Float> vec5 =Arrays.asList(-3.5f, 4.4f, -1.2f, 9.9f, 5.7f, -6.1f, 7.8f, -2.0f);
        float[] arrayf5 = new float[] { -3.5f, 4.4f, -1.2f, 9.9f, 5.7f, -6.1f, 7.8f, -2.0f };
        Float[] arrayF5 = new Float[] { -3.5f, 4.4f, -1.2f, 9.9f, 5.7f, -6.1f, 7.8f, -2.0f };
        return Arrays.asList(
            new Hotel("id_1", "Hotel 1", 1, 1.49d, Arrays.asList("one", "two"), "Hotel 1 description",
                vec1, arrayf1, arrayf1, arrayF1,
                4.0),
            new Hotel("id_2", "Hotel 2", 2, 1.44d, Arrays.asList("three", "four"), "Hotel 2 description with free-text search",
                vec2, arrayf2, arrayf2, arrayF2,
                4.0),
            new Hotel("id_3", "Hotel 3", 3, 1.53d, Arrays.asList("five", "six"), "Hotel 3 description",
                vec3, arrayf3, arrayf3, arrayF3,
                5.0),
            new Hotel("id_4", "Hotel 4", 4, 1.35d, Arrays.asList("seven", "eight"), "Hotel 4 description",
                vec4, arrayf4, arrayf4, arrayF4,
                4.0),
            new Hotel("id_5", "Hotel 5", 5, 1.89d, Arrays.asList("nine", "ten"),"Hotel 5 description",
                vec5, arrayf5, arrayf5, arrayF5,
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


    @Test
    public void searchWithTagFilter() {
        List<Hotel> hotels = getHotels();
        recordCollection.upsertBatchAsync(hotels, null).block();

        VectorSearchOptions options = VectorSearchOptions.builder()
//            .withVectorFieldName("")
            .withTop(3)
            .withVectorSearchFilter(
                VectorSearchFilter.builder()
                    .anyTagEqualTo("tags", "three")
                    .build())
            .build();

        // Embeddings similar to the third hotel, but as the filter is set to 4.0, the third hotel should not be returned
        List<VectorSearchResult<Hotel>> results = recordCollection
            .searchAsync(SEARCH_EMBEDDINGS, options).block().getResults();
        assertNotNull(results);
        assertEquals(1, results.size());
        // The second hotel contains the tag we are searching for
        assertEquals(hotels.get(1).getId(), results.get(0).getRecord().getId());
    }

    @ParameterizedTest
    @MethodSource("supportedKeyTypes")
    <T> void testKeyTypes(String suffix, Class<?> keyType, Object keyValue) {
        VectorStoreRecordKeyField keyField = VectorStoreRecordKeyField.builder()
            .withName("id")
            .withStorageName("id")
            .withFieldType(keyType)
            .build();

        VectorStoreRecordDataField dummyField = VectorStoreRecordDataField.builder()
            .withName("dummy")
            .withStorageName("dummy")
            .withFieldType(String.class)
            .build();

        VectorStoreRecordVectorField dummyVector = VectorStoreRecordVectorField.builder()
            .withName("vec")
            .withStorageName("vec")
            .withFieldType(List.class)
            .withDimensions(2)
            .withDistanceFunction(DistanceFunction.EUCLIDEAN_DISTANCE)
            .withIndexKind(IndexKind.UNDEFINED)
            .build();

        VectorStoreRecordDefinition definition = VectorStoreRecordDefinition.fromFields(
            Arrays.asList(keyField, dummyField, dummyVector)
        );

        OracleVectorStoreQueryProvider queryProvider = OracleVectorStoreQueryProvider.builder()
            .withDataSource(DATA_SOURCE)
            .build();

        JDBCVectorStore vectorStore = JDBCVectorStore.builder()
            .withDataSource(DATA_SOURCE)
            .withOptions(JDBCVectorStoreOptions.builder()
                .withQueryProvider(queryProvider)
                .build())
            .build();

        String collectionName = "test_keytype_" + suffix;

        VectorStoreRecordCollection collectionRaw =
            vectorStore.getCollection(collectionName,
                JDBCVectorStoreRecordCollectionOptions.<DummyRecordForKeyTypes>builder()
                    .withRecordClass(DummyRecordForKeyTypes.class)
                    .withRecordDefinition(definition)
                    .build());

        VectorStoreRecordCollection<Object, DummyRecordForKeyTypes> collection =
            (VectorStoreRecordCollection<Object, DummyRecordForKeyTypes>) collectionRaw;

        collection.createCollectionAsync().block();

        DummyRecordForKeyTypes record = new DummyRecordForKeyTypes(keyValue, "dummyValue", Arrays.asList(1.0f, 2.0f));
        collection.upsertAsync(record, null).block();

        DummyRecordForKeyTypes result = collection.getAsync(keyValue, null).block();
        assertNotNull(result);
        assertEquals("dummyValue", result.getDummy());

        collection.deleteCollectionAsync().block();
    }


    @Nested
    class HNSWIndexTests {
        @Test
        void testHNSWIndexIsCreatedSuccessfully() throws Exception {
            VectorStoreRecordKeyField keyField = VectorStoreRecordKeyField.builder()
                .withName("id")
                .withStorageName("id")
                .withFieldType(String.class)
                .build();

            VectorStoreRecordDataField dummyField = VectorStoreRecordDataField.builder()
                .withName("dummy")
                .withStorageName("dummy")
                .withFieldType(String.class)
                .isFilterable(false)
                .build();

            VectorStoreRecordVectorField hnswVector= VectorStoreRecordVectorField.builder()
                .withName("hnsw")
                .withStorageName("hnsw")
                .withFieldType(List.class)
                .withDimensions(8)
                .withDistanceFunction(DistanceFunction.COSINE_SIMILARITY)
                .withIndexKind(IndexKind.HNSW)
                .build();

            VectorStoreRecordDefinition definition = VectorStoreRecordDefinition.fromFields(
                Arrays.asList(keyField, dummyField, hnswVector)
            );

            OracleVectorStoreQueryProvider queryProvider = OracleVectorStoreQueryProvider.builder()
                .withDataSource(DATA_SOURCE)
                .build();

            JDBCVectorStore vectorStore = JDBCVectorStore.builder()
                .withDataSource(DATA_SOURCE)
                .withOptions(JDBCVectorStoreOptions.builder()
                    .withQueryProvider(queryProvider)
                    .build())
                .build();

            String collectionName = "skhotels_hnsw";
            VectorStoreRecordCollection<String, Object> collection =
                vectorStore.getCollection(collectionName,
                    JDBCVectorStoreRecordCollectionOptions.<Object>builder()
                        .withRecordClass(Object.class)
                        .withRecordDefinition(definition)
                        .build());

            // create collection
            collection.createCollectionAsync().block();

            String expectedIndexName = hnswVector.getEffectiveStorageName().toUpperCase() + "_VECTOR_INDEX";

            // check if index exist
            try (Connection conn = DATA_SOURCE.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                    "SELECT COUNT(*) FROM USER_INDEXES WHERE INDEX_NAME=?")) {
                stmt.setString(1, expectedIndexName);
                ResultSet rs = stmt.executeQuery();
                rs.next();
                int count = rs.getInt(1);

                assertEquals(1, count, "hnsw vector index should have been created");
            } finally {
                // clean up
                try (Connection conn = DATA_SOURCE.getConnection();
                    Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("DROP TABLE " + "SKCOLLECTION_" + collectionName);
                }
            }
        }
    }

    @Nested
    class UndefinedIndexTests {
        @Test
        void testNoIndexIsCreatedForUndefined() throws Exception {
            // create key field
            VectorStoreRecordKeyField keyField = VectorStoreRecordKeyField.builder()
                .withName("id")
                .withStorageName("id")
                .withFieldType(String.class)
                .build();

            // create vector field, set IndexKind to UNDEFINED
            VectorStoreRecordVectorField undefinedVector= VectorStoreRecordVectorField.builder()
                .withName("undef")
                .withStorageName("undef")
                .withFieldType(List.class)
                .withDimensions(8)
                .withDistanceFunction(DistanceFunction.COSINE_SIMILARITY)
                .withIndexKind(IndexKind.UNDEFINED)
                .build();

            VectorStoreRecordDataField dummyField = VectorStoreRecordDataField.builder()
                .withName("dummy")
                .withStorageName("dummy")
                .withFieldType(String.class)
                .isFilterable(false)
                .build();

            VectorStoreRecordDefinition definition = VectorStoreRecordDefinition.fromFields(
                Arrays.asList(keyField, dummyField,  undefinedVector)
            );

            OracleVectorStoreQueryProvider queryProvider = OracleVectorStoreQueryProvider.builder()
                .withDataSource(DATA_SOURCE)
                .build();

            JDBCVectorStore vectorStore = JDBCVectorStore.builder()
                .withDataSource(DATA_SOURCE)
                .withOptions(JDBCVectorStoreOptions.builder()
                    .withQueryProvider(queryProvider)
                    .build())
                .build();

            String collectionName = "skhotels_undefined";
            VectorStoreRecordCollection<String, Object> collection =
                vectorStore.getCollection(collectionName,
                    JDBCVectorStoreRecordCollectionOptions.<Object>builder()
                        .withRecordClass(Object.class)
                        .withRecordDefinition(definition)
                        .build());

            // create collection
            collection.createCollectionAsync().block();

            // check if index exist
            String expectedIndexName = undefinedVector.getEffectiveStorageName().toUpperCase() + "_VETCOR_INDEX";
            try (Connection conn = DATA_SOURCE.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                    "SELECT COUNT(*) FROM USER_INDEXES WHERE INDEX_NAME = ?")) {
                stmt.setString(1, expectedIndexName);
                ResultSet rs = stmt.executeQuery();
                rs.next();
                int count = rs.getInt(1);

                assertEquals(0,count,"Vector index should not be created for IndexKind.UNDEFINED");
            } finally {
                // clean up
                try (Connection conn = DATA_SOURCE.getConnection();
                    Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("DROP TABLE " + "SKCOLLECTION_" + collectionName);
                }
            }
        }
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

    // commented out temporarily because only String type key is supported in 
    // JDBCVectorStoreRecordCollection<Record>#getKeyFromRecord:
    // ...
    // return (String) keyField.get(data);
    // ...
    // thus upsertAync/getAsync won't work
    private static Stream<Arguments> supportedKeyTypes() {
        return Stream.of(
            Arguments.of("string", String.class, "asd123") /*,
            Arguments.of("integer", Integer.class, 321),
            Arguments.of("long", Long.class, 5L),
            Arguments.of("short", Short.class, (short) 3),
            Arguments.of("uuid", UUID.class, UUID.randomUUID())*/
        );
    }

    private static class DummyRecordForKeyTypes {
        private final Object id;
        private final String dummy;
        private final List<Float> vec;
        @JsonCreator
        public DummyRecordForKeyTypes(
            @JsonProperty("id")Object id,
            @JsonProperty("dummy") String dummy,
            @JsonProperty("vec") List<Float> vec) {
            this.id = id;
            this.dummy = dummy;
            this.vec = vec;
        }

        public Object getId() {
            return id;
        }

        public String getDummy() {
            return dummy;
        }

        @Override
        public String toString() {
            return String.valueOf(id);
        }
    }

    private static class DummyRecordForDataTypes {
        private final String id;
        private final Object dummy;
        private final List<Float> vec;
        @JsonCreator
        public DummyRecordForDataTypes(
            @JsonProperty("id") String id,
            @JsonProperty("dummy") Object dummy,
            @JsonProperty("vec") List<Float> vec) {
            this.id = id;
            this.dummy = dummy;
            this.vec = vec;
        }

        public String getId() {
            return id;
        }

        public Object getDummy() {
            return dummy;
        }

        @Override
        public String toString() {
            return String.valueOf(id);
        }
    }
}
