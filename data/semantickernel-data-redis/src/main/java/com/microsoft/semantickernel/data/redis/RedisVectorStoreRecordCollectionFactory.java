// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.redis;

import com.microsoft.semantickernel.data.vectorstorage.VectorStoreRecordCollection;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordDefinition;
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
     * @param <Record>       The type of the records in the collection.
     * @return The collection.
     */
    <Record> VectorStoreRecordCollection<String, Record> createVectorStoreRecordCollection(
        JedisPooled client,
        String collectionName,
        Class<Record> recordClass,
        VectorStoreRecordDefinition recordDefinition);
}
