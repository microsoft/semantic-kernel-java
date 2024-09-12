// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;

import com.microsoft.semantickernel.data.vectorstorage.VectorStore;
import com.microsoft.semantickernel.data.vectorstorage.VectorStoreRecordCollection;
import com.microsoft.semantickernel.data.vectorstorage.VectorStoreRecordCollectionOptions;
import com.microsoft.semantickernel.exceptions.SKException;
import reactor.core.publisher.Mono;

public class VolatileVectorStore implements VectorStore {

    private final Map<String, Map<String, ?>> collections;

    public VolatileVectorStore() {
        this.collections = new ConcurrentHashMap<>();
    }

    /**
     * Gets a collection from the vector store.
     *
     * @param collectionName The name of the collection.
     * @param options        The options for the collection.
     * @return The collection.
     */
    @Override
    public <Key, Record> VectorStoreRecordCollection<Key, Record> getCollection(
        @Nonnull String collectionName,
        @Nonnull VectorStoreRecordCollectionOptions<Key, Record> options) {
        if (options.getKeyClass() != String.class) {
            throw new SKException("Volatile only supports string keys");
        }
        if (options.getRecordClass() == null) {
            throw new SKException("Record class is required");
        }

        return (VectorStoreRecordCollection<Key, Record>) new VolatileVectorStoreRecordCollection<>(
            collectionName,
            collections,
            (VolatileVectorStoreRecordCollectionOptions<Record>) options);
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
