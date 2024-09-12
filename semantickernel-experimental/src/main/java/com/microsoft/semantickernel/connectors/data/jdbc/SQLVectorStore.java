// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.data.jdbc;

import com.microsoft.semantickernel.data.vectorstorage.VectorStore;
import reactor.core.publisher.Mono;

public interface SQLVectorStore
    extends VectorStore {

    /**
     * Prepares the vector store.
     *
     * @return A {@link Mono} that completes when the vector store is prepared to be used.
     */
    Mono<Void> prepareAsync();
}
