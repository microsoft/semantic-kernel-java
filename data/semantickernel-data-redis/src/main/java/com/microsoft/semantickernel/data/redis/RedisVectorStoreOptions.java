// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.redis;

import com.microsoft.semantickernel.exceptions.SKException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Options for the Redis vector store.
 */
public class RedisVectorStoreOptions {
    @Nullable
    private final RedisVectorStoreRecordCollectionFactory vectorStoreRecordCollectionFactory;

    @Nonnull
    private final RedisStorageType storageType;

    /**
     * Creates a new instance of the Redis vector store options.
     * @param storageType The storage type.
     * @param vectorStoreRecordCollectionFactory The vector store record collection factory.
     */
    public RedisVectorStoreOptions(
        @Nonnull RedisStorageType storageType,
        @Nullable RedisVectorStoreRecordCollectionFactory vectorStoreRecordCollectionFactory) {
        this.storageType = storageType;
        this.vectorStoreRecordCollectionFactory = vectorStoreRecordCollectionFactory;
    }

    /**
     * Creates a new instance of the Redis vector store options.
     */
    public RedisVectorStoreOptions() {
        this(RedisStorageType.JSON, null);
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
     * Gets the storage type.
     *
     * @return the storage type
     */
    @Nonnull
    public RedisStorageType getStorageType() {
        return storageType;
    }

    /**
     * Builder for Redis vector store options.
     */
    public static class Builder {
        @Nullable
        private RedisVectorStoreRecordCollectionFactory vectorStoreRecordCollectionFactory;
        @Nullable
        private RedisStorageType storageType;

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
         * Sets the storage type.
         *
         * @param storageType The storage type.
         * @return The updated builder instance.
         */
        public Builder withStorageType(RedisStorageType storageType) {
            this.storageType = storageType;
            return this;
        }

        /**
         * Builds the options.
         *
         * @return The options.
         */
        public RedisVectorStoreOptions build() {
            if (storageType == null) {
                throw new SKException("storageType is required");
            }

            return new RedisVectorStoreOptions(storageType, vectorStoreRecordCollectionFactory);
        }
    }
}
