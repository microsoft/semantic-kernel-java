// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.data.redis;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RedisVectorStoreOptions {
    @Nullable
    private final RedisVectorStoreRecordCollectionFactory vectorStoreRecordCollectionFactory;

    /**
     * Creates a new instance of the Redis vector store options.
     *
     * @param vectorStoreRecordCollectionFactory The vector store record collection factory.
     */
    public RedisVectorStoreOptions(
        @Nullable RedisVectorStoreRecordCollectionFactory vectorStoreRecordCollectionFactory) {
        this.vectorStoreRecordCollectionFactory = vectorStoreRecordCollectionFactory;
    }

    /**
     * Creates a new instance of the Redis vector store options.
     */
    public RedisVectorStoreOptions() {
        this(null);
    }

    /**
     * Creates a new builder.
     *
     * @return the builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Gets the vector store record collection factory.
     *
     * @return the vector store record collection factory
     */
    @Nullable
    public RedisVectorStoreRecordCollectionFactory getVectorStoreRecordCollectionFactory() {
        return vectorStoreRecordCollectionFactory;
    }

    /**
     * Builder for Redis vector store options.
     */
    public static class Builder {
        @Nullable
        private RedisVectorStoreRecordCollectionFactory vectorStoreRecordCollectionFactory;

        /**
         * Sets the vector store record collection factory.
         *
         * @param vectorStoreRecordCollectionFactory The vector store record collection factory.
         * @return The updated builder instance.
         */
        public Builder withVectorStoreRecordCollectionFactory(
            RedisVectorStoreRecordCollectionFactory vectorStoreRecordCollectionFactory) {
            this.vectorStoreRecordCollectionFactory = vectorStoreRecordCollectionFactory;
            return this;
        }

        /**
         * Builds the options.
         *
         * @return The options.
         */
        public RedisVectorStoreOptions build() {
            return new RedisVectorStoreOptions(vectorStoreRecordCollectionFactory);
        }
    }
}
