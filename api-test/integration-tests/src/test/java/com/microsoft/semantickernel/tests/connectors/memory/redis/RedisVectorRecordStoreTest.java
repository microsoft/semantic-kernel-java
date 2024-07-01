package com.microsoft.semantickernel.tests.connectors.memory.redis;

import com.microsoft.semantickernel.connectors.memory.redis.RedisVectorRecordStore;
import com.microsoft.semantickernel.connectors.memory.redis.RedisVectorStoreOptions;
import com.microsoft.semantickernel.memory.recorddefinition.VectorStoreRecordDataField;
import com.microsoft.semantickernel.memory.recorddefinition.VectorStoreRecordDefinition;
import com.microsoft.semantickernel.memory.recorddefinition.VectorStoreRecordField;
import com.microsoft.semantickernel.memory.recorddefinition.VectorStoreRecordKeyField;
import com.microsoft.semantickernel.memory.recorddefinition.VectorStoreRecordVectorField;
import com.microsoft.semantickernel.tests.connectors.memory.Hotel;
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import redis.clients.jedis.JedisPooled;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@Testcontainers
public class RedisVectorRecordStoreTest {

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

    private RedisVectorRecordStore<Hotel> buildRecordStore(@Nonnull RedisVectorStoreOptions<Hotel> options, @Nonnull String collectionName) {
        return new RedisVectorRecordStore<>(new JedisPooled(redisContainer.getRedisURI()), RedisVectorStoreOptions.<Hotel>builder()
                .withDefaultCollectionName(collectionName)
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
                new Hotel("id_1", "Hotel 1", 1, "Hotel 1 description", List.of(1.0f, 2.0f, 3.0f), 4.0),
                new Hotel("id_2", "Hotel 2", 2, "Hotel 2 description", List.of(2.0f, 3.0f, 4.0f), 5.0),
                new Hotel("id_3", "Hotel 3", 3, "Hotel 3 description", List.of(3.0f, 4.0f, 5.0f), 3.0),
                new Hotel("id_4", "Hotel 4", 4, "Hotel 4 description", List.of(4.0f, 5.0f, 6.0f), 2.0),
                new Hotel("id_5", "Hotel 5", 5, "Hotel 5 description", List.of(5.0f, 6.0f, 7.0f), 3.5)
        );
    }

    @ParameterizedTest
    @EnumSource(Options.class)
    public void upsertAndGetRecordAsync(Options options) {
        RedisVectorRecordStore<Hotel> recordStore = buildRecordStore(optionsMap.get(options), "upsertAndGetRecordAsync");

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
        RedisVectorRecordStore<Hotel> recordStore = buildRecordStore(optionsMap.get(options), "getBatchAsync" + options.toString());

        List<Hotel> hotels = getHotels();
        for (Hotel hotel : hotels) {
            recordStore.upsertAsync(hotel, null).block();
        }

        List<String> ids = new ArrayList<>();
        hotels.forEach(hotel -> ids.add(hotel.getId()));

        List<Hotel> retrievedHotels = (List<Hotel>) recordStore.getBatchAsync(ids, null).block();

        assertNotNull(retrievedHotels);
        assertEquals(hotels.size(), retrievedHotels.size());
        for (int i = 0; i < hotels.size(); i++) {
            assertEquals(hotels.get(i).getId(), retrievedHotels.get(i).getId());
        }
    }

    @ParameterizedTest
    @EnumSource(Options.class)
    public void upsertBatchAsync(Options options) {
        RedisVectorRecordStore<Hotel> recordStore = buildRecordStore(optionsMap.get(options), "upsertBatchAsync");

        List<Hotel> hotels = getHotels();
        Collection<String> keys = recordStore.upsertBatchAsync(hotels, null).block();

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
        RedisVectorRecordStore<Hotel> recordStore = buildRecordStore(optionsMap.get(options), "deleteAsync");

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
        RedisVectorRecordStore<Hotel> recordStore = buildRecordStore(optionsMap.get(options), "deleteBatchAsync");

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
}
