package com.microsoft.semantickernel.tests.connectors.memory.redis;

import com.microsoft.semantickernel.connectors.data.redis.RedisJsonVectorStoreRecordCollection;
import com.microsoft.semantickernel.connectors.data.redis.RedisJsonVectorStoreRecordCollectionOptions;
import com.microsoft.semantickernel.data.vectorsearch.VectorSearchFilter;
import com.microsoft.semantickernel.data.vectorsearch.VectorSearchResult;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordDataField;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordDefinition;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordField;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordKeyField;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordVectorField;
import com.microsoft.semantickernel.data.vectorstorage.options.GetRecordOptions;
import com.microsoft.semantickernel.data.vectorstorage.options.VectorSearchOptions;
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import redis.clients.jedis.JedisPooled;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RedisJsonVectorStoreRecordCollectionTest {

    @Container private static final RedisContainer redisContainer = new RedisContainer("redis/redis-stack:latest");

    private static final Map<RecordCollectionOptions, RedisJsonVectorStoreRecordCollectionOptions<Hotel>> optionsMap = new HashMap<>();

    public enum RecordCollectionOptions {
        DEFAULT, WITH_CUSTOM_DEFINITION
    }

    @BeforeAll
    static void setup() {
        optionsMap.put(RecordCollectionOptions.DEFAULT, RedisJsonVectorStoreRecordCollectionOptions.<Hotel>builder()
                .withRecordClass(Hotel.class)
                .build());

        List<VectorStoreRecordField> fields = new ArrayList<>();
        fields.add(VectorStoreRecordKeyField.builder()
                .withName("id")
                .withFieldType(String.class)
                .build());
        fields.add(VectorStoreRecordDataField.builder()
                .withName("name")
                .withFieldType(String.class)
                .build());
        fields.add(VectorStoreRecordDataField.builder()
                .withName("code")
                .withFieldType(Integer.class)
                .build());
        fields.add(VectorStoreRecordDataField.builder()
                .withName("description")
                .withStorageName("summary")
                .withFieldType(String.class)
                .build());
        fields.add(VectorStoreRecordVectorField.builder()
                .withName("euclidean")
                .withStorageName("summaryEmbedding1")
                .withFieldType(List.class)
                .withDimensions(8)
                .build());
        fields.add(VectorStoreRecordVectorField.builder()
                .withName("cosineDistance")
                .withStorageName("summaryEmbedding2")
                .withFieldType(List.class)
                .withDimensions(8)
                .build());
        fields.add(VectorStoreRecordVectorField.builder()
                .withName("dotProduct")
                .withStorageName("summaryEmbedding3")
                .withFieldType(List.class)
                .withDimensions(8)
                .build());
        fields.add(VectorStoreRecordDataField.builder()
                .withName("rating")
                .withFieldType(Double.class)
                .isFilterable(true)
                .build());
        VectorStoreRecordDefinition recordDefinition = VectorStoreRecordDefinition.fromFields(fields);

        optionsMap.put(RecordCollectionOptions.WITH_CUSTOM_DEFINITION, RedisJsonVectorStoreRecordCollectionOptions.<Hotel>builder()
                .withRecordClass(Hotel.class)
                .withRecordDefinition(recordDefinition)
                .build());

        // Search configuration
        List<Hotel> hotels = getHotels();

        for (RecordCollectionOptions options : RecordCollectionOptions.values()) {
            String collectionName = getCollectionName("search", options);
            RedisJsonVectorStoreRecordCollection<Hotel> recordCollection = createCollection(optionsMap.get(options), collectionName);

            recordCollection.createCollectionAsync().block();
            assertEquals(true, recordCollection.collectionExistsAsync().block());

            recordCollection.upsertBatchAsync(hotels, null).block();
        }

        // Wait for data to be indexed
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static RedisJsonVectorStoreRecordCollection<Hotel> createCollection(@Nonnull RedisJsonVectorStoreRecordCollectionOptions<Hotel> options, @Nonnull String collectionName) {
        return new RedisJsonVectorStoreRecordCollection<>(new JedisPooled(redisContainer.getRedisURI()), collectionName, RedisJsonVectorStoreRecordCollectionOptions.<Hotel>builder()
                .withRecordClass(options.getRecordClass())
                .withVectorStoreRecordMapper(options.getVectorStoreRecordMapper())
                .withRecordDefinition(options.getRecordDefinition())
                .withPrefixCollectionName(options.isPrefixCollectionName())
                .build());
    }

    private static List<Hotel> getHotels() {
        return Arrays.asList(
                new Hotel("id_1", "Hotel 1", 1, "Hotel 1 description", Arrays.asList(0.5f, 3.2f, 7.1f, -4.0f, 2.8f, 10.0f, -1.3f, 5.5f),null, null, 4.0),
                new Hotel("id_2", "Hotel 2", 2, "Hotel 2 description", Arrays.asList(-2.0f, 8.1f, 0.9f, 5.4f, -3.3f, 2.2f, 9.9f, -4.5f),null, null, 4.0),
                new Hotel("id_3", "Hotel 3", 3, "Hotel 3 description", Arrays.asList(4.5f, -6.2f, 3.1f, 7.7f, -0.8f, 1.1f, -2.2f, 8.3f),null, null, 5.0),
                new Hotel("id_4", "Hotel 4", 4, "Hotel 4 description", Arrays.asList(7.0f, 1.2f, -5.3f, 2.5f, 6.6f, -7.8f, 3.9f, -0.1f),null, null, 4.0),
                new Hotel("id_5", "Hotel 5", 5, "Hotel 5 description", Arrays.asList(-3.5f, 4.4f, -1.2f, 9.9f, 5.7f, -6.1f, 7.8f, -2.0f),null, null, 4.0)
        );
    }

    /**
     * Search embeddings similar to the third hotel embeddings.
     * In order of similarity:
     * 1. Hotel 3
     * 2. Hotel 1
     * 3. Hotel 4
     */
    private static final List<Float> SEARCH_EMBEDDINGS = Arrays.asList(4.5f, -6.2f, 3.1f, 7.7f, -0.8f, 1.1f, -2.2f, 8.2f);

    private static String getCollectionName(String id, RecordCollectionOptions options) {
        return id + options.name();
    }

    @Order(1)
    @ParameterizedTest
    @EnumSource(RecordCollectionOptions.class)
    public void createCollectionAsync(RecordCollectionOptions options) {
        String collectionName = getCollectionName("createCollectionAsync", options);
        RedisJsonVectorStoreRecordCollection<Hotel> recordCollection = createCollection(optionsMap.get(options), collectionName);

        assertEquals(false, recordCollection.collectionExistsAsync().block());
        recordCollection.createCollectionAsync().block();
        assertEquals(true, recordCollection.collectionExistsAsync().block());
    }

    @Test
    public void deleteCollectionAsync() {
        String collectionName = getCollectionName("deleteCollectionAsync", RecordCollectionOptions.DEFAULT);
        RedisJsonVectorStoreRecordCollection<Hotel> recordCollection = createCollection(optionsMap.get(RecordCollectionOptions.DEFAULT), collectionName);

        assertEquals(false, recordCollection.collectionExistsAsync().block());
        recordCollection.createCollectionAsync().block();
        recordCollection.deleteCollectionAsync().block();
        assertEquals(false, recordCollection.collectionExistsAsync().block());
    }

    @ParameterizedTest
    @EnumSource(RecordCollectionOptions.class)
    public void upsertAndGetRecordAsync(RecordCollectionOptions options) {
        String collectionName = getCollectionName("upsertAndGetRecordAsync", options);
        RedisJsonVectorStoreRecordCollection<Hotel> recordCollection = createCollection(optionsMap.get(options), collectionName);

        List<Hotel> hotels = getHotels();
        for (Hotel hotel : hotels) {
            recordCollection.upsertAsync(hotel, null).block();
        }

        for (Hotel hotel : hotels) {
            Hotel retrievedHotel = recordCollection.getAsync(hotel.getId(), null).block();
            assertNotNull(retrievedHotel);
            assertEquals(hotel.getId(), retrievedHotel.getId());
        }
    }

    @ParameterizedTest
    @EnumSource(RecordCollectionOptions.class)
    public void getBatchAsync(RecordCollectionOptions options) {
        String collectionName = getCollectionName("getBatchAsync", options);
        RedisJsonVectorStoreRecordCollection<Hotel> recordCollection = createCollection(optionsMap.get(options), collectionName);

        List<Hotel> hotels = getHotels();
        for (Hotel hotel : hotels) {
            recordCollection.upsertAsync(hotel, null).block();
        }

        List<String> ids = new ArrayList<>();
        hotels.forEach(hotel -> ids.add(hotel.getId()));

        List<Hotel> retrievedHotels = recordCollection.getBatchAsync(ids, new GetRecordOptions(true)).block();

        assertNotNull(retrievedHotels);
        assertEquals(hotels.size(), retrievedHotels.size());
        for (int i = 0; i < hotels.size(); i++) {
            assertEquals(hotels.get(i).getId(), retrievedHotels.get(i).getId());
        }
    }

    @ParameterizedTest
    @EnumSource(RecordCollectionOptions.class)
    public void upsertBatchAsync(RecordCollectionOptions options) {
        String collectionName = getCollectionName("upsertBatchAsync", options);
        RedisJsonVectorStoreRecordCollection<Hotel> recordCollection = createCollection(optionsMap.get(options), collectionName);

        List<Hotel> hotels = getHotels();
        List<String> keys = recordCollection.upsertBatchAsync(hotels, null).block();
        assertNotNull(keys);

        List<Hotel> retrievedHotels = (List<Hotel>) recordCollection.getBatchAsync(keys, null).block();

        assertNotNull(retrievedHotels);
        assertEquals(hotels.size(), retrievedHotels.size());
        for (int i = 0; i < hotels.size(); i++) {
            assertEquals(hotels.get(i).getId(), retrievedHotels.get(i).getId());
        }
    }

    @ParameterizedTest
    @EnumSource(RecordCollectionOptions.class)
    public void deleteAsync(RecordCollectionOptions options) {
        String collectionName = getCollectionName("deleteAsync", options);
        RedisJsonVectorStoreRecordCollection<Hotel> recordCollection = createCollection(optionsMap.get(options), collectionName);

        List<Hotel> hotels = getHotels();
        recordCollection.upsertBatchAsync(hotels, null).block();

        for (Hotel hotel : hotels) {
            recordCollection.deleteAsync(hotel.getId(), null).block();
            Hotel retrievedHotel = recordCollection.getAsync(hotel.getId(), null).block();
            assertNull(retrievedHotel);
        }
    }

    @ParameterizedTest
    @EnumSource(RecordCollectionOptions.class)
    public void deleteBatchAsync(RecordCollectionOptions options) {
        String collectionName = getCollectionName("deleteBatchAsync", options);
        RedisJsonVectorStoreRecordCollection<Hotel> recordCollection = createCollection(optionsMap.get(options), collectionName);

        List<Hotel> hotels = getHotels();
        recordCollection.upsertBatchAsync(hotels, null).block();

        List<String> ids = new ArrayList<>();
        hotels.forEach(hotel -> ids.add(hotel.getId()));

        recordCollection.deleteBatchAsync(ids, null).block();

        for (String id : ids) {
            Hotel retrievedHotel = recordCollection.getAsync(id, null).block();
            assertNull(retrievedHotel);
        }
    }

    @ParameterizedTest
    @EnumSource(RecordCollectionOptions.class)
    public void getAsyncWithVectors(RecordCollectionOptions options) {
        String collectionName = getCollectionName("getAsyncWithVectors", options);
        RedisJsonVectorStoreRecordCollection<Hotel> recordCollection = createCollection(optionsMap.get(options), collectionName);

        List<Hotel> hotels = getHotels();
        recordCollection.upsertBatchAsync(hotels, null).block();

        for (Hotel hotel : hotels) {
            Hotel retrievedHotel = recordCollection.getAsync(hotel.getId(), new GetRecordOptions(true)).block();
            assertNotNull(retrievedHotel);
            assertNotNull(retrievedHotel.getEuclidean());
            assertEquals(hotel.getId(), retrievedHotel.getId());
            assertEquals(hotel.getDescription(), retrievedHotel.getDescription());
        }
    }

    @ParameterizedTest
    @EnumSource(RecordCollectionOptions.class)
    public void getBatchAsyncWithVectors(RecordCollectionOptions options) {
        String collectionName = getCollectionName("getBatchAsyncWithVectors", options);
        RedisJsonVectorStoreRecordCollection<Hotel> recordCollection = createCollection(optionsMap.get(options), collectionName);

        List<Hotel> hotels = getHotels();
        recordCollection.upsertBatchAsync(hotels, null).block();

        List<String> ids = new ArrayList<>();
        hotels.forEach(hotel -> ids.add(hotel.getId()));

        List<Hotel> retrievedHotels = recordCollection.getBatchAsync(ids, new GetRecordOptions(true)).block();

        assertNotNull(retrievedHotels);
        assertEquals(hotels.size(), retrievedHotels.size());
        for (int i = 0; i < hotels.size(); i++) {
            assertEquals(hotels.get(i).getId(), retrievedHotels.get(i).getId());
            assertEquals(hotels.get(i).getDescription(), retrievedHotels.get(i).getDescription());
            assertNotNull(retrievedHotels.get(i).getEuclidean());
        }
    }

    @ParameterizedTest
    @EnumSource(RecordCollectionOptions.class)
    public void getAsyncWithNoVectors(RecordCollectionOptions options) {
        String collectionName = getCollectionName("getAsyncWithNoVectors", options);
        RedisJsonVectorStoreRecordCollection<Hotel> recordCollection = createCollection(optionsMap.get(options), collectionName);

        List<Hotel> hotels = getHotels();
        recordCollection.upsertBatchAsync(hotels, null).block();

        GetRecordOptions getRecordOptions = GetRecordOptions.builder().includeVectors(false).build();
        for (Hotel hotel : hotels) {
            Hotel retrievedHotel = recordCollection.getAsync(hotel.getId(), getRecordOptions).block();
            assertNotNull(retrievedHotel);
            assertNull(retrievedHotel.getEuclidean());
            assertEquals(hotel.getId(), retrievedHotel.getId());
            assertEquals(hotel.getDescription(), retrievedHotel.getDescription());
        }
    }

    @ParameterizedTest
    @EnumSource(RecordCollectionOptions.class)
    public void getBatchAsyncWithNoVectors(RecordCollectionOptions options) {
        String collectionName = getCollectionName("getBatchAsyncWithNoVectors", options);
        RedisJsonVectorStoreRecordCollection<Hotel> recordCollection = createCollection(optionsMap.get(options), collectionName);

        List<Hotel> hotels = getHotels();
        recordCollection.upsertBatchAsync(hotels, null).block();

        GetRecordOptions getRecordOptions = GetRecordOptions.builder().includeVectors(false).build();
        List<String> ids = new ArrayList<>();
        hotels.forEach(hotel -> ids.add(hotel.getId()));

        List<Hotel> retrievedHotels = recordCollection.getBatchAsync(ids, getRecordOptions).block();

        assertNotNull(retrievedHotels);
        assertEquals(hotels.size(), retrievedHotels.size());
        for (int i = 0; i < hotels.size(); i++) {
            assertEquals(hotels.get(i).getId(), retrievedHotels.get(i).getId());
            assertEquals(hotels.get(i).getDescription(), retrievedHotels.get(i).getDescription());
            assertNull(retrievedHotels.get(i).getEuclidean());
        }
    }

    private static Stream<Arguments> provideSearchParameters() {
        return Stream.of(
                Arguments.of(RecordCollectionOptions.DEFAULT, "euclidean"),
                Arguments.of(RecordCollectionOptions.DEFAULT, "cosineDistance"),
                Arguments.of(RecordCollectionOptions.DEFAULT, "dotProduct"),
                Arguments.of(RecordCollectionOptions.WITH_CUSTOM_DEFINITION, "euclidean"),
                Arguments.of(RecordCollectionOptions.WITH_CUSTOM_DEFINITION, "cosineDistance"),
                Arguments.of(RecordCollectionOptions.WITH_CUSTOM_DEFINITION, "dotProduct")
        );
    }

    private final String indexingFailureMessage = "If you are running in a slow machine, data might not be indexed yet. Adjust setup delay if needed";

    @ParameterizedTest
    @MethodSource("provideSearchParameters")
    public void search(RecordCollectionOptions options, String embeddingName) {
        String collectionName = getCollectionName("search", options);
        RedisJsonVectorStoreRecordCollection<Hotel>  recordCollection = createCollection(optionsMap.get(options), collectionName);

        List<Hotel> hotels = getHotels();
        recordCollection.upsertBatchAsync(hotels, null).block();

        VectorSearchOptions searchOptions = VectorSearchOptions.builder()
                .withVectorFieldName(embeddingName)
                .build();

        // Embeddings similar to the third hotel
        List<VectorSearchResult<Hotel>> results = recordCollection.searchAsync(SEARCH_EMBEDDINGS, searchOptions).block().getResults();
        assertNotNull(results);
        assertEquals(VectorSearchOptions.DEFAULT_TOP, results.size(), indexingFailureMessage);
        // The third hotel should be the most similar
        assertEquals(hotels.get(2).getId(), results.get(0).getRecord().getId(), indexingFailureMessage);
        // Score should be different than zero
        assertNotEquals(0.0, results.get(0).getScore());
        assertNull(results.get(0).getRecord().getEuclidean());
    }

    @ParameterizedTest
    @MethodSource("provideSearchParameters")
    public void searchWithVectors(RecordCollectionOptions options, String embeddingName) {
        String collectionName = getCollectionName("search", options);
        RedisJsonVectorStoreRecordCollection<Hotel>  recordCollection = createCollection(optionsMap.get(options), collectionName);

        List<Hotel> hotels = getHotels();
        recordCollection.upsertBatchAsync(hotels, null).block();

        VectorSearchOptions searchOptions = VectorSearchOptions.builder()
                .withVectorFieldName(embeddingName)
                .withIncludeVectors(true)
                .build();

        // Embeddings similar to the third hotel
        List<VectorSearchResult<Hotel>> results = recordCollection.searchAsync(SEARCH_EMBEDDINGS, searchOptions).block().getResults();
        assertNotNull(results);
        assertEquals(VectorSearchOptions.DEFAULT_TOP, results.size(), indexingFailureMessage);
        // The third hotel should be the most similar
        assertEquals(hotels.get(2).getId(), results.get(0).getRecord().getId(), indexingFailureMessage);
        assertNotNull(results.get(0).getRecord().getEuclidean());
    }

    @ParameterizedTest
    @MethodSource("provideSearchParameters")
    public void searchWithOffSet(RecordCollectionOptions options, String embeddingName) {
        String collectionName = getCollectionName("search", options);
        RedisJsonVectorStoreRecordCollection<Hotel>  recordCollection = createCollection(optionsMap.get(options), collectionName);

        List<Hotel> hotels = getHotels();
        recordCollection.upsertBatchAsync(hotels, null).block();

        VectorSearchOptions searchOptions = VectorSearchOptions.builder()
                .withVectorFieldName(embeddingName)
                .withSkip(1)
                .withTop(4)
                .build();

        // Embeddings similar to the third hotel
        List<VectorSearchResult<Hotel>> results = recordCollection.searchAsync(SEARCH_EMBEDDINGS, searchOptions).block().getResults();
        assertNotNull(results);
        assertEquals(4, results.size(), indexingFailureMessage);
        // The first hotel should be the most similar
        assertEquals(hotels.get(0).getId(), results.get(0).getRecord().getId(), indexingFailureMessage);
    }

    @ParameterizedTest
    @MethodSource("provideSearchParameters")
    public void searchWithFilterEqualToFilter(RecordCollectionOptions recordCollectionOptions, String embeddingName) {
        String collectionName = getCollectionName("search", recordCollectionOptions);
        RedisJsonVectorStoreRecordCollection<Hotel>  recordCollection = createCollection(optionsMap.get(recordCollectionOptions), collectionName);

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
}
