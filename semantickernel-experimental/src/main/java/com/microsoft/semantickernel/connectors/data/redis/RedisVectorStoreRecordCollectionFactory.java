// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.data.redis;

import com.azure.search.documents.indexes.SearchIndexAsyncClient;
import redis.clients.jedis.JedisPooled;

/**
 * Factory for creating Redis vector store record collections.
 */
public interface RedisVectorStoreRecordCollectionFactory {

    /**
     * Creates a new vector store record collection.
     *
     * @param client         The Redis client.
     * @param collectionName The name of the collection.
     * @param options        The options for the collection.
     * @param <Record>       The type of the records in the collection.
     * @return The collection.
     */
    <Record> RedisVectorStoreRecordCollection<Record> createVectorStoreRecordCollection(
        JedisPooled client,
        String collectionName,
        RedisVectorStoreRecordCollectionOptions<Record> options);
}
