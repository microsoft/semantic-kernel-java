// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.data.jdbc;

import com.microsoft.semantickernel.data.recorddefinition.VectorStoreRecordDefinition;

public class JDBCVectorStoreRecordCollectionOptions<Record> {
    private final Class<Record> recordClass;
    private final JDBCVectorStoreRecordMapper<Record> vectorStoreRecordMapper;
    private final VectorStoreRecordDefinition recordDefinition;
    private final JDBCVectorStoreQueryProvider queryProvider;

    public JDBCVectorStoreRecordCollectionOptions(
        Class<Record> recordClass,
        VectorStoreRecordDefinition recordDefinition,
        JDBCVectorStoreRecordMapper<Record> vectorStoreRecordMapper,
        JDBCVectorStoreQueryProvider queryProvider) {
        this.recordClass = recordClass;
        this.recordDefinition = recordDefinition;
        this.vectorStoreRecordMapper = vectorStoreRecordMapper;
        this.queryProvider = queryProvider;
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
     * Gets the query provider.
     * @return the query provider
     */
    public JDBCVectorStoreQueryProvider getQueryProvider() {
        return queryProvider;
    }

    public static class Builder<Record> {
        private Class<Record> recordClass;
        private VectorStoreRecordDefinition recordDefinition;
        private JDBCVectorStoreRecordMapper<Record> vectorStoreRecordMapper;
        private JDBCVectorStoreQueryProvider queryProvider;

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
                queryProvider
            );
        }
    }
}
