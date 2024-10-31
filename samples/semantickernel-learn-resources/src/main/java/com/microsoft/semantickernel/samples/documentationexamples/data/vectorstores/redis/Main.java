// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.samples.documentationexamples.data.vectorstores.redis;

import com.microsoft.semantickernel.data.redis.RedisJsonVectorStoreRecordCollectionOptions;
import com.microsoft.semantickernel.data.redis.RedisStorageType;
import com.microsoft.semantickernel.data.redis.RedisVectorStore;
import com.microsoft.semantickernel.data.redis.RedisVectorStoreOptions;
import com.microsoft.semantickernel.samples.documentationexamples.data.index.Hotel;
import redis.clients.jedis.JedisPooled;

public class Main {
    public static void main(String[] args) {
        JedisPooled jedis = new JedisPooled("<your-redis-url>");

        // Build a Redis Vector Store
        // Available storage types are JSON and HASHSET. Default is JSON.
        var vectorStore = RedisVectorStore.builder()
            .withClient(jedis)
            .withOptions(
                RedisVectorStoreOptions.builder()
                    .withStorageType(RedisStorageType.HASH_SET).build())
            .build();

        var collection = vectorStore.getCollection("skhotels",
            RedisJsonVectorStoreRecordCollectionOptions.<Hotel>builder()
                .withRecordClass(Hotel.class)
                .build());

        collection = vectorStore.getCollection("skhotels",
            RedisJsonVectorStoreRecordCollectionOptions.<Hotel>builder()
                .withRecordClass(Hotel.class)
                .withPrefixCollectionName(false)
                .build());

        collection.getAsync("myprefix_h1", null).block();
    }
}
