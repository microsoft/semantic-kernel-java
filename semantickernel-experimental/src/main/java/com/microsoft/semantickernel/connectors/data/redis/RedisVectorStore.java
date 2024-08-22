// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.data.redis;

import com.microsoft.semantickernel.builders.SemanticKernelBuilder;
import com.microsoft.semantickernel.data.VectorStore;
import com.microsoft.semantickernel.data.VectorStoreRecordCollection;
import com.microsoft.semantickernel.data.VectorStoreRecordCollectionOptions;
import com.microsoft.semantickernel.data.recorddefinition.VectorStoreRecordDefinition;
import com.microsoft.semantickernel.exceptions.SKException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import reactor.core.publisher.Mono;
import redis.clients.jedis.JedisPooled;

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

    /**
     * Gets a collection from the vector store.
     *
     * @param collectionName   The name of the collection.
     * @return The collection.
     */
    public <Key, Record> VectorStoreRecordCollection<Key, Record> getCollection(
        @Nonnull String collectionName,
        @Nonnull VectorStoreRecordCollectionOptions<Key, Record> options) {
        if (!options.getKeyClass().equals(String.class)) {
            throw new SKException("Redis only supports string keys");
        }
        if (options.getRecordClass() == null) {
            throw new SKException("Record class is required");
        }

        if (this.options.getVectorStoreRecordCollectionFactory() != null) {
            return (VectorStoreRecordCollection<Key, Record>) this.options
                .getVectorStoreRecordCollectionFactory()
                .createVectorStoreRecordCollection(
                    client,
                    collectionName,
                    options.getRecordClass(),
                    options.getRecordDefinition());
        }

        if (this.options.getStorageType() == RedisStorageType.JSON) {
            return (VectorStoreRecordCollection<Key, Record>) new RedisJsonVectorStoreRecordCollection<>(
                client,
                collectionName,
                (RedisJsonVectorStoreRecordCollectionOptions<Record>) options);
        } else {
            return (VectorStoreRecordCollection<Key, Record>) new RedisHashSetVectorStoreRecordCollection<>(
                client,
                collectionName,
                (RedisHashSetVectorStoreRecordCollectionOptions<Record>) options);
        }
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
     * Builder for the Redis vector store.
     */
    public static Builder builder() {
        return new Builder();
    }

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
