package com.microsoft.semantickernel.tests.connectors.memory.redis;

import com.microsoft.semantickernel.connectors.data.redis.RedisHashSetVectorStoreRecordCollectionOptions;
import com.microsoft.semantickernel.connectors.data.redis.RedisJsonVectorStoreRecordCollectionOptions;
import com.microsoft.semantickernel.connectors.data.redis.RedisStorageType;
import com.microsoft.semantickernel.connectors.data.redis.RedisVectorStore;
import com.microsoft.semantickernel.connectors.data.redis.RedisVectorStoreOptions;
import com.microsoft.semantickernel.data.vectorstorage.VectorStoreRecordCollectionOptions;
import com.microsoft.semantickernel.tests.connectors.memory.Hotel;
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import redis.clients.jedis.JedisPooled;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
public class RedisVectorStoreTest {
    @Container
    private static final RedisContainer redisJsonContainer = new RedisContainer("redis/redis-stack:latest");
    @Container
    private static final RedisContainer redisHashSetContainer = new RedisContainer("redis/redis-stack:latest");

    public static JedisPooled buildClient(RedisStorageType storageType) {
        if (storageType == RedisStorageType.JSON) {
            return new JedisPooled(redisJsonContainer.getRedisURI());
        } else {
            return new JedisPooled(redisHashSetContainer.getRedisURI());
        }
    }

    private static VectorStoreRecordCollectionOptions<String, Hotel> getRecordCollectionOptions(RedisStorageType storageType) {
        if (storageType == RedisStorageType.JSON) {
            return RedisJsonVectorStoreRecordCollectionOptions.<Hotel>builder()
                    .withRecordClass(Hotel.class)
                    .build();
        } else {
            return RedisHashSetVectorStoreRecordCollectionOptions.<Hotel>builder()
                    .withRecordClass(Hotel.class)
                    .build();
        }
    }

    @ParameterizedTest
    @EnumSource(RedisStorageType.class)
    public void getCollectionNamesAsync(RedisStorageType storageType) {
        RedisVectorStore vectorStore = new RedisVectorStore(buildClient(storageType), RedisVectorStoreOptions.builder()
                .withStorageType(storageType)
                .build());

        List<String> collectionNames = Arrays.asList("collection1", "collection2", "collection3");

        for (String collectionName : collectionNames) {
            vectorStore.getCollection(collectionName, getRecordCollectionOptions(storageType)).createCollectionAsync().block();
        }

        List<String> retrievedCollectionNames = vectorStore.getCollectionNamesAsync().block();
        assertNotNull(retrievedCollectionNames);
        assertEquals(collectionNames.size(), retrievedCollectionNames.size());
        for (String collectionName : collectionNames) {
            assertTrue(retrievedCollectionNames.contains(collectionName));
        }
    }
}
