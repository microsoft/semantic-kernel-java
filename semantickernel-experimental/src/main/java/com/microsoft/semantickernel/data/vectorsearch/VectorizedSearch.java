// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.vectorsearch;

import com.microsoft.semantickernel.data.vectorstorage.options.VectorSearchOptions;
import reactor.core.publisher.Mono;

import java.util.List;

public interface VectorizedSearch<Record> {

    /**
     * Vectorized search. This method searches for records that are similar to the given vector.
     *
     * @param vector The vector to search with.
     * @param options The options to use for the search.
     * @return A list of search results.
     */
    Mono<List<VectorSearchResult<Record>>> searchAsync(List<Float> vector,
        VectorSearchOptions options);
}
