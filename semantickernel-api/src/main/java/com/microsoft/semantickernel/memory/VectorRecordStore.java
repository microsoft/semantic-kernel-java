// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.memory;

import com.microsoft.semantickernel.memory.recordoptions.DeleteRecordOptions;
import com.microsoft.semantickernel.memory.recordoptions.GetRecordOptions;
import com.microsoft.semantickernel.memory.recordoptions.UpsertRecordOptions;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface VectorRecordStore<Key, Record> {
    /**
     * Gets a record from the store.
     *
     * @param key The key of the record to get.
     * @param options The options for getting the record.
     * @return A Mono emitting the record.
     */
    Mono<Record> getAsync(Key key, GetRecordOptions options);

    /**
     * Gets a batch of records from the store.
     *
     * @param keys The keys of the records to get.
     * @param options The options for getting the records.
     * @return A Mono emitting a collection of records.
     */
    Mono<Collection<Record>> getBatchAsync(Collection<Key> keys, GetRecordOptions options);

    /**
     * Inserts or updates a record in the store.
     *
     * @param data The record to upsert.
     * @param options The options for upserting the record.
     * @return A Mono emitting the key of the upserted record.
     */
    Mono<Key> upsertAsync(Record data, UpsertRecordOptions options);

    /**
     * Inserts or updates a batch of records in the store.
     *
     * @param data The records to upsert.
     * @param options The options for upserting the records.
     * @return A Mono emitting a collection of keys of the upserted records.
     */
    Mono<Collection<Key>> upsertBatchAsync(Collection<Record> data, UpsertRecordOptions options);

    /**
     * Deletes a record from the store.
     *
     * @param key The key of the record to delete.
     * @param options The options for deleting the record.
     * @return A Mono representing the completion of the deletion operation.
     */
    Mono<Void> deleteAsync(Key key, DeleteRecordOptions options);

    /**
     * Deletes a batch of records from the store.
     *
     * @param keys The keys of the records to delete.
     * @param options The options for deleting the records.
     * @return A Mono representing the completion of the deletion operation.
     */
    Mono<Void> deleteBatchAsync(Collection<Key> keys, DeleteRecordOptions options);
}
