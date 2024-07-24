// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data;

import com.microsoft.semantickernel.data.VectorStoreRecordCollection;
import com.microsoft.semantickernel.data.recorddefinition.VectorStoreRecordDefinition;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Represents a vector store.
 *
 * @param <RecordCollection> The type of the record collection.
 */
public interface VectorStore<RecordCollection extends VectorStoreRecordCollection<?, ?>> {

    /**
     * Gets a collection from the vector store.
     *
     * @param collectionName The name of the collection.
     * @param recordClass The class type of the record.
     * @param recordDefinition The record definition.
     * @return The collection.
     */
    <Key, Record> RecordCollection getCollection(
        @Nonnull String collectionName,
        @Nonnull Class<Record> recordClass,
        @Nullable VectorStoreRecordDefinition recordDefinition);

    /**
     * Gets the names of all collections in the vector store.
     *
     * @return A list of collection names.
     */
    Mono<List<String>> getCollectionNamesAsync();
}
