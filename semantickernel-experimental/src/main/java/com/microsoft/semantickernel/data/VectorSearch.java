// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data;

import com.microsoft.semantickernel.data.record.options.VectorSearchOptions;
import com.microsoft.semantickernel.data.vectorsearch.queries.VectorSearchQuery;
import reactor.core.publisher.Mono;

import java.util.List;

public abstract class VectorSearch<Record> {

    /**
     * Search the vector store for records that match the given embedding and filter.
     *
     * @param query The vector search query.
     * @return A list of search results.
     */
    public abstract Mono<List<VectorSearchResult<Record>>> searchAsync(VectorSearchQuery query);

    /**
     * Vectorized search. This method searches for records that are similar to the given vector.
     *
     * @param vector The vector to search with.
     * @param options The options to use for the search.
     * @return A list of search results.
     */
    public Mono<List<VectorSearchResult<Record>>> searchAsync(List<Float> vector,
        VectorSearchOptions options) {
        return searchAsync(VectorSearchQuery.createQuery(vector, options));
    }

    /**
     * Vectorizable text search. This method searches for records that are similar to the given text.
     *
     * @param searchText The text to search with.
     * @param options The options to use for the search.
     * @return A list of search results.
     */
    public Mono<List<VectorSearchResult<Record>>> searchAsync(String searchText,
        VectorSearchOptions options) {
        return searchAsync(VectorSearchQuery.createQuery(searchText, options));
    }
}
