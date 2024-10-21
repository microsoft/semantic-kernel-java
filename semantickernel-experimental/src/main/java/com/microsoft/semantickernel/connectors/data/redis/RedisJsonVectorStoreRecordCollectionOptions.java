// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.data.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.semantickernel.data.vectorstorage.VectorStoreRecordCollectionOptions;
import com.microsoft.semantickernel.data.vectorstorage.VectorStoreRecordMapper;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordDefinition;
import com.microsoft.semantickernel.exceptions.SKException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map.Entry;

/**
 * Options for a Redis vector store record collection.
 *
 * @param <Record> the record type
 */
public class RedisJsonVectorStoreRecordCollectionOptions<Record>
    implements VectorStoreRecordCollectionOptions<String, Record> {
    private final Class<Record> recordClass;
    @Nullable
    private final VectorStoreRecordMapper<Record, Entry<String, Object>> vectorStoreRecordMapper;
    @Nullable
    private final VectorStoreRecordDefinition recordDefinition;
    private final boolean prefixCollectionName;
    private final ObjectMapper objectMapper;

    private RedisJsonVectorStoreRecordCollectionOptions(
        @Nonnull Class<Record> recordClass,
        @Nullable VectorStoreRecordMapper<Record, Entry<String, Object>> vectorStoreRecordMapper,
        @Nullable VectorStoreRecordDefinition recordDefinition,
        boolean prefixCollectionName,
        @Nullable ObjectMapper objectMapper) {
        this.recordClass = recordClass;
        this.vectorStoreRecordMapper = vectorStoreRecordMapper;
        this.recordDefinition = recordDefinition;
        this.prefixCollectionName = prefixCollectionName;
        this.objectMapper = objectMapper;
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
     * Gets the key class.
     *
     * @return the key class
     */
    @Override
    public Class<String> getKeyClass() {
        return String.class;
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
     * Gets the record definition.
     *
     * @return the record definition
     */
    @Nullable
    public VectorStoreRecordDefinition getRecordDefinition() {
        return recordDefinition;
    }

    /**
     * Gets the vector store record mapper.
     *
     * @return the vector store record mapper
     */
    @Nullable
    public VectorStoreRecordMapper<Record, Entry<String, Object>> getVectorStoreRecordMapper() {
        return vectorStoreRecordMapper;
    }

    /**
     * Gets whether to prefix the collection name to the redis key.
     *
     * @return whether to prefix the collection name to the redis key
     */
    public boolean isPrefixCollectionName() {
        return prefixCollectionName;
    }

    /**
     * Gets the object mapper.
     *
     * @return the object mapper
     */
    ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    /**
     * Builder for {@link RedisJsonVectorStoreRecordCollectionOptions}.
     *
     * @param <Record> the record type
     */
    public static class Builder<Record> {
        @Nullable
        private VectorStoreRecordMapper<Record, Entry<String, Object>> vectorStoreRecordMapper;
        @Nullable
        private Class<Record> recordClass;
        @Nullable
        private VectorStoreRecordDefinition recordDefinition;
        private boolean prefixCollectionName = true;
        @Nullable
        private ObjectMapper objectMapper = new ObjectMapper();

        /**
         * Sets the record class.
         *
         * @param recordClass the record class
         * @return the builder
         */
        public Builder<Record> withRecordClass(Class<Record> recordClass) {
            this.recordClass = recordClass;
            return this;
        }

        /**
         * Sets the vector store record mapper.
         *
         * @param vectorStoreRecordMapper the vector store record mapper
         * @return the builder
         */
        public Builder<Record> withVectorStoreRecordMapper(
            VectorStoreRecordMapper<Record, Entry<String, Object>> vectorStoreRecordMapper) {
            this.vectorStoreRecordMapper = vectorStoreRecordMapper;
            return this;
        }

        /**
         * Sets the record definition.
         *
         * @param recordDefinition the record definition
         * @return the builder
         */
        public Builder<Record> withRecordDefinition(VectorStoreRecordDefinition recordDefinition) {
            this.recordDefinition = recordDefinition;
            return this;
        }

        /**
         * Sets whether the collection name should be prefixed to the key names before reading or writing to the Redis store. Default is true.
         * <p>
         * For a record to be indexed by a specific Redis index, the key name must be prefixed with the matching prefix configured on the Redis index.
         * You can either pass in keys that are already prefixed, or set this option to true to have the collection name prefixed to the key names automatically.
         * @param prefixCollectionName whether to prefix the collection name to the key
         * @return the builder
         */
        public Builder<Record> withPrefixCollectionName(boolean prefixCollectionName) {
            this.prefixCollectionName = prefixCollectionName;
            return this;
        }

        /**
         * Sets the object mapper to use for serialization and deserialization.
         *
         * @param objectMapper the object mapper
         * @return the builder
         */
        @SuppressFBWarnings("EI_EXPOSE_REP2")
        public Builder<Record> withObjectMapper(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
            return this;
        }

        /**
         * Builds the options.
         *
         * @return the options
         */
        public RedisJsonVectorStoreRecordCollectionOptions<Record> build() {
            if (recordClass == null) {
                throw new SKException("recordClass must be provided");
            }

            return new RedisJsonVectorStoreRecordCollectionOptions<>(
                recordClass,
                vectorStoreRecordMapper,
                recordDefinition,
                prefixCollectionName,
                objectMapper);
        }
    }
}
