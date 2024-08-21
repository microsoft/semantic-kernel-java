// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.data.redis;

import com.microsoft.semantickernel.data.VectorStoreRecordCollection;
import com.microsoft.semantickernel.data.recorddefinition.VectorStoreRecordDefinition;
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
     * @param recordClass    The class type of the record.
     * @param recordDefinition The record definition.
     * @return The collection.
     */
    <Record> VectorStoreRecordCollection<String, Record> createVectorStoreRecordCollection(
        JedisPooled client,
        String collectionName,
        Class<Record> recordClass,
        VectorStoreRecordDefinition recordDefinition);
}
