// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.vectorsearch;

import com.microsoft.semantickernel.data.vectorstorage.options.VectorSearchOptions;
import reactor.core.publisher.Mono;

/**
 * A vectorizable text search.
 *
 * @param <Record> The record type.
 */
public interface VectorizableTextSearch<Record> {
    /**
     * Vectorizable text search. This method searches for records that are similar to the given text.
     *
     * @param searchText The text to search with.
     * @param options The options to use for the search.
     * @return VectorSearchResults.
     */
    Mono<VectorSearchResults<Record>> searchAsync(String searchText,
        VectorSearchOptions options);
}
