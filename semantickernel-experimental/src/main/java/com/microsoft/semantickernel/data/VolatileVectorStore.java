// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data;

import com.microsoft.semantickernel.data.recorddefinition.VectorStoreRecordDefinition;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import reactor.core.publisher.Mono;

public class VolatileVectorStore implements VectorStore {

    private final Map<String, Map<String, ?>> collections;

    public VolatileVectorStore() {
        this.collections = new ConcurrentHashMap<>();
    }

    /**
     * Gets a collection from the vector store.
     *
     * @param collectionName   The name of the collection.
     * @param recordDefinition The record definition.
     * @return The collection.
     */
    @Override
    public <Key, Record> VectorStoreRecordCollection<Key, Record> getCollection(
        @Nonnull String collectionName,
        @Nonnull Class<Key> keyClass,
        @Nonnull Class<Record> recordClass,
        @Nullable VectorStoreRecordDefinition recordDefinition) {
        if (keyClass != String.class) {
            throw new IllegalArgumentException("Volatile only supports string keys");
        }

        return (VectorStoreRecordCollection<Key, Record>) getCollection(
            collectionName,
            recordClass,
            recordDefinition);
    }

    public <Record> VectorStoreRecordCollection<String, Record> getCollection(
        @Nonnull String collectionName,
        @Nonnull Class<Record> recordClass,
        @Nullable VectorStoreRecordDefinition recordDefinition) {
        return new VolatileVectorStoreRecordCollection<>(
            collectionName,
            collections,
            VolatileVectorStoreRecordCollectionOptions.<Record>builder()
                .withRecordClass(recordClass)
                .withRecordDefinition(recordDefinition)
                .build());
    }

    /**
     * Gets the names of all collections in the vector store.
     *
     * @return A list of collection names.
     */
    @Override
    public Mono<List<String>> getCollectionNamesAsync() {
        return Mono.just(new ArrayList<>(collections.keySet()));
    }
}
