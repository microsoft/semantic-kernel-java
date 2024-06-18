package com.microsoft.semantickernel.memory;

import com.microsoft.semantickernel.memory.recordoptions.DeleteRecordOptions;
import com.microsoft.semantickernel.memory.recordoptions.GetRecordOptions;
import com.microsoft.semantickernel.memory.recordoptions.UpsertRecordOptions;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface VectorRecordStore<Key, Record> {
    Mono<Record> getAsync(Key key, GetRecordOptions options);
    Mono<Collection<Record>> getBatchAsync(Collection<Key> keys, GetRecordOptions options);
    Mono<Key> upsertAsync(Record data, UpsertRecordOptions options);
    Mono<Collection<Key>> upsertBatchAsync(Collection<Record> data, UpsertRecordOptions options);
    Mono<Void> deleteAsync(Key key, DeleteRecordOptions options);
    Mono<Void> deleteBatchAsync(Iterable<Key> keys, DeleteRecordOptions options);
}
