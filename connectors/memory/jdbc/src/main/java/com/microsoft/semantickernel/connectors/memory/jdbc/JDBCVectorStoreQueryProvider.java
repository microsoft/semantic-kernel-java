// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.memory.jdbc;

import com.microsoft.semantickernel.builders.SemanticKernelBuilder;
import com.microsoft.semantickernel.memory.recordoptions.DeleteRecordOptions;
import com.microsoft.semantickernel.memory.recordoptions.GetRecordOptions;
import com.microsoft.semantickernel.memory.recordoptions.UpsertRecordOptions;

import javax.annotation.Nullable;
import java.sql.PreparedStatement;
import java.util.List;

public interface JDBCVectorStoreQueryProvider<Record> {
    /**
     * Formats a query to get records from the database.
     *
     * @param numberOfKeys the number of keys to get records for
     * @param options the options for getting the records
     * @return the formatted query
     */
    String formatGetQuery(int numberOfKeys, @Nullable GetRecordOptions options);

    /**
     * Formats a query to delete records from the database.
     *
     * @param numberOfKeys the number of keys to delete records for
     * @param options the options for deleting the records
     * @return the formatted query
     */
    String formatDeleteQuery(int numberOfKeys, @Nullable DeleteRecordOptions options);

    /**
     * Formats a query to upsert records in the database.
     * @param options the options for upserting the records
     *
     * @return the formatted query
     */
    String formatUpsertQuery(@Nullable UpsertRecordOptions options);

    /**
     * Configures the values for a get query generated by {@link #formatGetQuery(int, GetRecordOptions)}.
     *
     * @param statement the prepared statement
     * @param keys the keys to get records for
     * @param collectionName the name of the collection
     */
    void configureStatementGetQuery(PreparedStatement statement, List<String> keys,
        String collectionName);

    /**
     * Configures the values for a delete query generated by {@link #formatDeleteQuery(int, DeleteRecordOptions)}.
     *
     * @param statement the prepared statement
     * @param keys the keys to delete records for
     * @param collectionName the name of the collection
     */
    void configureStatementDeleteQuery(PreparedStatement statement, List<String> keys,
        String collectionName);

    /**
     * Configures the values for an upsert query generated by {@link #formatUpsertQuery(UpsertRecordOptions)}.
     *
     * @param statement the prepared statement
     * @param data the record to upsert
     * @param collectionName the name of the collection
     * @return the key of the record
     */
    String configureStatementUpsertQuery(PreparedStatement statement, Record data,
        String collectionName);

    interface Builder<Record> extends SemanticKernelBuilder<JDBCVectorStoreQueryProvider<Record>> {

    }
}
