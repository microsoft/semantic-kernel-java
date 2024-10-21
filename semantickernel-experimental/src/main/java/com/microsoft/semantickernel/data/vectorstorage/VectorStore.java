// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.vectorstorage;

import java.util.List;
import javax.annotation.Nonnull;

import reactor.core.publisher.Mono;

/**
 * Represents a vector store.
 */
public interface VectorStore {

    /**
     * Gets a collection from the vector store.
     *
     * @param collectionName   The name of the collection.
     * @param options          The options for the collection.
     * @param <Key>            The key type.
     * @param <Record>         The record type.
     * @return The collection.
     */
    <Key, Record> VectorStoreRecordCollection<Key, Record> getCollection(
        @Nonnull String collectionName,
        @Nonnull VectorStoreRecordCollectionOptions<Key, Record> options);

    /**
     * Gets the names of all collections in the vector store.
     *
     * @return A list of collection names.
     */
    Mono<List<String>> getCollectionNamesAsync();
}
