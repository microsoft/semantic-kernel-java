package com.microsoft.semantickernel.connectors.data.jdbc;

import javax.annotation.Nullable;

public class JDBCVectorStoreOptions {
    @Nullable
    private final JDBCVectorStoreRecordCollectionFactory vectorStoreRecordCollectionFactory;
    @Nullable
    private final JDBCVectorStoreQueryProvider queryProvider;

    /**
     * Creates a new instance of the JDBC vector store options.
     *
     * @param vectorStoreRecordCollectionFactory The vector store record collection factory.
     */
    public JDBCVectorStoreOptions(
        @Nullable JDBCVectorStoreQueryProvider queryProvider,
        @Nullable JDBCVectorStoreRecordCollectionFactory vectorStoreRecordCollectionFactory) {
        this.queryProvider = queryProvider;
        this.vectorStoreRecordCollectionFactory = vectorStoreRecordCollectionFactory;
    }

    /**
     * Creates a new instance of the JDBC vector store options.
     */
    public JDBCVectorStoreOptions() {
        this(null, null);
    }

    /**
     * Gets the query provider.
     *
     * @return the query provider
     */
    @Nullable
    public JDBCVectorStoreQueryProvider getQueryProvider() {
        return queryProvider;
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
    public JDBCVectorStoreRecordCollectionFactory getVectorStoreRecordCollectionFactory() {
        return vectorStoreRecordCollectionFactory;
    }

    /**
     * Builder for JDBC vector store options.
     *
     */
    public static class Builder {
        @Nullable
        private JDBCVectorStoreQueryProvider queryProvider;
        @Nullable
        private JDBCVectorStoreRecordCollectionFactory vectorStoreRecordCollectionFactory;

        /**
         * Sets the query provider.
         *
         * @param queryProvider The query provider.
         * @return The updated builder instance.
         */
        public Builder withQueryProvider(JDBCVectorStoreQueryProvider queryProvider) {
            this.queryProvider = queryProvider;
            return this;
        }

        /**
         * Sets the vector store record collection factory.
         *
         * @param vectorStoreRecordCollectionFactory The vector store record collection factory.
         * @return The updated builder instance.
         */
        public Builder withVectorStoreRecordCollectionFactory(
            JDBCVectorStoreRecordCollectionFactory vectorStoreRecordCollectionFactory) {
            this.vectorStoreRecordCollectionFactory = vectorStoreRecordCollectionFactory;
            return this;
        }

        /**
         * Builds the JDBC vector store options.
         *
         * @return The JDBC vector store options.
         */
        public JDBCVectorStoreOptions build() {
            return new JDBCVectorStoreOptions(queryProvider, vectorStoreRecordCollectionFactory);
        }
    }
}
