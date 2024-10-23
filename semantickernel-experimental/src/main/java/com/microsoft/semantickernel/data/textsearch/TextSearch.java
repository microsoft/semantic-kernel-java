// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.textsearch;

import reactor.core.publisher.Mono;

public interface TextSearch {

    /**
     * Perform a search for content related to the specified query and return String values representing the search results.
     *
     * @param query   The text to search for.
     * @param options The search options.
     * @return The search results.
     */
    Mono<KernelSearchResults<String>> searchAsync(String query, TextSearchOptions options);

    /**
     * Perform a search for content related to the specified query and return TextSearchResult values representing the search results.
     *
     * @param query   The text to search for.
     * @param options The search options.
     * @return The search results.
     */
    Mono<KernelSearchResults<TextSearchResult>> getTextSearchResultsAsync(String query,
        TextSearchOptions options);

    /**
     * Perform a search for content related to the specified query and return Object values representing the search results.
     *
     * @param query   The text to search for.
     * @param options The search options.
     * @return The search results.
     */
    Mono<KernelSearchResults<Object>> getSearchResultsAsync(String query,
        TextSearchOptions options);
}
