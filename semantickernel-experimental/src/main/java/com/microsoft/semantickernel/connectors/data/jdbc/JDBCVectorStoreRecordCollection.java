// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.data.jdbc;

import com.microsoft.semantickernel.builders.SemanticKernelBuilder;
import com.microsoft.semantickernel.connectors.data.mysql.MySQLVectorStoreQueryProvider;
import com.microsoft.semantickernel.connectors.data.postgres.PostgreSQLVectorStoreQueryProvider;
import com.microsoft.semantickernel.connectors.data.postgres.PostgreSQLVectorStoreRecordMapper;
import com.microsoft.semantickernel.data.vectorsearch.VectorSearchResult;
import com.microsoft.semantickernel.data.vectorsearch.VectorizedSearch;
import com.microsoft.semantickernel.data.vectorsearch.queries.VectorSearchQuery;
import com.microsoft.semantickernel.data.vectorstorage.VectorStoreRecordMapper;
import com.microsoft.semantickernel.data.vectorstorage.VectorStoreRecordCollection;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordDefinition;
import com.microsoft.semantickernel.data.vectorstorage.options.DeleteRecordOptions;
import com.microsoft.semantickernel.data.vectorstorage.options.GetRecordOptions;
import com.microsoft.semantickernel.data.vectorstorage.options.UpsertRecordOptions;
import com.microsoft.semantickernel.data.vectorstorage.options.VectorSearchOptions;
import com.microsoft.semantickernel.exceptions.SKException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.sql.DataSource;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class JDBCVectorStoreRecordCollection<Record>
    implements SQLVectorStoreRecordCollection<String, Record>,
    VectorizedSearch<Record> {

    private final String collectionName;
    private final VectorStoreRecordDefinition recordDefinition;
    private final VectorStoreRecordMapper<Record, ResultSet> vectorStoreRecordMapper;
    private final JDBCVectorStoreRecordCollectionOptions<Record> options;
    private final SQLVectorStoreQueryProvider queryProvider;

    /**
     * Creates a new instance of the {@link JDBCVectorStoreRecordCollection}.
     *
     * @param dataSource     the data source
     * @param collectionName the name of the collection
     * @param options        the options
     */
    @SuppressFBWarnings("EI_EXPOSE_REP2") // DataSource is not exposed
    public JDBCVectorStoreRecordCollection(
        @Nonnull DataSource dataSource,
        @Nonnull String collectionName,
        @Nonnull JDBCVectorStoreRecordCollectionOptions<Record> options) {
        this.collectionName = collectionName;
        this.options = options;

        // If record definition is not provided, create one from the record class
        recordDefinition = options.getRecordDefinition() == null
            ? VectorStoreRecordDefinition.fromRecordClass(options.getRecordClass())
            : options.getRecordDefinition();

        // If the query provider is not provided, set a default one
        if (options.getQueryProvider() == null) {
            this.queryProvider = JDBCVectorStoreQueryProvider.builder()
                .withDataSource(dataSource)
                .build();
        } else {
            this.queryProvider = options.getQueryProvider();
        }

        // If mapper is not provided, set a default one
        if (options.getVectorStoreRecordMapper() == null) {
            // Default mapper for PostgreSQL
            if (this.queryProvider instanceof PostgreSQLVectorStoreQueryProvider) {
                vectorStoreRecordMapper = PostgreSQLVectorStoreRecordMapper.<Record>builder()
                    .withRecordClass(options.getRecordClass())
                    .withVectorStoreRecordDefinition(recordDefinition)
                    .build();
                // Default mapper for MySQL
            } else if (this.queryProvider instanceof MySQLVectorStoreQueryProvider) {
                vectorStoreRecordMapper = JDBCVectorStoreRecordMapper.<Record>builder()
                    .withRecordClass(options.getRecordClass())
                    .withVectorStoreRecordDefinition(recordDefinition)
                    .build();
                // Default mapper for other databases
            } else {
                vectorStoreRecordMapper = JDBCVectorStoreRecordMapper.<Record>builder()
                    .withRecordClass(options.getRecordClass())
                    .withVectorStoreRecordDefinition(recordDefinition)
                    .build();
            }
        } else {
            vectorStoreRecordMapper = options.getVectorStoreRecordMapper();
        }

        // Check if the types are supported
        queryProvider.validateSupportedTypes(recordDefinition);
    }

    /**
     * Gets the name of the collection.
     *
     * @return The name of the collection.
     */
    @Override
    public String getCollectionName() {
        return collectionName;
    }

    /**
     * Checks if the collection exists in the store.
     *
     * @return A Mono emitting a boolean indicating if the collection exists.
     * @throws SKException if the operation fails
     */
    @Override
    public Mono<Boolean> collectionExistsAsync() {
        return Mono.fromCallable(
            () -> queryProvider.collectionExists(this.collectionName))
            .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Creates the collection in the store.
     *
     * @return A Mono representing the completion of the creation operation.
     * @throws SKException if the operation fails
     */
    @Override
    public Mono<VectorStoreRecordCollection<String, Record>> createCollectionAsync() {
        return Mono.fromRunnable(
            () -> queryProvider.createCollection(this.collectionName, recordDefinition))
            .subscribeOn(Schedulers.boundedElastic())
            .then(Mono.just(this));
    }

    /**
     * Creates the collection in the store if it does not exist.
     *
     * @return A Mono representing the completion of the creation operation.
     * @throws SKException if the operation fails
     */
    @Override
    public Mono<VectorStoreRecordCollection<String, Record>> createCollectionIfNotExistsAsync() {
        return collectionExistsAsync().map(
            exists -> {
                if (!exists) {
                    return createCollectionAsync();
                }
                return Mono.empty();
            })
            .flatMap(mono -> mono)
            .then(Mono.just(this));
    }

    /**
     * Deletes the collection from the store.
     *
     * @return A Mono representing the completion of the deletion operation.
     * @throws SKException if the operation fails
     */
    @Override
    public Mono<Void> deleteCollectionAsync() {
        return Mono.fromRunnable(
            () -> {
                queryProvider.deleteCollection(this.collectionName);
            }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    /**
     * Gets a record from the store.
     *
     * @param key     The key of the record to get.
     * @param options The options for getting the record.
     * @return A Mono emitting the record.
     * @throws SKException if the operation fails
     */
    @Override
    public Mono<Record> getAsync(String key, GetRecordOptions options) {
        Objects.requireNonNull(key, "key is required");

        return this.getBatchAsync(Collections.singletonList(key), options)
            .mapNotNull(records -> {
                if (records.isEmpty()) {
                    return null;
                }
                return records.get(0);
            });
    }

    /**
     * Gets a batch of records from the store.
     *
     * @param keys    The keys of the records to get.
     * @param options The options for getting the records.
     * @return A Mono emitting a collection of records.
     * @throws SKException if the operation fails
     */
    @Override
    public Mono<List<Record>> getBatchAsync(@Nonnull List<String> keys, GetRecordOptions options) {
        Objects.requireNonNull(keys, "keys is required");

        return Mono.fromCallable(
            () -> queryProvider.getRecords(this.collectionName, keys, recordDefinition,
                vectorStoreRecordMapper, options))
            .subscribeOn(Schedulers.boundedElastic());
    }

    protected String getKeyFromRecord(Record data) {
        try {
            Field keyField = data.getClass()
                .getDeclaredField(recordDefinition.getKeyField().getEffectiveStorageName());
            keyField.setAccessible(true);
            return (String) keyField.get(data);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new SKException("Failed to get key from record", e);
        }
    }

    /**
     * Inserts or updates a record in the store.
     *
     * @param data    The record to upsert.
     * @param options The options for upserting the record.
     * @return A Mono emitting the key of the upserted record.
     * @throws SKException if the operation fails
     */
    @Override
    public Mono<String> upsertAsync(Record data, UpsertRecordOptions options) {
        Objects.requireNonNull(data, "data is required");

        return this.upsertBatchAsync(Collections.singletonList(data), options)
            .mapNotNull(keys -> {
                if (keys.isEmpty()) {
                    return null;
                }
                return keys.get(0);
            });
    }

    /**
     * Inserts or updates a batch of records in the store.
     *
     * @param data    The records to upsert.
     * @param options The options for upserting the records.
     * @return A Mono emitting a collection of keys of the upserted records.
     * @throws SKException if the operation fails
     */
    @Override
    public Mono<List<String>> upsertBatchAsync(List<Record> data, UpsertRecordOptions options) {
        Objects.requireNonNull(data, "data is required");

        return Mono.fromCallable(
            () -> {
                queryProvider.upsertRecords(this.collectionName, data, recordDefinition, options);
                return data.stream().map(this::getKeyFromRecord).collect(Collectors.toList());
            })
            .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Deletes a record from the store.
     *
     * @param key     The key of the record to delete.
     * @param options The options for deleting the record.
     * @return A Mono representing the completion of the deletion operation.
     * @throws SKException if the operation fails
     */
    @Override
    public Mono<Void> deleteAsync(String key, DeleteRecordOptions options) {
        return this.deleteBatchAsync(Collections.singletonList(key), options);
    }

    /**
     * Deletes a batch of records from the store.
     *
     * @param keys    The keys of the records to delete.
     * @param options The options for deleting the records.
     * @return A Mono representing the completion of the deletion operation.
     * @throws SKException if the operation fails
     */
    @Override
    public Mono<Void> deleteBatchAsync(List<String> keys, DeleteRecordOptions options) {
        return Mono.fromRunnable(
            () -> {
                queryProvider.deleteRecords(this.collectionName, keys, recordDefinition, options);
            }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    /**
     * Prepares the collection for use.
     *
     * @return A Mono representing the completion of the preparation operation.
     * @throws SKException if the operation fails
     */
    @Override
    public Mono<Void> prepareAsync() {
        return Mono.fromRunnable(queryProvider::prepareVectorStore)
            .subscribeOn(Schedulers.boundedElastic()).then();
    }

    @Override
    public Mono<List<VectorSearchResult<Record>>> searchAsync(VectorSearchQuery query) {
        return Mono.fromCallable(
            () -> queryProvider.search(this.collectionName, query, recordDefinition,
                vectorStoreRecordMapper))
            .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Vectorized search. This method searches for records that are similar to the given vector.
     *
     * @param vector              The vector to search with.
     * @param vectorSearchOptions The options to use for the search.
     * @return A list of search results.
     */
    @Override
    public Mono<List<VectorSearchResult<Record>>> searchAsync(List<Float> vector,
        VectorSearchOptions vectorSearchOptions) {
        return this.searchAsync(VectorSearchQuery.createQuery(vector, vectorSearchOptions));
    }

    public static class Builder<Record>
        implements SemanticKernelBuilder<JDBCVectorStoreRecordCollection<Record>> {

        private DataSource dataSource;
        private String collectionName;
        private JDBCVectorStoreRecordCollectionOptions<Record> options;

        /**
         * Sets the data source.
         *
         * @param dataSource the data source
         * @return the builder
         */
        @SuppressFBWarnings("EI_EXPOSE_REP2") // DataSource is not exposed
        public Builder<Record> withDataSource(DataSource dataSource) {
            this.dataSource = dataSource;
            return this;
        }

        /**
         * Sets the collection name.
         *
         * @param collectionName the collection name
         * @return the builder
         */
        public Builder<Record> withCollectionName(String collectionName) {
            this.collectionName = collectionName;
            return this;
        }

        /**
         * Sets the options.
         *
         * @param options the options
         * @return the builder
         */
        public Builder<Record> withOptions(JDBCVectorStoreRecordCollectionOptions<Record> options) {
            this.options = options;
            return this;
        }

        @Override
        public JDBCVectorStoreRecordCollection<Record> build() {
            if (dataSource == null) {
                throw new SKException("dataSource is required");
            }
            if (collectionName == null) {
                throw new SKException("collectionName is required");
            }
            if (options == null) {
                throw new SKException("options is required");
            }

            return new JDBCVectorStoreRecordCollection<>(dataSource, collectionName, options);
        }
    }
}
