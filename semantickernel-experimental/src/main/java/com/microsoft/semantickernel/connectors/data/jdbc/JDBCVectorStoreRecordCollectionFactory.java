// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.data.jdbc;

import javax.sql.DataSource;

/**
 * Factory for creating JDBC vector store record collections.
 */
public interface JDBCVectorStoreRecordCollectionFactory {
    /**
     * Creates a new JDBC vector store record collection.
     *
     * @param dataSource The data source.
     * @param collectionName The name of the collection.
     * @param options The options for the collection.
     * @param <Record> The type of record in the collection.
     * @return The new JDBC vector store record collection.
     */
    <Record> JDBCVectorStoreRecordCollection<Record> createVectorStoreRecordCollection(
        DataSource dataSource,
        String collectionName,
        JDBCVectorStoreRecordCollectionOptions<Record> options);
}
