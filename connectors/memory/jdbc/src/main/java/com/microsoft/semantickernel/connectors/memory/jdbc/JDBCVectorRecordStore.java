// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.memory.jdbc;

import com.microsoft.semantickernel.exceptions.SKException;
import com.microsoft.semantickernel.memory.VectorRecordStore;
import com.microsoft.semantickernel.memory.recorddefinition.VectorStoreRecordDefinition;
import com.microsoft.semantickernel.memory.recorddefinition.VectorStoreRecordField;
import com.microsoft.semantickernel.memory.recordoptions.DeleteRecordOptions;
import com.microsoft.semantickernel.memory.recordoptions.GetRecordOptions;
import com.microsoft.semantickernel.memory.recordoptions.UpsertRecordOptions;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class JDBCVectorRecordStore<Record> implements VectorRecordStore<String, Record> {
    private final Connection connection;
    private final String collectionName;
    private final JDBCVectorStoreOptions<Record> options;
    private static final HashSet<Class<?>> supportedVectorTypes = new HashSet<>(Arrays.asList(
        String.class,
        List.class,
        Collection.class));

    /**
     * Creates a new instance of the JDBCVectorRecordStore.
     *
     * @param connection The JDBC connection.
     * @param options    The options for the store.
     */
    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public JDBCVectorRecordStore(Connection connection, String collectionName,
        JDBCVectorStoreOptions<Record> options) {
        this.connection = connection;
        this.collectionName = collectionName;

        // If record definition is not provided, create one from the record class
        final VectorStoreRecordDefinition vectorStoreRecordDefinition;
        if (options.getRecordDefinition() == null) {
            vectorStoreRecordDefinition = VectorStoreRecordDefinition
                .create(options.getRecordClass());
        } else {
            vectorStoreRecordDefinition = options.getRecordDefinition();
        }

        // Check that the fields are supported
        VectorStoreRecordDefinition.checkFieldsType(supportedVectorTypes,
            options.getRecordClass(),
            vectorStoreRecordDefinition.getVectorFields().stream()
                .map(field -> (VectorStoreRecordField) field).collect(Collectors.toList()));

        // If mapper is not provided, add a default one
        JDBCVectorStoreRecordMapper<Record> vectorStoreRecordMapper = options
            .getVectorStoreRecordMapper();
        if (vectorStoreRecordMapper == null) {
            vectorStoreRecordMapper = JDBCVectorStoreRecordMapper.<Record>builder()
                .withRecordClass(options.getRecordClass())
                .withVectorStoreRecordDefinition(vectorStoreRecordDefinition)
                .build();
        }

        // If query handler is not provided, add a default one
        JDBCVectorStoreQueryProvider<Record> vectorStoreRecordHandler = options
            .getVectorStoreQueryHandler();
        if (vectorStoreRecordHandler == null) {
            vectorStoreRecordHandler = MySQLVectorStoreDefaultQueryProvider.<Record>builder()
                .withStorageTableName(options.getStorageTableName())
                .withRecordDefinition(vectorStoreRecordDefinition)
                .withRecordClass(options.getRecordClass())
                .withSanitizeKeyFunction(options.getSanitizeKeyFunction() == null
                    ? (key, collection) -> key
                    : options.getSanitizeKeyFunction())
                .build();
        }

        this.options = JDBCVectorStoreOptions.<Record>builder()
            .withRecordClass(options.getRecordClass())
            .withStorageTableName(options.getStorageTableName())
            .withRecordDefinition(vectorStoreRecordDefinition)
            .withVectorStoreRecordMapper(vectorStoreRecordMapper)
            .withVectorStoreQueryHandler(vectorStoreRecordHandler)
            .build();
    }

    /**
     * Gets a record from the store.
     *
     * @param key       The key of the record to get.
     * @param options The options for getting the record.
     * @return A Mono emitting the record.
     */
    @Override
    public Mono<Record> getAsync(String key, GetRecordOptions options) {
        JDBCVectorStoreQueryProvider<Record> queryHandler = this.options
            .getVectorStoreQueryHandler();
        return Mono.defer(
            () -> {
                String query = queryHandler.formatGetQuery(1, options);

                try (PreparedStatement statement = this.connection.prepareStatement(query)) {
                    queryHandler.configureStatementGetQuery(statement,
                        Collections.singletonList(key),
                        this.collectionName);

                    ResultSet resultSet = statement.executeQuery();
                    if (resultSet.next()) {
                        return Mono.just(this.options.getVectorStoreRecordMapper()
                            .mapStorageModeltoRecord(resultSet));
                    }
                } catch (SQLException e) {
                    throw new SKException("Failed to get record", e);
                }
                return Mono.empty();
            })
            .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Gets a batch of records from the store.
     *
     * @param keys The keys of the records to get.
     * @param options The options for getting the records.
     * @return A Mono emitting a collection of records.
     */
    @Override
    public Mono<List<Record>> getBatchAsync(List<String> keys, GetRecordOptions options) {
        JDBCVectorStoreQueryProvider<Record> queryHandler = this.options
            .getVectorStoreQueryHandler();
        return Mono.defer(
            () -> {
                String query = queryHandler.formatGetQuery(keys.size(), options);
                List<Record> records = new ArrayList<>();

                try (PreparedStatement statement = this.connection.prepareStatement(query)) {
                    queryHandler.configureStatementGetQuery(statement, keys, this.collectionName);

                    ResultSet resultSet = statement.executeQuery();
                    while (resultSet.next()) {
                        records.add(this.options.getVectorStoreRecordMapper()
                            .mapStorageModeltoRecord(resultSet));
                    }
                } catch (SQLException e) {
                    throw new SKException("Failed to get records", e);
                }

                return Mono.just(records);
            }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Inserts or updates a record in the store.
     *
     * @param data    The record to upsert.
     * @param options The options for upserting the record.
     * @return A Mono emitting the key of the upserted record.
     */
    @Override
    public Mono<String> upsertAsync(Record data, UpsertRecordOptions options) {
        JDBCVectorStoreQueryProvider<Record> queryHandler = this.options
            .getVectorStoreQueryHandler();
        return Mono.defer(
            () -> {
                String query = queryHandler.formatUpsertQuery(options);

                try (PreparedStatement statement = this.connection.prepareStatement(query)) {
                    String key = queryHandler.configureStatementUpsertQuery(statement, data,
                        this.collectionName);
                    statement.execute();

                    return Mono.just(key);
                } catch (SQLException e) {
                    throw new SKException("Failed to upsert record", e);
                }
            })
            .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Inserts or updates a batch of records in the store.
     *
     * @param data    The records to upsert.
     * @param options The options for upserting the records.
     * @return A Mono emitting a collection of keys of the upserted records.
     */
    @Override
    public Mono<List<String>> upsertBatchAsync(List<Record> data, UpsertRecordOptions options) {
        JDBCVectorStoreQueryProvider<Record> queryHandler = this.options
            .getVectorStoreQueryHandler();
        return Mono.defer(
            () -> {
                String query = queryHandler.formatUpsertQuery(options);
                List<String> keys = new ArrayList<>();

                try (PreparedStatement statement = this.connection.prepareStatement(query)) {
                    for (Record record : data) {
                        keys.add(
                            queryHandler.configureStatementUpsertQuery(statement, record,
                                this.collectionName));
                        statement.addBatch();
                    }

                    statement.executeBatch();
                    return Mono.just(keys);
                } catch (SQLException e) {
                    throw new SKException("Failed to upsert records", e);
                }
            })
            .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Deletes a record from the store.
     *
     * @param key       The key of the record to delete.
     * @param options The options for deleting the record.
     * @return A Mono representing the completion of the deletion operation.
     */
    @Override
    public Mono<Void> deleteAsync(String key, DeleteRecordOptions options) {
        JDBCVectorStoreQueryProvider<Record> queryHandler = this.options
            .getVectorStoreQueryHandler();
        return Mono.fromRunnable(
            () -> {
                String query = queryHandler.formatDeleteQuery(1, options);

                try (PreparedStatement statement = this.connection.prepareStatement(query)) {
                    queryHandler.configureStatementDeleteQuery(statement,
                        Collections.singletonList(key),
                        this.collectionName);
                    statement.execute();
                } catch (SQLException e) {
                    throw new SKException("Failed to delete record", e);
                }
            }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    /**
     * Deletes a batch of records from the store.
     *
     * @param keys The keys of the records to delete.
     * @param options The options for deleting the records.
     * @return A Mono representing the completion of the deletion operation.
     */
    @Override
    public Mono<Void> deleteBatchAsync(List<String> keys, DeleteRecordOptions options) {
        JDBCVectorStoreQueryProvider<Record> queryHandler = this.options
            .getVectorStoreQueryHandler();
        return Mono.fromRunnable(
            () -> {
                String query = queryHandler.formatDeleteQuery(keys.size(), options);

                try (PreparedStatement statement = this.connection.prepareStatement(query)) {
                    queryHandler.configureStatementDeleteQuery(statement, keys,
                        this.collectionName);

                    statement.execute();
                } catch (SQLException e) {
                    throw new SKException("Failed to delete records", e);
                }
            }).subscribeOn(Schedulers.boundedElastic()).then();
    }
}
