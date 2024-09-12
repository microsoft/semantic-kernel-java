// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.vectorsearch;

import com.microsoft.semantickernel.data.vectorstorage.options.VectorSearchOptions;
import reactor.core.publisher.Mono;

import java.util.List;

public interface VectorizableSearch<Record> extends VectorSearch<Record> {
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
