// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.memory.jdbc;

import com.microsoft.semantickernel.memory.recorddefinition.VectorStoreRecordDefinition;

import java.util.function.BiFunction;

public class JDBCVectorStoreOptions<Record> {
    private final String storageTableName;
    private final String defaultCollectionName;
    private final Class<Record> recordClass;
    private final JDBCVectorStoreRecordMapper<Record> vectorStoreRecordMapper;
    private final JDBCVectorStoreQueryHandler<Record> vectorStoreQueryHandler;
    private final VectorStoreRecordDefinition recordDefinition;
    private final BiFunction<String, String, String> sanitizeKeyFunction;

    public JDBCVectorStoreOptions(
        Class<Record> recordClass,
        String storageTableName,
        String defaultCollectionName,
        JDBCVectorStoreRecordMapper<Record> vectorStoreRecordMapper,
        JDBCVectorStoreQueryHandler<Record> vectorStoreQueryHandler,
        VectorStoreRecordDefinition recordDefinition,
        BiFunction<String, String, String> sanitizeKeyFunction) {
        this.recordClass = recordClass;
        this.storageTableName = storageTableName;
        this.defaultCollectionName = defaultCollectionName;
        this.vectorStoreRecordMapper = vectorStoreRecordMapper;
        this.vectorStoreQueryHandler = vectorStoreQueryHandler;
        this.recordDefinition = recordDefinition;
        this.sanitizeKeyFunction = sanitizeKeyFunction;
    }

    /**
     * Creates a new builder.
     * @param <Record> the record type
     * @return the builder
     */
    public static <Record> Builder<Record> builder() {
        return new Builder<>();
    }

    /**
     * Gets the record class.
     * @return the record class
     */
    public Class<Record> getRecordClass() {
        return recordClass;
    }

    /**
     * Gets the storage table name. This is the name of the table in the database where the records are stored.
     * @return the storage table name
     */
    public String getStorageTableName() {
        return storageTableName;
    }

    /**
     * Gets the default collection name. This is the name of the collection to use if none is provided.
     * @return the default collection name
     */
    public String getDefaultCollectionName() {
        return defaultCollectionName;
    }

    /**
     * Gets the vector store record mapper.
     * @return the vector store record mapper
     */
    public JDBCVectorStoreRecordMapper<Record> getVectorStoreRecordMapper() {
        return vectorStoreRecordMapper;
    }

    /**
     * Gets the vector store query handler.
     * @return the vector store query handler
     */
    public JDBCVectorStoreQueryHandler<Record> getVectorStoreQueryHandler() {
        return vectorStoreQueryHandler;
    }

    /**
     * Gets the record definition.
     * @return the record definition
     */
    public VectorStoreRecordDefinition getRecordDefinition() {
        return recordDefinition;
    }

    /**
     * Gets the function to sanitize the key.
     * @return the function to sanitize the key
     */
    public BiFunction<String, String, String> getSanitizeKeyFunction() {
        return sanitizeKeyFunction;
    }

    /**
     * Sanitizes the key.
     * @param key the key
     * @param collection the collection name
     * @return the sanitized key
     */
    public String sanitizeKey(String key, String collection) {
        return sanitizeKeyFunction.apply(key, collection);
    }

    public static class Builder<Record> {
        private Class<Record> recordClass;
        private String storageTableName;
        private String defaultCollectionName;
        private JDBCVectorStoreRecordMapper<Record> vectorStoreRecordMapper;
        private JDBCVectorStoreQueryHandler<Record> vectorStoreQueryHandler;
        private VectorStoreRecordDefinition recordDefinition;
        private BiFunction<String, String, String> sanitizeKeyFunction;

        /**
         * Sets the record class.
         * @param recordClass the record class
         * @return the builder
         */
        public Builder<Record> withRecordClass(Class<Record> recordClass) {
            this.recordClass = recordClass;
            return this;
        }

        /**
         * Sets the storage table name. This is the name of the table in the database where the records are stored.
         * @param storageTableName the storage table name
         * @return the builder
         */
        public Builder<Record> withStorageTableName(String storageTableName) {
            this.storageTableName = storageTableName;
            return this;
        }

        /**
         * Sets the default collection name. This is the name of the collection to use if none is provided.
         * @param defaultCollectionName the default collection name
         * @return the builder
         */
        public Builder<Record> withDefaultCollectionName(String defaultCollectionName) {
            this.defaultCollectionName = defaultCollectionName;
            return this;
        }

        /**
         * Sets the vector store record mapper.
         * @param vectorStoreRecordMapper the vector store record mapper
         * @return the builder
         */
        public Builder<Record> withVectorStoreRecordMapper(
            JDBCVectorStoreRecordMapper<Record> vectorStoreRecordMapper) {
            this.vectorStoreRecordMapper = vectorStoreRecordMapper;
            return this;
        }

        /**
         * Sets the vector store query handler.
         * @param vectorStoreQueryHandler the vector store query handler
         * @return the builder
         */
        public Builder<Record> withVectorStoreQueryHandler(
            JDBCVectorStoreQueryHandler<Record> vectorStoreQueryHandler) {
            this.vectorStoreQueryHandler = vectorStoreQueryHandler;
            return this;
        }

        /**
         * Sets the record definition.
         * @param recordDefinition the record definition
         * @return the builder
         */
        public Builder<Record> withRecordDefinition(VectorStoreRecordDefinition recordDefinition) {
            this.recordDefinition = recordDefinition;
            return this;
        }

        /**
         * Sets the function to sanitize the key.
         * The function takes the key and the collection name and returns the sanitized key.
         * (key, collection) -> sanitizedKey
         * Use when the key must be a combination of the key and the collection name.
         * @param sanitizeKeyFunction the function to sanitize the key
         * @return the builder
         */
        public Builder<Record> withSanitizeKeyFunction(
            BiFunction<String, String, String> sanitizeKeyFunction) {
            this.sanitizeKeyFunction = sanitizeKeyFunction;
            return this;
        }

        /**
         * Builds the options.
         * @return the options
         */
        public JDBCVectorStoreOptions<Record> build() {
            if (recordClass == null) {
                throw new IllegalArgumentException("recordClass is required");
            }
            if (storageTableName == null) {
                throw new IllegalArgumentException("storageTableName is required");
            }

            return new JDBCVectorStoreOptions<>(
                recordClass,
                storageTableName,
                defaultCollectionName,
                vectorStoreRecordMapper,
                vectorStoreQueryHandler,
                recordDefinition,
                sanitizeKeyFunction);
        }
    }
}
