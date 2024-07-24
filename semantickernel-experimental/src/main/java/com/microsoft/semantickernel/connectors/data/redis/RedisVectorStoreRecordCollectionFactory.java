// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.data.redis;

import redis.clients.jedis.JedisPooled;

/**
 * Factory for creating Redis vector store record collections.
 *
 */
public interface RedisVectorStoreRecordCollectionFactory {
    /**
     * Creates a new vector store record collection.
     *
     * @param client The Redis client.
     * @param collectionName The name of the collection.
     * @param options The options for the collection.
     * @return The collection.
     */
    <Record> RedisVectorStoreRecordCollection<Record> createVectorStoreRecordCollection(
        JedisPooled client,
        String collectionName,
        RedisVectorStoreRecordCollectionOptions<Record> options);
}
