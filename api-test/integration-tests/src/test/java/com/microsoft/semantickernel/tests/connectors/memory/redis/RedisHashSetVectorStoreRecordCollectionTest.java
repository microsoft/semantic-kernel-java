package com.microsoft.semantickernel.tests.connectors.memory.redis;

import com.microsoft.semantickernel.connectors.data.redis.RedisHashSetVectorStoreRecordCollection;
import com.microsoft.semantickernel.connectors.data.redis.RedisHashSetVectorStoreRecordCollectionOptions;
import com.microsoft.semantickernel.data.record.definition.VectorStoreRecordDataField;
import com.microsoft.semantickernel.data.record.definition.VectorStoreRecordDefinition;
import com.microsoft.semantickernel.data.record.definition.VectorStoreRecordField;
import com.microsoft.semantickernel.data.record.definition.VectorStoreRecordKeyField;
import com.microsoft.semantickernel.data.record.definition.VectorStoreRecordVectorField;
import com.microsoft.semantickernel.data.record.options.GetRecordOptions;
import com.microsoft.semantickernel.tests.connectors.memory.Hotel;
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import redis.clients.jedis.JedisPooled;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RedisHashSetVectorStoreRecordCollectionTest {

    @Container private static final RedisContainer redisContainer = new RedisContainer("redis/redis-stack:latest");

    private static final Map<RecordCollectionOptions, RedisHashSetVectorStoreRecordCollectionOptions<Hotel>> optionsMap = new HashMap<>();

    public enum RecordCollectionOptions {
        DEFAULT, WITH_CUSTOM_DEFINITION
    }

    @BeforeAll
    static void setup() {
        optionsMap.put(RecordCollectionOptions.DEFAULT, RedisHashSetVectorStoreRecordCollectionOptions.<Hotel>builder()
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
                .withHasEmbedding(true)
                .withEmbeddingFieldName("descriptionEmbedding")
                .build());
        fields.add(VectorStoreRecordVectorField.builder()
                .withName("descriptionEmbedding")
                .withStorageName("summaryEmbedding")
                .withFieldType(List.class)
                .withDimensions(768)
                .build());
        fields.add(VectorStoreRecordDataField.builder()
                .withName("rating")
                .withFieldType(Double.class)
                .build());
        VectorStoreRecordDefinition recordDefinition = VectorStoreRecordDefinition.fromFields(fields);

        optionsMap.put(RecordCollectionOptions.WITH_CUSTOM_DEFINITION, RedisHashSetVectorStoreRecordCollectionOptions.<Hotel>builder()
                .withRecordClass(Hotel.class)
                .withRecordDefinition(recordDefinition)
                .build());
    }

    private RedisHashSetVectorStoreRecordCollection<Hotel> buildrecordCollection(@Nonnull RedisHashSetVectorStoreRecordCollectionOptions<Hotel> options, @Nonnull String collectionName) {
        return new RedisHashSetVectorStoreRecordCollection<>(new JedisPooled(redisContainer.getRedisURI()), collectionName, RedisHashSetVectorStoreRecordCollectionOptions.<Hotel>builder()
                .withRecordClass(options.getRecordClass())
                .withVectorStoreRecordMapper(options.getVectorStoreRecordMapper())
                .withRecordDefinition(options.getRecordDefinition())
                .withPrefixCollectionName(options.isPrefixCollectionName())
                .build());
    }

    private List<Hotel> getHotels() {
        return List.of(
                new Hotel("id_1", "Hotel 1", 1, "Hotel 1 description", Arrays.asList(1.0f, 2.0f, 3.0f), 4.0),
                new Hotel("id_2", "Hotel 2", 2, "Hotel 2 description", Arrays.asList(1.0f, 2.0f, 3.0f), 3.0),
                new Hotel("id_3", "Hotel 3", 3, "Hotel 3 description", Arrays.asList(1.0f, 2.0f, 3.0f), 5.0),
                new Hotel("id_4", "Hotel 4", 4, "Hotel 4 description", Arrays.asList(1.0f, 2.0f, 3.0f), 4.0),
                new Hotel("id_5", "Hotel 5", 5, "Hotel 5 description", Arrays.asList(1.0f, 2.0f, 3.0f), 5.0)
        );
    }

    @Order(1)
    @ParameterizedTest
    @EnumSource(RecordCollectionOptions.class)
    public void buildrecordCollection(RecordCollectionOptions options) {
        assertNotNull(buildrecordCollection(optionsMap.get(options), options.name()));
    }

    @Order(2)
    @ParameterizedTest
    @EnumSource(RecordCollectionOptions.class)
    public void createCollectionAsync(RecordCollectionOptions options) {
        RedisHashSetVectorStoreRecordCollection<Hotel> recordCollection = buildrecordCollection(optionsMap.get(options), options.name());

        assertEquals(false, recordCollection.collectionExistsAsync().block());
        recordCollection.createCollectionAsync().block();
        assertEquals(true, recordCollection.collectionExistsAsync().block());
    }

    @Test
    public void deleteCollectionAsync() {
        RedisHashSetVectorStoreRecordCollection<Hotel> recordCollection = buildrecordCollection(optionsMap.get(RecordCollectionOptions.DEFAULT), "deleteCollectionAsync");

        assertEquals(false, recordCollection.collectionExistsAsync().block());
        recordCollection.createCollectionAsync().block();
        recordCollection.deleteCollectionAsync().block();
        assertEquals(false, recordCollection.collectionExistsAsync().block());
    }

    @ParameterizedTest
    @EnumSource(RecordCollectionOptions.class)
    public void upsertAndGetRecordAsync(RecordCollectionOptions options) {
        RedisHashSetVectorStoreRecordCollection<Hotel> recordCollection = buildrecordCollection(optionsMap.get(options), options.name());

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
        RedisHashSetVectorStoreRecordCollection<Hotel> recordCollection = buildrecordCollection(optionsMap.get(options), options.name());

        List<Hotel> hotels = getHotels();
        for (Hotel hotel : hotels) {
            recordCollection.upsertAsync(hotel, null).block();
        }

        List<String> ids = new ArrayList<>();
        hotels.forEach(hotel -> ids.add(hotel.getId()));

        List<Hotel> retrievedHotels = recordCollection.getBatchAsync(ids, null).block();

        assertNotNull(retrievedHotels);
        assertEquals(hotels.size(), retrievedHotels.size());
        for (int i = 0; i < hotels.size(); i++) {
            assertEquals(hotels.get(i).getId(), retrievedHotels.get(i).getId());
        }
    }

    @ParameterizedTest
    @EnumSource(RecordCollectionOptions.class)
    public void upsertBatchAsync(RecordCollectionOptions options) {
        RedisHashSetVectorStoreRecordCollection<Hotel> recordCollection = buildrecordCollection(optionsMap.get(options), options.name());

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
        RedisHashSetVectorStoreRecordCollection<Hotel> recordCollection = buildrecordCollection(optionsMap.get(options), options.name());

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
        RedisHashSetVectorStoreRecordCollection<Hotel> recordCollection = buildrecordCollection(optionsMap.get(options), options.name());

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
        RedisHashSetVectorStoreRecordCollection<Hotel> recordCollection = buildrecordCollection(optionsMap.get(options), options.name());

        List<Hotel> hotels = getHotels();
        recordCollection.upsertBatchAsync(hotels, null).block();

        for (Hotel hotel : hotels) {
            Hotel retrievedHotel = recordCollection.getAsync(hotel.getId(), null).block();
            assertNotNull(retrievedHotel);
            assertNotNull(retrievedHotel.getDescriptionEmbedding());
            assertEquals(hotel.getId(), retrievedHotel.getId());
            assertEquals(hotel.getDescription(), retrievedHotel.getDescription());
        }
    }

    @ParameterizedTest
    @EnumSource(RecordCollectionOptions.class)
    public void getBatchAsyncWithVectors(RecordCollectionOptions options) {
        RedisHashSetVectorStoreRecordCollection<Hotel> recordCollection = buildrecordCollection(optionsMap.get(options), options.name());

        List<Hotel> hotels = getHotels();
        recordCollection.upsertBatchAsync(hotels, null).block();

        List<String> ids = new ArrayList<>();
        hotels.forEach(hotel -> ids.add(hotel.getId()));

        List<Hotel> retrievedHotels = recordCollection.getBatchAsync(ids, null).block();

        assertNotNull(retrievedHotels);
        assertEquals(hotels.size(), retrievedHotels.size());
        for (int i = 0; i < hotels.size(); i++) {
            assertEquals(hotels.get(i).getId(), retrievedHotels.get(i).getId());
            assertEquals(hotels.get(i).getDescription(), retrievedHotels.get(i).getDescription());
            assertNotNull(retrievedHotels.get(i).getDescriptionEmbedding());
        }
    }

    @ParameterizedTest
    @EnumSource(RecordCollectionOptions.class)
    public void getAsyncWithNoVectors(RecordCollectionOptions options) {
        RedisHashSetVectorStoreRecordCollection<Hotel> recordCollection = buildrecordCollection(optionsMap.get(options), options.name());

        List<Hotel> hotels = getHotels();
        recordCollection.upsertBatchAsync(hotels, null).block();

        GetRecordOptions getRecordOptions = GetRecordOptions.builder().includeVectors(false).build();
        for (Hotel hotel : hotels) {
            Hotel retrievedHotel = recordCollection.getAsync(hotel.getId(), getRecordOptions).block();
            assertNotNull(retrievedHotel);
            assertNull(retrievedHotel.getDescriptionEmbedding());
            assertEquals(hotel.getId(), retrievedHotel.getId());
            assertEquals(hotel.getDescription(), retrievedHotel.getDescription());
        }
    }

    @ParameterizedTest
    @EnumSource(RecordCollectionOptions.class)
    public void getBatchAsyncWithNoVectors(RecordCollectionOptions options) {
        RedisHashSetVectorStoreRecordCollection<Hotel> recordCollection = buildrecordCollection(optionsMap.get(options), options.name());

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
            assertNull(retrievedHotels.get(i).getDescriptionEmbedding());
        }
    }
}
