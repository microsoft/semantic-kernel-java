// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.data.jdbc;

import com.microsoft.semantickernel.data.recorddefinition.VectorStoreRecordDefinition;

public class JDBCVectorStoreRecordCollectionOptions<Record> {

    /**
     * The default name for the collections table.
     */
    public static final String DEFAULT_COLLECTIONS_TABLE = "SKCollections";

    /**
     * The prefix for collection tables.
     */
    public static final String DEFAULT_PREFIX_FOR_COLLECTION_TABLES = "SKCollection_";

    private final Class<Record> recordClass;
    private final JDBCVectorStoreRecordMapper<Record> vectorStoreRecordMapper;
    private final VectorStoreRecordDefinition recordDefinition;
    private final JDBCVectorStoreQueryProvider queryProvider;
    private final String collectionsTableName;
    private final String prefixForCollectionTables;

    private JDBCVectorStoreRecordCollectionOptions(
        Class<Record> recordClass,
        VectorStoreRecordDefinition recordDefinition,
        JDBCVectorStoreRecordMapper<Record> vectorStoreRecordMapper,
        JDBCVectorStoreQueryProvider queryProvider,
        String collectionsTableName,
        String prefixForCollectionTables) {
        this.recordClass = recordClass;
        this.recordDefinition = recordDefinition;
        this.vectorStoreRecordMapper = vectorStoreRecordMapper;
        this.queryProvider = queryProvider;
        this.collectionsTableName = collectionsTableName;
        this.prefixForCollectionTables = prefixForCollectionTables;
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
     * Gets the record definition.
     * @return the record definition
     */
    public VectorStoreRecordDefinition getRecordDefinition() {
        return recordDefinition;
    }

    /**
     * Gets the vector store record mapper.
     * @return the vector store record mapper
     */
    public JDBCVectorStoreRecordMapper<Record> getVectorStoreRecordMapper() {
        return vectorStoreRecordMapper;
    }

    /**
     * Gets the collections table.
     * @return the collections table
     */
    public String getCollectionsTableName() {
        return collectionsTableName;
    }

    /**
     * Gets the prefix for collection tables.
     * @return the prefix for collection tables
     */
    public String getPrefixForCollectionTables() {
        return prefixForCollectionTables;
    }

    /**
     * Gets the query provider.
     * @return the query provider
     */
    public JDBCVectorStoreQueryProvider getQueryProvider() {
        return queryProvider;
    }

    public static void validateSQLidentifier(String identifier) {
        if (!identifier.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
            throw new IllegalArgumentException("Invalid SQL identifier: " + identifier);
        }
    }

    public static class Builder<Record> {
        private Class<Record> recordClass;
        private VectorStoreRecordDefinition recordDefinition;
        private JDBCVectorStoreRecordMapper<Record> vectorStoreRecordMapper;
        private JDBCVectorStoreQueryProvider queryProvider;
        private String collectionsTableName = DEFAULT_COLLECTIONS_TABLE;
        private String prefixForCollectionTables = DEFAULT_PREFIX_FOR_COLLECTION_TABLES;

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
         * Sets the record definition.
         * @param recordDefinition the record definition
         * @return the builder
         */
        public Builder<Record> withRecordDefinition(VectorStoreRecordDefinition recordDefinition) {
            this.recordDefinition = recordDefinition;
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
         * Sets the query provider.
         * @param queryProvider the query provider
         * @return the builder
         */
        public Builder<Record> withQueryProvider(JDBCVectorStoreQueryProvider queryProvider) {
            this.queryProvider = queryProvider;
            return this;
        }

        /**
         * Sets the collections table name.
         * @param collectionsTableName the collections table name
         * @return the builder
         */
        public Builder<Record> withCollectionsTableName(String collectionsTableName) {
            validateSQLidentifier(collectionsTableName);
            this.collectionsTableName = collectionsTableName;
            return this;
        }

        /**
         * Sets the prefix for collection tables.
         * @param prefixForCollectionTables the prefix for collection tables
         * @return the builder
         */
        public Builder<Record> withPrefixForCollectionTables(String prefixForCollectionTables) {
            validateSQLidentifier(prefixForCollectionTables);
            this.prefixForCollectionTables = prefixForCollectionTables;
            return this;
        }

        /**
         * Builds the options.
         * @return the options
         */
        public JDBCVectorStoreRecordCollectionOptions<Record> build() {
            if (recordClass == null) {
                throw new IllegalArgumentException("recordClass is required");
            }

            return new JDBCVectorStoreRecordCollectionOptions<>(
                recordClass,
                recordDefinition,
                vectorStoreRecordMapper,
                queryProvider,
                collectionsTableName,
                prefixForCollectionTables);
        }
    }
}
