package com.microsoft.semantickernel.connectors.memory.redis;

import com.microsoft.semantickernel.memory.VectorStoreRecordMapper;
import com.microsoft.semantickernel.memory.recorddefinition.VectorStoreRecordDefinition;

import java.util.Map.Entry;

public class RedisVectorStoreOptions<Record> {

    private final String defaultCollectionName;
    private final Class<Record> recordClass;
    private final VectorStoreRecordMapper<Record, Entry<String, Object>> vectorStoreRecordMapper;
    private final VectorStoreRecordDefinition recordDefinition;
    private final boolean prefixCollectionName;

    private RedisVectorStoreOptions(String defaultCollectionName,
                                    Class<Record> recordClass,
                                    VectorStoreRecordMapper<Record, Entry<String, Object>> vectorStoreRecordMapper,
                                    VectorStoreRecordDefinition recordDefinition,
                                    boolean prefixCollectionName) {
        this.defaultCollectionName = defaultCollectionName;
        this.recordClass = recordClass;
        this.vectorStoreRecordMapper = vectorStoreRecordMapper;
        this.recordDefinition = recordDefinition;
        this.prefixCollectionName = prefixCollectionName;
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
     * Gets the default collection name.
     *
     * @return the default collection name
     */
    public String getDefaultCollectionName() {
        return defaultCollectionName;
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
    public VectorStoreRecordDefinition getRecordDefinition() {
        return recordDefinition;
    }

    /**
     * Gets the vector store record mapper.
     *
     * @return the vector store record mapper
     */
    public VectorStoreRecordMapper<Record, Entry<String, Object>> getVectorStoreRecordMapper() {
        return vectorStoreRecordMapper;
    }

    /**
     * Gets whether to prefix the collection name to the redis key.
     *
     * @return whether to prefix the collection name to the redis key
     */
    public boolean prefixCollectionName() {
        return prefixCollectionName;
    }

    /**
     * Builder for {@link RedisVectorStoreOptions}.
     *
     * @param <Record> the record type
     */
    public static class Builder<Record> {
        private String defaultCollectionName;
        private VectorStoreRecordMapper<Record, Entry<String, Object>> vectorStoreRecordMapper;
        private Class<Record> recordClass;
        private VectorStoreRecordDefinition recordDefinition;
        private boolean prefixCollectionName = false;

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
         * Sets the default collection name.
         *
         * @param defaultCollectionName the default collection name
         * @return the builder
         */
        public Builder<Record> withDefaultCollectionName(String defaultCollectionName) {
            this.defaultCollectionName = defaultCollectionName;
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
         * Sets whether to prefix the collection name to the redis key.
         *
         * @param prefixCollectionName whether to prefix the collection name to the redis key
         * @return the builder
         */
        public Builder<Record> withPrefixCollectionName(boolean prefixCollectionName) {
            this.prefixCollectionName = prefixCollectionName;
            return this;
        }

        /**
         * Builds the options.
         *
         * @return the options
         */
        public RedisVectorStoreOptions<Record> build() {
            if (recordClass == null) {
                throw new IllegalArgumentException("recordClass must be provided");
            }

            return new RedisVectorStoreOptions<>(defaultCollectionName,
                    recordClass,
                    vectorStoreRecordMapper,
                    recordDefinition,
                    prefixCollectionName);
        }
    }
}
