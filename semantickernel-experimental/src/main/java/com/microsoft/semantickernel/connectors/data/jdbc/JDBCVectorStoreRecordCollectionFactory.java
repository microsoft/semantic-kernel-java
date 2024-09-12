// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.data.jdbc;

import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordDefinition;

import javax.sql.DataSource;

/**
 * Factory for creating JDBC vector store record collections.
 */
public interface JDBCVectorStoreRecordCollectionFactory {

    /**
     * Creates a new JDBC vector store record collection.
     *
     * @param dataSource       The JDBC data source.
     * @param collectionName   The name of the collection.
     * @param recordClass      The class type of the
     * @param recordDefinition The record definition.
     * @return The new JDBC vector store record collection.
     */
    <Record> JDBCVectorStoreRecordCollection<Record> createVectorStoreRecordCollection(
        DataSource dataSource,
        String collectionName,
        Class<Record> recordClass,
        VectorStoreRecordDefinition recordDefinition);
}
