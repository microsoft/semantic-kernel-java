package com.microsoft.semantickernel.tests.connectors.memory.redis;

import com.microsoft.semantickernel.connectors.memory.redis.RedisVectorStore;
import com.microsoft.semantickernel.connectors.memory.redis.RedisVectorStoreOptions;
import com.microsoft.semantickernel.data.recorddefinition.VectorStoreRecordDefinition;
import com.microsoft.semantickernel.tests.connectors.memory.Hotel;
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;
import redis.clients.jedis.JedisPooled;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
public class RedisVectorStoreTest {
    @Container
    private static final RedisContainer redisContainer = new RedisContainer("redis/redis-stack:latest");
    private static JedisPooled jedis;

    @BeforeAll
    public static void setUp() {
        jedis = new JedisPooled(redisContainer.getRedisURI());
    }

    @Test
    public void getCollectionNamesAsync() {
        RedisVectorStore<Hotel> vectorStore = new RedisVectorStore<>(jedis, new RedisVectorStoreOptions<>(Hotel.class, null));
        List<String> collectionNames = Arrays.asList("collection1", "collection2", "collection3");

        for (String collectionName : collectionNames) {
            vectorStore.getCollection(collectionName, VectorStoreRecordDefinition.fromRecordClass(Hotel.class)).createCollectionAsync().block();
        }

        List<String> retrievedCollectionNames = vectorStore.getCollectionNamesAsync().block();
        assertNotNull(retrievedCollectionNames);
        assertEquals(collectionNames.size(), retrievedCollectionNames.size());
        for (String collectionName : collectionNames) {
            assertTrue(retrievedCollectionNames.contains(collectionName));
        }
    }
}
