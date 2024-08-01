// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.data.jdbc;

import com.microsoft.semantickernel.data.VectorStore;
import com.microsoft.semantickernel.data.VectorStoreRecordCollection;
import reactor.core.publisher.Mono;

public interface SQLVectorStore<RecordCollection extends VectorStoreRecordCollection<?, ?>>
    extends VectorStore<RecordCollection> {

    /**
     * Prepares the vector store.
     *
     * @return A {@link Mono} that completes when the vector store is prepared to be used.
     */
    Mono<Void> prepareAsync();
}
