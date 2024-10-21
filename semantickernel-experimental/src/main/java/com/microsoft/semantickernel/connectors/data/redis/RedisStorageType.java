// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.data.redis;

/**
 * The storage type for the Redis vector store.
 */
public enum RedisStorageType {
    /**
     * Redis storage with JSON module.
     */
    JSON,
    /**
     * Redis storage with hash set.
     */
    HASH_SET
}
