// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.vectorstorage;

import com.microsoft.semantickernel.data.vectorstorage.options.DeleteRecordOptions;
import com.microsoft.semantickernel.data.vectorstorage.options.GetRecordOptions;
import com.microsoft.semantickernel.data.vectorstorage.options.UpsertRecordOptions;
import java.util.List;
import reactor.core.publisher.Mono;

public interface VectorStoreRecordCollection<Key, Record> {

    /**
     * Gets the name of the collection.
     *
     * @return The name of the collection.
     */
    String getCollectionName();

    /**
     * Checks if the collection exists in the store.
     *
     * @return A Mono emitting a boolean indicating if the collection exists.
     */
    Mono<Boolean> collectionExistsAsync();

    /**
     * Creates the collection in the store.
     *
     * @return A Mono representing the completion of the creation operation.
     */
    Mono<VectorStoreRecordCollection<Key, Record>> createCollectionAsync();

    /**
     * Creates the collection in the store if it does not exist.
     *
     * @return A Mono representing the completion of the creation operation.
     */
    Mono<VectorStoreRecordCollection<Key, Record>> createCollectionIfNotExistsAsync();

    /**
     * Deletes the collection from the store.
     *
     * @return A Mono representing the completion of the deletion operation.
     */
    Mono<Void> deleteCollectionAsync();

    /**
     * Gets a record from the store.
     *
     * @param key     The key of the record to get.
     * @param options The options for getting the record.
     * @return A Mono emitting the record.
     */
    Mono<Record> getAsync(Key key, GetRecordOptions options);

    /**
     * Gets a batch of records from the store.
     *
     * @param keys    The keys of the records to get.
     * @param options The options for getting the records.
     * @return A Mono emitting a list of records.
     */
    Mono<List<Record>> getBatchAsync(List<Key> keys, GetRecordOptions options);

    /**
     * Inserts or updates a record in the store.
     *
     * @param data    The record to upsert.
     * @param options The options for upserting the record.
     * @return A Mono emitting the key of the upserted record.
     */
    Mono<Key> upsertAsync(Record data, UpsertRecordOptions options);

    /**
     * Inserts or updates a batch of records in the store.
     *
     * @param data    The records to upsert.
     * @param options The options for upserting the records.
     * @return A Mono emitting a list of keys of the upserted records.
     */
    Mono<List<Key>> upsertBatchAsync(List<Record> data, UpsertRecordOptions options);

    /**
     * Deletes a record from the store.
     *
     * @param key     The key of the record to delete.
     * @param options The options for deleting the record.
     * @return A Mono representing the completion of the deletion operation.
     */
    Mono<Void> deleteAsync(Key key, DeleteRecordOptions options);

    /**
     * Deletes a batch of records from the store.
     *
     * @param keys    The keys of the records to delete.
     * @param options The options for deleting the records.
     * @return A Mono representing the completion of the deletion operation.
     */
    Mono<Void> deleteBatchAsync(List<Key> keys, DeleteRecordOptions options);
}
