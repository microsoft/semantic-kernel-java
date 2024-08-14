// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.data.jdbc;

import com.microsoft.semantickernel.data.VectorStoreRecordCollection;
import com.microsoft.semantickernel.data.recorddefinition.VectorStoreRecordDefinition;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.util.List;

/**
 * A JDBC vector store.
 */
public class JDBCVectorStore implements SQLVectorStore {
    private final DataSource dataSource;
    private final JDBCVectorStoreOptions options;
    private final JDBCVectorStoreQueryProvider queryProvider;

    /**
     * Creates a new instance of the {@link JDBCVectorStore}.
     * If using this constructor, call {@link #prepareAsync()} before using the vector store.
     *
     * @param dataSource the connection
     * @param options    the options
     */
    @SuppressFBWarnings("EI_EXPOSE_REP2") // DataSource is not exposed
    public JDBCVectorStore(@Nonnull DataSource dataSource,
        @Nullable JDBCVectorStoreOptions options) {
        this.dataSource = dataSource;
        this.options = options;

        if (this.options != null && this.options.getQueryProvider() != null) {
            this.queryProvider = this.options.getQueryProvider();
        } else {
            this.queryProvider = JDBCVectorStoreDefaultQueryProvider.builder()
                .withDataSource(dataSource)
                .build();
        }
    }

    /**
     * Creates a new builder for the vector store.
     *
     * @return the builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Gets a collection from the vector store.
     *
     * @param collectionName   The name of the collection.
     * @param recordClass      The class type of the record.
     * @param recordDefinition The record definition.
     * @return The collection.
     */
    @Override
    public <Key, Record> VectorStoreRecordCollection<Key, Record> getCollection(
        @Nonnull String collectionName, @Nonnull Class<Key> keyClass,
        @Nonnull Class<Record> recordClass,
        @Nullable VectorStoreRecordDefinition recordDefinition) {
        if (keyClass != String.class) {
            throw new IllegalArgumentException("Redis only supports string keys");
        }

        return (VectorStoreRecordCollection<Key, Record>) getCollection(
            collectionName,
            recordClass,
            recordDefinition);
    }

    /**
     * Gets a collection from the vector store.
     *
     * @param collectionName   The name of the collection.
     * @param recordClass      The class type of the record.
     * @param recordDefinition The record definition.
     * @return The collection.
     */
    public <Record> JDBCVectorStoreRecordCollection<Record> getCollection(
        @Nonnull String collectionName,
        @Nonnull Class<Record> recordClass,
        @Nullable VectorStoreRecordDefinition recordDefinition) {
        if (this.options != null && this.options.getVectorStoreRecordCollectionFactory() != null) {
            return this.options.getVectorStoreRecordCollectionFactory()
                .createVectorStoreRecordCollection(
                    dataSource,
                    collectionName,
                    recordClass,
                    recordDefinition);
        }

        return new JDBCVectorStoreRecordCollection<>(
            dataSource,
            collectionName,
            JDBCVectorStoreRecordCollectionOptions.<Record>builder()
                .withRecordClass(recordClass)
                .withRecordDefinition(recordDefinition)
                .withQueryProvider(this.queryProvider)
                .build());
    }

    /**
     * Gets the names of all collections in the vector store.
     *
     * @return A list of collection names.
     */
    @Override
    public Mono<List<String>> getCollectionNamesAsync() {
        return Mono.fromCallable(queryProvider::getCollectionNames)
            .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Prepares the vector store.
     */
    @Override
    public Mono<Void> prepareAsync() {
        return Mono.fromRunnable(queryProvider::prepareVectorStore)
            .subscribeOn(Schedulers.boundedElastic()).then();
    }

    /**
     * Builder for creating a {@link JDBCVectorStore}.
     */
    public static class Builder {
        private DataSource dataSource;
        private JDBCVectorStoreOptions options;

        /**
         * Sets the data source.
         *
         * @param dataSource the data source
         * @return the builder
         */
        @SuppressFBWarnings("EI_EXPOSE_REP2")
        public Builder withDataSource(DataSource dataSource) {
            this.dataSource = dataSource;
            return this;
        }

        /**
         * Sets the options.
         *
         * @param options the options
         * @return the builder
         */
        public Builder withOptions(JDBCVectorStoreOptions options) {
            this.options = options;
            return this;
        }

        /**
         * Builds the {@link JDBCVectorStore}.
         *
         * @return the {@link JDBCVectorStore}
         */
        public JDBCVectorStore build() {
            return buildAsync().block();
        }

        /**
         * Builds the {@link JDBCVectorStore} asynchronously.
         *
         * @return the {@link Mono} with the {@link JDBCVectorStore}
         */
        public Mono<JDBCVectorStore> buildAsync() {
            if (dataSource == null) {
                throw new IllegalArgumentException("dataSource is required");
            }

            JDBCVectorStore vectorStore = new JDBCVectorStore(dataSource, options);
            return vectorStore.prepareAsync().thenReturn(vectorStore);
        }
    }
}
