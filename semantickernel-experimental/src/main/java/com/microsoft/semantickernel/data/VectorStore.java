// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data;

import com.microsoft.semantickernel.data.recorddefinition.VectorStoreRecordDefinition;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import reactor.core.publisher.Mono;

/**
 * Represents a vector store.
 */
public interface VectorStore {

    /**
     * Gets a collection from the vector store.
     *
     * @param collectionName   The name of the collection.
     * @param recordClass      The class type of the record.
     * @param recordDefinition The record definition.
     * @return The collection.
     */
    <Key, Record> VectorStoreRecordCollection<Key, Record> getCollection(
        @Nonnull String collectionName,
        @Nonnull Class<Key> keyClass,
        @Nonnull Class<Record> recordClass,
        @Nullable VectorStoreRecordDefinition recordDefinition);

    /**
     * Gets the names of all collections in the vector store.
     *
     * @return A list of collection names.
     */
    Mono<List<String>> getCollectionNamesAsync();
}
