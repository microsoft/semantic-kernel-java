// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.data.redis;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RedisVectorStoreOptions<Record> {

    private final Class<Record> recordClass;
    @Nullable
    private final RedisVectorStoreRecordCollectionFactory<Record> vectorStoreRecordCollectionFactory;

    /**
     * Creates a new instance of the Redis vector store options.
     *
     * @param recordClass The record class.
     * @param vectorStoreRecordCollectionFactory The vector store record collection factory.
     */
    public RedisVectorStoreOptions(@Nonnull Class<Record> recordClass,
        @Nullable RedisVectorStoreRecordCollectionFactory<Record> vectorStoreRecordCollectionFactory) {
        this.recordClass = recordClass;
        this.vectorStoreRecordCollectionFactory = vectorStoreRecordCollectionFactory;
    }

    /**
     * Creates a new builder.
     *
     * @param <Record> the record type
     * @return the builder
     */
    public static <Record> Builder<Record> builder() {
        return new Builder<>();
    }

    /**
     * Gets the record class.
     *
     * @return the record class
     */
    public Class<Record> getRecordClass() {
        return recordClass;
    }

    /**
     * Gets the vector store record collection factory.
     *
     * @return the vector store record collection factory
     */
    @Nullable
    public RedisVectorStoreRecordCollectionFactory<Record> getVectorStoreRecordCollectionFactory() {
        return vectorStoreRecordCollectionFactory;
    }

    /**
     * Builder for Redis vector store options.
     *
     * @param <Record> the record type
     */
    public static class Builder<Record> {
        private Class<Record> recordClass;
        @Nullable
        private RedisVectorStoreRecordCollectionFactory<Record> vectorStoreRecordCollectionFactory;

        /**
         * Sets the record class.
         *
         * @param recordClass The record class.
         * @return The updated builder instance.
         */
        public Builder<Record> withRecordClass(Class<Record> recordClass) {
            this.recordClass = recordClass;
            return this;
        }

        /**
         * Sets the vector store record collection factory.
         *
         * @param vectorStoreRecordCollectionFactory The vector store record collection factory.
         * @return The updated builder instance.
         */
        public Builder<Record> withVectorStoreRecordCollectionFactory(
            RedisVectorStoreRecordCollectionFactory<Record> vectorStoreRecordCollectionFactory) {
            this.vectorStoreRecordCollectionFactory = vectorStoreRecordCollectionFactory;
            return this;
        }

        /**
         * Builds the options.
         *
         * @return The options.
         */
        public RedisVectorStoreOptions<Record> build() {
            if (recordClass == null) {
                throw new IllegalArgumentException("recordClass is required");
            }

            return new RedisVectorStoreOptions<>(recordClass, vectorStoreRecordCollectionFactory);
        }
    }
}
