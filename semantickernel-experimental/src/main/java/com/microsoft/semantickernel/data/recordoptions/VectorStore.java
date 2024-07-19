// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.recordoptions;

import com.microsoft.semantickernel.data.VectorStoreRecordCollection;
import reactor.core.publisher.Mono;

import java.util.List;

public interface VectorStore<Key, Record> {
    public VectorStoreRecordCollection<Key, Record> getCollection(String collectionName);

    public Mono<List<String>> listCollectionNamesAsync();
}
