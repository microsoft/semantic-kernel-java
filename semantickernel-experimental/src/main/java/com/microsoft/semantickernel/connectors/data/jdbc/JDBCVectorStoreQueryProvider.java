// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.data.jdbc;

import com.microsoft.semantickernel.builders.SemanticKernelBuilder;
import com.microsoft.semantickernel.data.recorddefinition.VectorStoreRecordDefinition;
import com.microsoft.semantickernel.data.recordoptions.DeleteRecordOptions;
import com.microsoft.semantickernel.data.recordoptions.GetRecordOptions;
import com.microsoft.semantickernel.data.recordoptions.UpsertRecordOptions;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * The JDBC vector store query provider.
 * Provides the necessary methods to interact with a JDBC vector store and vector store collections.
 */
public interface JDBCVectorStoreQueryProvider {
    /**
     * The default name for the collections table.
     */
    String DEFAULT_COLLECTIONS_TABLE = "SKCollections";

    /**
     * The prefix for collection tables.
     */
    String DEFAULT_PREFIX_FOR_COLLECTION_TABLES = "SKCollection_";

    /**
     * Prepares the vector store.
     * Executes any necessary setup steps for the vector store.
     *
     * @throws SQLException if an error occurs
     */
    void prepareVectorStore() throws SQLException;

    /**
     * Checks if the types of the record class fields are supported.
     *
     * @param recordClass the record class
     * @param recordDefinition the record definition
     */
    void validateSupportedTypes(Class<?> recordClass, VectorStoreRecordDefinition recordDefinition);

    /**
     * Checks if a collection exists.
     *
     * @param collectionName the collection name
     * @return true if the collection exists, false otherwise
     * @throws SQLException if an error occurs
     */
    boolean collectionExists(String collectionName) throws SQLException;

    /**
     * Creates a collection.
     *
     * @param collectionName the collection name
     * @param recordClass the record class
     * @param recordDefinition the record definition
     * @throws SQLException if an error occurs
     */
    void createCollection(String collectionName, Class<?> recordClass, VectorStoreRecordDefinition recordDefinition) throws SQLException;

    /**
     * Deletes a collection.
     *
     * @param collectionName the collection name
     * @throws SQLException if an error occurs
     */
    void deleteCollection(String collectionName) throws SQLException;

    /**
     * Gets the names of the collections.
     *
     * @return the result set
     * @throws SQLException if an error occurs
     */
    ResultSet getCollectionNames() throws SQLException;

    /**
     * Gets the records.
     *
     * @param collectionName the collection name
     * @param keys the keys
     * @param recordDefinition the record definition
     * @param options the options
     * @return the result set
     * @throws SQLException if an error occurs
     */
    ResultSet getRecords(String collectionName, List<String> keys, VectorStoreRecordDefinition recordDefinition, GetRecordOptions options) throws SQLException;

    /**
     * Upserts records.
     *
     * @param collectionName the collection name
     * @param records the records
     * @param vectorStoreRecordDefinition the record definition
     * @param options the options
     * @throws SQLException if an error occurs
     */
    void upsertRecords(String collectionName, List<?> records, VectorStoreRecordDefinition vectorStoreRecordDefinition, UpsertRecordOptions options) throws SQLException;

    /**
     * Deletes records.
     *
     * @param collectionName the collection name
     * @param keys the keys
     * @param recordDefinition the record definition
     * @param options the options
     * @throws SQLException if an error occurs
     */
    void deleteRecords(String collectionName, List<String> keys, VectorStoreRecordDefinition recordDefinition, DeleteRecordOptions options) throws SQLException;

    /**
     * The builder for the JDBC vector store query provider.
     */
    interface Builder extends SemanticKernelBuilder<JDBCVectorStoreQueryProvider> {

    }
}
