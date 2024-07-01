package com.microsoft.semantickernel.tests.connectors.memory.redis;

import com.microsoft.semantickernel.connectors.memory.redis.RedisVectorRecordStore;
import com.microsoft.semantickernel.connectors.memory.redis.RedisVectorStoreOptions;
import com.microsoft.semantickernel.tests.connectors.memory.Hotel;
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import redis.clients.jedis.JedisPooled;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@Testcontainers
public class RedisVectorRecordStoreTest {

    @Container private static final RedisContainer redisContainer = new RedisContainer("redis/redis-stack:latest");

    private static JedisPooled client;
    @BeforeAll
    static void setup() {
        String uri = redisContainer.getRedisURI();
        client = new JedisPooled(uri);
    }

    private RedisVectorRecordStore<Hotel> buildRecordStore(String collectionName, RedisVectorStoreOptions<Hotel> options) {
        if (options == null) {
            options = RedisVectorStoreOptions.<Hotel>builder()
                    .withRecordClass(Hotel.class)
                    .withDefaultCollectionName(collectionName)
                    .build();
        }
        return new RedisVectorRecordStore<>(client, options);
    }

    @Test
    public void buildRecordStore() {
        assertNotNull(buildRecordStore("buildTest", null));
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

    @Test
    public void upsertAndGetRecordAsync() {
        RedisVectorRecordStore<Hotel> recordStore = buildRecordStore("upsertAndGetRecordAsync", null);

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

    @Test
    public void upsertBatchAsync() {
        RedisVectorRecordStore<Hotel> recordStore = buildRecordStore("upsertBatchAsync", null);

        List<Hotel> hotels = getHotels();
        recordStore.upsertBatchAsync(hotels, null).block();

        for (Hotel hotel : hotels) {
            Hotel retrievedHotel = recordStore.getAsync(hotel.getId(), null).block();
            assertNotNull(retrievedHotel);
            assertEquals(hotel.getId(), retrievedHotel.getId());
        }
    }

    @Test
    public void getBatchAsync() {
        RedisVectorRecordStore<Hotel> recordStore = buildRecordStore("getBatchAsync", null);

        List<Hotel> hotels = getHotels();
        recordStore.upsertBatchAsync(hotels, null).block();

        List<String> ids = new ArrayList<>();
        hotels.forEach(hotel -> ids.add(hotel.getId()));

        List<Hotel> retrievedHotels = (List<Hotel>) recordStore.getBatchAsync(ids, null).block();

        assertNotNull(retrievedHotels);
        assertEquals(hotels.size(), retrievedHotels.size());
        for (int i = 0; i < hotels.size(); i++) {
            assertEquals(hotels.get(i).getId(), retrievedHotels.get(i).getId());
        }
    }

    @Test
    public void deleteAsync() {
        RedisVectorRecordStore<Hotel> recordStore = buildRecordStore("removeAsync", null);

        List<Hotel> hotels = getHotels();
        recordStore.upsertBatchAsync(hotels, null).block();

        for (Hotel hotel : hotels) {
            recordStore.deleteAsync(hotel.getId(), null).block();
            Hotel retrievedHotel = recordStore.getAsync(hotel.getId(), null).block();
            assertNull(retrievedHotel);
        }
    }

    @Test
    public void deleteBatchAsync() {
        RedisVectorRecordStore<Hotel> recordStore = buildRecordStore("deleteBatchAsync", null);

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
