package com.microsoft.semantickernel.tests.connectors.memory.redis;

import com.microsoft.semantickernel.connectors.memory.redis.RedisVectorStoreRecordCollection;
import com.microsoft.semantickernel.connectors.memory.redis.RedisVectorStoreOptions;
import com.microsoft.semantickernel.data.recorddefinition.VectorStoreRecordDataField;
import com.microsoft.semantickernel.data.recorddefinition.VectorStoreRecordDefinition;
import com.microsoft.semantickernel.data.recorddefinition.VectorStoreRecordField;
import com.microsoft.semantickernel.data.recorddefinition.VectorStoreRecordKeyField;
import com.microsoft.semantickernel.data.recorddefinition.VectorStoreRecordVectorField;
import com.microsoft.semantickernel.data.recordoptions.GetRecordOptions;
import com.microsoft.semantickernel.tests.connectors.memory.Hotel;
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
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
public class RedisVectorStoreRecordCollectionTest {

    @Container private static final RedisContainer redisContainer = new RedisContainer("redis/redis-stack:latest");

    private static final Map<Options, RedisVectorStoreOptions<Hotel>> optionsMap = new HashMap<>();

    public enum Options {
        DEFAULT, WITH_CUSTOM_DEFINITION
    }

    @BeforeAll
    static void setup() {
        optionsMap.put(Options.DEFAULT, RedisVectorStoreOptions.<Hotel>builder()
                .withRecordClass(Hotel.class)
                .build());

        List<VectorStoreRecordField> fields = new ArrayList<>();
        fields.add(new VectorStoreRecordKeyField("id"));
        fields.add(new VectorStoreRecordDataField("name"));
        fields.add(new VectorStoreRecordDataField("code"));
        fields.add(new VectorStoreRecordDataField("description", true, "descriptionEmbedding"));
        fields.add(new VectorStoreRecordVectorField("descriptionEmbedding"));
        fields.add(new VectorStoreRecordDataField("rating"));
        VectorStoreRecordDefinition recordDefinition = VectorStoreRecordDefinition.create(fields);

        optionsMap.put(Options.WITH_CUSTOM_DEFINITION, RedisVectorStoreOptions.<Hotel>builder()
                .withRecordClass(Hotel.class)
                .withRecordDefinition(recordDefinition)
                .build());
    }

    private RedisVectorStoreRecordCollection<Hotel> buildRecordStore(@Nonnull RedisVectorStoreOptions<Hotel> options, @Nonnull String collectionName) {
        return new RedisVectorStoreRecordCollection<>(new JedisPooled(redisContainer.getRedisURI()), collectionName, RedisVectorStoreOptions.<Hotel>builder()
                .withRecordClass(options.getRecordClass())
                .withVectorStoreRecordMapper(options.getVectorStoreRecordMapper())
                .withRecordDefinition(options.getRecordDefinition())
                .withPrefixCollectionName(options.prefixCollectionName())
                .build());
    }

