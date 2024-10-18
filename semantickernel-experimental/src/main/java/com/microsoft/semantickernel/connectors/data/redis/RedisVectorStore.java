// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.data.redis;

import com.microsoft.semantickernel.builders.SemanticKernelBuilder;
import com.microsoft.semantickernel.data.VectorStore;
import com.microsoft.semantickernel.data.VectorStoreRecordCollection;
import com.microsoft.semantickernel.data.recorddefinition.VectorStoreRecordDefinition;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import reactor.core.publisher.Mono;
import redis.clients.jedis.JedisPooled;

/**
 * Represents a Redis vector store.
 */
public class RedisVectorStore implements VectorStore {

    private final JedisPooled client;
    private final RedisVectorStoreOptions options;

    /**
     * Creates a new instance of the Redis vector store.
     *
     * @param client  The Redis client.
     * @param options The options for the vector store.
     */
    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public RedisVectorStore(@Nonnull JedisPooled client,
        @Nonnull RedisVectorStoreOptions options) {
        this.client = client;
        this.options = options;
    }

    @Override
    public <Key, Record> VectorStoreRecordCollection<Key, Record> getCollection(
        @Nonnull String collectionName,
        @Nonnull Class<Key> keyClass,
        @Nonnull Class<Record> recordClass,
        @Nullable VectorStoreRecordDefinition recordDefinition) {
        if (keyClass != String.class) {
            throw new IllegalArgumentException("Redis only supports string keys");
        }

        return (VectorStoreRecordCollection<Key, Record>) getCollection(
            collectionName,
            recordClass,
            recordDefinition);
    }

    /**
     * Gets a collection from the vector store.
     *
     * @param collectionName   The name of the collection.
     * @param recordClass      The class type of the record.
     * @param recordDefinition The record definition.
     * @param <Record>         The type of record in the collection.
     * @return The collection.
     */
    public <Record> RedisVectorStoreRecordCollection<Record> getCollection(
        @Nonnull String collectionName,
        @Nonnull Class<Record> recordClass,
        @Nullable VectorStoreRecordDefinition recordDefinition) {

        if (options.getVectorStoreRecordCollectionFactory() != null) {
            return options.getVectorStoreRecordCollectionFactory()
                .createVectorStoreRecordCollection(
                    client,
                    collectionName,
                    RedisVectorStoreRecordCollectionOptions.<Record>builder()
                        .withRecordClass(recordClass)
                        .withRecordDefinition(recordDefinition)
                        .build());
        }

        return new RedisVectorStoreRecordCollection<>(client, collectionName,
            RedisVectorStoreRecordCollectionOptions.<Record>builder()
                .withRecordClass(recordClass)
                .withRecordDefinition(recordDefinition)
                .build());
    }

    /**
     * Gets the names of all collections in the vector store.
     *
     * @return A list of collection names.
     */
    @Override
    public Mono<List<String>> getCollectionNamesAsync() {
        return Mono.fromCallable(() -> new ArrayList<>(client.ftList()));
    }

    /**
     * Create a builder for the Redis vector store.
     * @return A new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for the Redis vector store.
     */
    public static class Builder implements SemanticKernelBuilder<RedisVectorStore> {

        @Nullable
        private JedisPooled client;
        @Nullable
        private RedisVectorStoreOptions options;

        /**
         * Sets the Redis client.
         *
         * @param client the Redis client
         * @return the builder
         */
        @SuppressFBWarnings("EI_EXPOSE_REP2")
        public Builder withClient(JedisPooled client) {
            this.client = client;
            return this;
        }

        /**
         * Sets the options for the vector store.
         *
         * @param options the options for the vector store
         * @return the builder
         */
        public Builder withOptions(RedisVectorStoreOptions options) {
            this.options = options;
            return this;
        }

        @Override
        public RedisVectorStore build() {
            if (client == null) {
                throw new IllegalArgumentException("client is required");
            }

            if (options == null) {
                throw new IllegalArgumentException("options is required");
            }

            return new RedisVectorStore(client, options);
        }
    }
}
