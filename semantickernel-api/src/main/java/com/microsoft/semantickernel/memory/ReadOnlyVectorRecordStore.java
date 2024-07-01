// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.memory;

import com.microsoft.semantickernel.memory.recordoptions.DeleteRecordOptions;
import com.microsoft.semantickernel.memory.recordoptions.UpsertRecordOptions;
import reactor.core.publisher.Mono;

import java.util.Collection;

interface ReadOnlyVectorRecordStore<Key, Record> extends VectorRecordStore<Key, Record> {

    default Mono<Key> upsertAsync(Record data, UpsertRecordOptions options) {
        throw new UnsupportedOperationException();
    }

    default Mono<Collection<Key>> upsertBatchAsync(Collection<Record> data,
        UpsertRecordOptions options) {
        throw new UnsupportedOperationException();
    }

    default Mono<Void> deleteAsync(Key key, DeleteRecordOptions options) {
        throw new UnsupportedOperationException();
    }

    default Mono<Void> deleteBatchAsync(Collection<Key> keys, DeleteRecordOptions options) {
        throw new UnsupportedOperationException();
    }
}
