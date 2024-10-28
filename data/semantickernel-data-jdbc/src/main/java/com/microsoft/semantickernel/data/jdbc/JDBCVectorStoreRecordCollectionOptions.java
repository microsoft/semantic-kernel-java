// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.jdbc;

import com.microsoft.semantickernel.data.vectorstorage.VectorStoreRecordCollectionOptions;
import com.microsoft.semantickernel.data.vectorstorage.VectorStoreRecordMapper;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordDefinition;
import com.microsoft.semantickernel.exceptions.SKException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.sql.ResultSet;

import static com.microsoft.semantickernel.data.jdbc.SQLVectorStoreQueryProvider.DEFAULT_COLLECTIONS_TABLE;
import static com.microsoft.semantickernel.data.jdbc.SQLVectorStoreQueryProvider.DEFAULT_PREFIX_FOR_COLLECTION_TABLES;

/**
 * Options for a JDBC vector store record collection.
 * @param <Record> the record type
 */
public class JDBCVectorStoreRecordCollectionOptions<Record>
    implements VectorStoreRecordCollectionOptions<String, Record> {
    private final Class<Record> recordClass;
    private final VectorStoreRecordMapper<Record, ResultSet> vectorStoreRecordMapper;
    private final VectorStoreRecordDefinition recordDefinition;
    private final SQLVectorStoreQueryProvider queryProvider;
    private final String collectionsTableName;
    private final String prefixForCollectionTables;

    private JDBCVectorStoreRecordCollectionOptions(
        Class<Record> recordClass,
        VectorStoreRecordDefinition recordDefinition,
        VectorStoreRecordMapper<Record, ResultSet> vectorStoreRecordMapper,
        SQLVectorStoreQueryProvider queryProvider,
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
    public VectorStoreRecordMapper<Record, ResultSet> getVectorStoreRecordMapper() {
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
    @SuppressFBWarnings("EI_EXPOSE_REP") // DataSource in queryProvider is not exposed
    public SQLVectorStoreQueryProvider getQueryProvider() {
        return queryProvider;
    }

    /**
     * Builder for JDBC vector store record collection options.
     * @param <Record> the record type
     */
    public static class Builder<Record> {
        private Class<Record> recordClass;
        private VectorStoreRecordDefinition recordDefinition;
        private VectorStoreRecordMapper<Record, ResultSet> vectorStoreRecordMapper;
        private SQLVectorStoreQueryProvider queryProvider;
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
            VectorStoreRecordMapper<Record, ResultSet> vectorStoreRecordMapper) {
            this.vectorStoreRecordMapper = vectorStoreRecordMapper;
            return this;
        }

        /**
         * Sets the query provider.
         * @param queryProvider the query provider
         * @return the builder
         */
        @SuppressFBWarnings("EI_EXPOSE_REP2") // DataSource in queryProvider is not exposed
        public Builder<Record> withQueryProvider(SQLVectorStoreQueryProvider queryProvider) {
            this.queryProvider = queryProvider;
            return this;
        }

        /**
         * Sets the collections table name.
         * @param collectionsTableName the collections table name
         * @return the builder
         */
        public Builder<Record> withCollectionsTableName(String collectionsTableName) {
            this.collectionsTableName = JDBCVectorStoreQueryProvider
                .validateSQLidentifier(collectionsTableName);
            return this;
        }

        /**
         * Sets the prefix for collection tables.
         * @param prefixForCollectionTables the prefix for collection tables
         * @return the builder
         */
        public Builder<Record> withPrefixForCollectionTables(String prefixForCollectionTables) {
            this.prefixForCollectionTables = JDBCVectorStoreQueryProvider
                .validateSQLidentifier(prefixForCollectionTables);
            return this;
        }

        /**
         * Builds the options.
         * @return the options
         */
        public JDBCVectorStoreRecordCollectionOptions<Record> build() {
            if (recordClass == null) {
                throw new SKException("recordClass is required");
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
