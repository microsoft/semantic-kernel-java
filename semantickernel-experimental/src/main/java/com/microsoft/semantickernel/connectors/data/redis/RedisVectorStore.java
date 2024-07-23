// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.data.redis;

import com.microsoft.semantickernel.builders.SemanticKernelBuilder;
import com.microsoft.semantickernel.data.VectorStore;
import com.microsoft.semantickernel.data.recorddefinition.VectorStoreRecordDefinition;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import reactor.core.publisher.Mono;
import redis.clients.jedis.JedisPooled;

public class RedisVectorStore<Record>
    implements VectorStore<String, Record, RedisVectorStoreRecordCollection<Record>> {

    private final JedisPooled client;
    private final RedisVectorStoreOptions<Record> options;

    /**
     * Creates a new instance of the Redis vector store.
     *
     * @param client  The Redis client.
     * @param options The options for the vector store.
     */
    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public RedisVectorStore(@Nonnull JedisPooled client,
        @Nonnull RedisVectorStoreOptions<Record> options) {
        this.client = client;
        this.options = options;
    }

    /**
     * Gets a collection from the vector store.
     *
     * @param collectionName   The name of the collection.
     * @param recordDefinition The record definition.
     * @return The collection.
     */
    @Override
    public RedisVectorStoreRecordCollection<Record> getCollection(@Nonnull String collectionName,
        VectorStoreRecordDefinition recordDefinition) {
        if (options.getVectorStoreRecordCollectionFactory() != null) {
            return options.getVectorStoreRecordCollectionFactory()
                .createVectorStoreRecordCollection(
                    client,
                    collectionName,
                    RedisVectorStoreRecordCollectionOptions.<Record>builder()
                        .withRecordClass(options.getRecordClass())
                        .withRecordDefinition(recordDefinition)
                        .build());
        }

        return new RedisVectorStoreRecordCollection<>(client, collectionName,
            RedisVectorStoreRecordCollectionOptions.<Record>builder()
                .withRecordClass(options.getRecordClass())
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
     * Builder for the Redis vector store.
     *
     * @param <Record> The record type.
     */
    public static <Record> Builder<Record> builder() {
        return new Builder<>();
    }

    public static class Builder<Record> implements SemanticKernelBuilder<RedisVectorStore<Record>> {

        @Nullable
        private JedisPooled client;
        @Nullable
        private RedisVectorStoreOptions<Record> options;

        /**
         * Sets the Redis client.
         *
         * @param client the Redis client
         * @return the builder
         */
        @SuppressFBWarnings("EI_EXPOSE_REP2")
        public Builder<Record> withClient(JedisPooled client) {
            this.client = client;
            return this;
        }

        /**
         * Sets the options for the vector store.
         *
         * @param options the options for the vector store
         * @return the builder
         */
        public Builder<Record> withOptions(RedisVectorStoreOptions<Record> options) {
            this.options = options;
            return this;
        }

        @Override
        public RedisVectorStore<Record> build() {
            if (client == null) {
                throw new IllegalArgumentException("client is required");
            }

            if (options == null) {
                throw new IllegalArgumentException("options is required");
            }

            return new RedisVectorStore<>(client, options);
        }
    }
}