    @ParameterizedTest
    @EnumSource(Options.class)
    public void buildRecordStore(Options options) {
        assertNotNull(buildRecordStore(optionsMap.get(options), "buildTest"));
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

    @ParameterizedTest
    @EnumSource(Options.class)
    public void upsertAndGetRecordAsync(Options options) {
        RedisVectorStoreRecordCollection<Hotel> recordStore = buildRecordStore(optionsMap.get(options), "upsertAndGetRecordAsync");

        List<Hotel> hotels = getHotels();
        for (Hotel hotel : hotels) {
            recordStore.upsertAsync(hotel, null).block();
        }

        for (Hotel hotel : hotels) {
            Hotel retrievedHotel = recordStore.getAsync(hotel.getId(), null).block();
            assertNotNull(retrievedHotel);
            assertEquals(hotel.getId(), retrievedHotel.getId());
        }
    }

    @ParameterizedTest
    @EnumSource(Options.class)
    public void getBatchAsync(Options options) {
        RedisVectorStoreRecordCollection<Hotel> recordStore = buildRecordStore(optionsMap.get(options), "getBatchAsync");

        List<Hotel> hotels = getHotels();
        for (Hotel hotel : hotels) {
            recordStore.upsertAsync(hotel, null).block();
        }

        List<String> ids = new ArrayList<>();
        hotels.forEach(hotel -> ids.add(hotel.getId()));

        List<Hotel> retrievedHotels = recordStore.getBatchAsync(ids, null).block();

        assertNotNull(retrievedHotels);
        assertEquals(hotels.size(), retrievedHotels.size());
        for (int i = 0; i < hotels.size(); i++) {
            assertEquals(hotels.get(i).getId(), retrievedHotels.get(i).getId());
        }
    }

    @ParameterizedTest
    @EnumSource(Options.class)
    public void upsertBatchAsync(Options options) {
        RedisVectorStoreRecordCollection<Hotel> recordStore = buildRecordStore(optionsMap.get(options), "upsertBatchAsync");

        List<Hotel> hotels = getHotels();
        List<String> keys = recordStore.upsertBatchAsync(hotels, null).block();
        assertNotNull(keys);

        List<Hotel> retrievedHotels = (List<Hotel>) recordStore.getBatchAsync(keys, null).block();

        assertNotNull(retrievedHotels);
        assertEquals(hotels.size(), retrievedHotels.size());
        for (int i = 0; i < hotels.size(); i++) {
            assertEquals(hotels.get(i).getId(), retrievedHotels.get(i).getId());
        }
    }

    @ParameterizedTest
    @EnumSource(Options.class)
    public void deleteAsync(Options options) {
        RedisVectorStoreRecordCollection<Hotel> recordStore = buildRecordStore(optionsMap.get(options), "deleteAsync");

        List<Hotel> hotels = getHotels();
        recordStore.upsertBatchAsync(hotels, null).block();

        for (Hotel hotel : hotels) {
            recordStore.deleteAsync(hotel.getId(), null).block();
            Hotel retrievedHotel = recordStore.getAsync(hotel.getId(), null).block();
            assertNull(retrievedHotel);
        }
    }

    @ParameterizedTest
    @EnumSource(Options.class)
    public void deleteBatchAsync(Options options) {
        RedisVectorStoreRecordCollection<Hotel> recordStore = buildRecordStore(optionsMap.get(options), "deleteBatchAsync");

        List<Hotel> hotels = getHotels();
        recordStore.upsertBatchAsync(hotels, null).block();

        List<String> ids = new ArrayList<>();
        hotels.forEach(hotel -> ids.add(hotel.getId()));

        recordStore.deleteBatchAsync(ids, null).block();

        for (String id : ids) {
            Hotel retrievedHotel = recordStore.getAsync(id, null).block();
            assertNull(retrievedHotel);
        }
    }

    @Test
    public void getAsyncWithVectors() {
        RedisVectorStoreRecordCollection<Hotel> recordStore = buildRecordStore(optionsMap.get(Options.DEFAULT), "getAsyncWithVectors");

        List<Hotel> hotels = getHotels();
        recordStore.upsertBatchAsync(hotels, null).block();

        for (Hotel hotel : hotels) {
            Hotel retrievedHotel = recordStore.getAsync(hotel.getId(), null).block();
            assertNotNull(retrievedHotel);
            assertNotNull(retrievedHotel.getDescriptionEmbedding());
            assertEquals(hotel.getId(), retrievedHotel.getId());
            assertEquals(hotel.getDescription(), retrievedHotel.getDescription());
        }
    }

    @Test
    public void getBatchAsyncWithVectors() {
        RedisVectorStoreRecordCollection<Hotel> recordStore = buildRecordStore(optionsMap.get(Options.DEFAULT), "getBatchAsyncWithVectors");

        List<Hotel> hotels = getHotels();
        recordStore.upsertBatchAsync(hotels, null).block();

        List<String> ids = new ArrayList<>();
        hotels.forEach(hotel -> ids.add(hotel.getId()));

        List<Hotel> retrievedHotels = recordStore.getBatchAsync(ids, null).block();

        assertNotNull(retrievedHotels);
        assertEquals(hotels.size(), retrievedHotels.size());
        for (int i = 0; i < hotels.size(); i++) {
            assertEquals(hotels.get(i).getId(), retrievedHotels.get(i).getId());
            assertEquals(hotels.get(i).getDescription(), retrievedHotels.get(i).getDescription());
            assertNotNull(retrievedHotels.get(i).getDescriptionEmbedding());
        }
    }

    @Test
    public void getAsyncWithNoVectors() {
        RedisVectorStoreRecordCollection<Hotel> recordStore = buildRecordStore(optionsMap.get(Options.WITH_CUSTOM_DEFINITION), "getAsyncWithNoVectors");

        List<Hotel> hotels = getHotels();
        recordStore.upsertBatchAsync(hotels, null).block();

        GetRecordOptions getRecordOptions = GetRecordOptions.builder().includeVectors(false).build();
        for (Hotel hotel : hotels) {
            Hotel retrievedHotel = recordStore.getAsync(hotel.getId(), getRecordOptions).block();
            assertNotNull(retrievedHotel);
            assertNull(retrievedHotel.getDescriptionEmbedding());
            assertEquals(hotel.getId(), retrievedHotel.getId());
            assertEquals(hotel.getDescription(), retrievedHotel.getDescription());
        }
    }

    @Test
    public void getBatchAsyncWithNoVectors() {
        RedisVectorStoreRecordCollection<Hotel> recordStore = buildRecordStore(optionsMap.get(Options.WITH_CUSTOM_DEFINITION), "getBatchAsyncWithNoVectors");

        List<Hotel> hotels = getHotels();
        recordStore.upsertBatchAsync(hotels, null).block();

        GetRecordOptions getRecordOptions = GetRecordOptions.builder().includeVectors(false).build();
        List<String> ids = new ArrayList<>();
        hotels.forEach(hotel -> ids.add(hotel.getId()));

        List<Hotel> retrievedHotels = recordStore.getBatchAsync(ids, getRecordOptions).block();

        assertNotNull(retrievedHotels);
        assertEquals(hotels.size(), retrievedHotels.size());
        for (int i = 0; i < hotels.size(); i++) {
            assertEquals(hotels.get(i).getId(), retrievedHotels.get(i).getId());
            assertEquals(hotels.get(i).getDescription(), retrievedHotels.get(i).getDescription());
            assertNull(retrievedHotels.get(i).getDescriptionEmbedding());
        }
    }
}
