// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data;

import com.microsoft.semantickernel.data.VectorStoreRecordCollection;
import com.microsoft.semantickernel.data.recorddefinition.VectorStoreRecordDefinition;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Represents a vector store.
 *
 * @param <Key> The type of the key.
 * @param <Record> The type of the record.
 * @param <RecordCollection> The type of the record collection.
 */
public interface VectorStore<Key, Record, RecordCollection extends VectorStoreRecordCollection<Key, Record>> {

    /**
     * Gets a collection from the vector store.
     *
     * @param collectionName The name of the collection.
     * @param recordDefinition The record definition.
     * @return The collection.
     */
    RecordCollection getCollection(@Nonnull String collectionName,
        VectorStoreRecordDefinition recordDefinition);

    /**
     * Gets the names of all collections in the vector store.
     *
     * @return A list of collection names.
     */
    Mono<List<String>> getCollectionNamesAsync();
}
