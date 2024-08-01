// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.data.jdbc;

import java.sql.Connection;

/**
 * Factory for creating JDBC vector store record collections.
 */
public interface JDBCVectorStoreRecordCollectionFactory {
    /**
     * Creates a new JDBC vector store record collection.
     *
     * @param options The options for the collection.
     * @return The new JDBC vector store record collection.
     */
    <Record> JDBCVectorStoreRecordCollection<Record> createVectorStoreRecordCollection(
        Connection connection,
        String collectionName,
        JDBCVectorStoreRecordCollectionOptions<Record> options);
}
