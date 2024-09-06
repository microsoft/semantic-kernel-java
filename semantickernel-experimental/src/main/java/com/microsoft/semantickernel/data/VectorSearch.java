// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data;

import com.microsoft.semantickernel.data.record.options.VectorSearchOptions;
import reactor.core.publisher.Mono;

import java.util.List;

public interface VectorSearch<Record> {
    /**
     * Vectorized search. This method searches for records that are similar to the given vector.
     *
     * @param vector The vector to search with.
     * @param options The options to use for the search.
     * @return A list of search results.
     */
    <Vector> Mono<List<VectorSearchResult<Record>>> searchAsync(Vector vector,
        VectorSearchOptions options);

    /**
     * Vectorizable text search. This method searches for records that are similar to the given text.
     *
     * @param searchText The text to search with.
     * @param options The options to use for the search.
     * @return A list of search results.
     */
    Mono<List<VectorSearchResult<Record>>> searchAsync(String searchText,
        VectorSearchOptions options);
}
