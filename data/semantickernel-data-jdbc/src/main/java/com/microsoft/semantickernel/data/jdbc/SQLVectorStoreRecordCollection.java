// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.jdbc;

import com.microsoft.semantickernel.data.vectorstorage.VectorStoreRecordCollection;
import reactor.core.publisher.Mono;

/**
 * Represents a SQL vector store record collection.
 *
 * @param <Key>    the key type
 * @param <Record> the record type
 */
public interface SQLVectorStoreRecordCollection<Key, Record>
    extends VectorStoreRecordCollection<Key, Record> {

    /**
     * Prepares the vector store record collection.
     *
     * @return A {@link Mono} that completes when the vector store record collection is prepared to be used.
     */
    Mono<Void> prepareAsync();
}
