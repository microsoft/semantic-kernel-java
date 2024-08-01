// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.data.jdbc;

import com.microsoft.semantickernel.builders.SemanticKernelBuilder;
import com.microsoft.semantickernel.data.recorddefinition.VectorStoreRecordDefinition;
import com.microsoft.semantickernel.data.recordoptions.DeleteRecordOptions;
import com.microsoft.semantickernel.data.recordoptions.GetRecordOptions;
import com.microsoft.semantickernel.data.recordoptions.UpsertRecordOptions;

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
     */
    void prepareVectorStore();

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
     */
    boolean collectionExists(String collectionName);

    /**
     * Creates a collection.
     *
     * @param collectionName the collection name
     * @param recordClass the record class
     * @param recordDefinition the record definition
     */
    void createCollection(String collectionName, Class<?> recordClass,
        VectorStoreRecordDefinition recordDefinition);

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
                                     VectorStoreRecordDefinition recordDefinition, JDBCVectorStoreRecordMapper<Record> mapper,
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
