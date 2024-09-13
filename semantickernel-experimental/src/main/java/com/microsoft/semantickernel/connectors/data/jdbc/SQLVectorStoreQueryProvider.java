// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.data.jdbc;

import com.microsoft.semantickernel.builders.SemanticKernelBuilder;
import com.microsoft.semantickernel.data.vectorsearch.VectorSearchResult;
import com.microsoft.semantickernel.data.vectorsearch.queries.VectorSearchQuery;
import com.microsoft.semantickernel.data.vectorstorage.VectorStoreRecordMapper;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordDefinition;
import com.microsoft.semantickernel.data.vectorstorage.options.DeleteRecordOptions;
import com.microsoft.semantickernel.data.vectorstorage.options.GetRecordOptions;
import com.microsoft.semantickernel.data.vectorstorage.options.UpsertRecordOptions;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

/**
 * The JDBC vector store query provider.
 * Provides the necessary methods to interact with a JDBC vector store and vector store collections.
 */
public interface SQLVectorStoreQueryProvider {
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
     * Vector search.
     * Executes a vector search query and returns the results.
     * The results are mapped to the specified record type using the provided mapper.
     * The query is executed against the specified collection.
     *
     * @param <Record> the record type
     * @param collectionName the collection name
     * @param query the vectorized search query, containing the vector and search options
     * @param recordDefinition the record definition
     * @param mapper the mapper, responsible for mapping the result set to the record type.
     * @return the search results
     */
    <Record> List<VectorSearchResult<Record>> search(String collectionName,
        VectorSearchQuery query,
        VectorStoreRecordDefinition recordDefinition,
        VectorStoreRecordMapper<Record, ResultSet> mapper);

    /**
     * The builder for the JDBC vector store query provider.
     */
    interface Builder extends SemanticKernelBuilder<SQLVectorStoreQueryProvider> {

    }
}
