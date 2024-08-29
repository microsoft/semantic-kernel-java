// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.data.jdbc;

import com.microsoft.semantickernel.builders.SemanticKernelBuilder;
import com.microsoft.semantickernel.data.VectorStoreRecordMapper;
import com.microsoft.semantickernel.data.record.definition.VectorStoreRecordDefinition;
import com.microsoft.semantickernel.data.record.options.DeleteRecordOptions;
import com.microsoft.semantickernel.data.record.options.GetRecordOptions;
import com.microsoft.semantickernel.data.record.options.UpsertRecordOptions;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

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
     * Gets the supported key types and their corresponding SQL types.
     *
     * @return the supported key types
     */
    Map<Class<?>, String> getSupportedKeyTypes();

    /**
     * Gets the supported data types and their corresponding SQL types.
     *
     * @return the supported data types
     */
    Map<Class<?>, String> getSupportedDataTypes();

    /**
     * Gets the supported vector types and their corresponding SQL types.
     *
     * @return the supported vector types
     */
    Map<Class<?>, String> getSupportedVectorTypes();

    /**
     * Prepares the vector store.
     * Executes any necessary setup steps for the vector store.
     */
    void prepareVectorStore();

    /**
     * Checks if the types of the record class fields are supported.
     *
     * @param recordDefinition the record definition
     */
    void validateSupportedTypes(VectorStoreRecordDefinition recordDefinition);

    /**
     * Checks if a collection exists.
     *
     * @param collectionName the collection name
     * @return true if the collection exists, false otherwise
     */
    boolean collectionExists(String collectionName);

    /**
     * Creates a collection.
     *
     * @param collectionName the collection name
     * @param recordDefinition the record definition
     */
    void createCollection(String collectionName, VectorStoreRecordDefinition recordDefinition);

    /**
     * Deletes a collection.
     *
     * @param collectionName the collection name
     */
    void deleteCollection(String collectionName);

    /**
     * Gets the collection names.
     *
     * @return the collection names
     */
    List<String> getCollectionNames();

    /**
     * Gets records.
     *
     * @param collectionName the collection name
     * @param keys the keys
     * @param recordDefinition the record definition
     * @param mapper the mapper
     * @param options the options
     * @return the records
     */
    <Record> List<Record> getRecords(String collectionName, List<String> keys,
        VectorStoreRecordDefinition recordDefinition,
        VectorStoreRecordMapper<Record, ResultSet> mapper,
        GetRecordOptions options);

    /**
     * Upserts records.
     *
     * @param collectionName the collection name
     * @param records the records
     * @param vectorStoreRecordDefinition the record definition
     * @param options the options
     */
    void upsertRecords(String collectionName, List<?> records,
        VectorStoreRecordDefinition vectorStoreRecordDefinition, UpsertRecordOptions options);

    /**
     * Deletes records.
     *
     * @param collectionName the collection name
     * @param keys the keys
     * @param recordDefinition the record definition
     * @param options the options
     */
    void deleteRecords(String collectionName, List<String> keys,
        VectorStoreRecordDefinition recordDefinition, DeleteRecordOptions options);

    /**
     * The builder for the JDBC vector store query provider.
     */
    interface Builder extends SemanticKernelBuilder<JDBCVectorStoreQueryProvider> {

    }
}
