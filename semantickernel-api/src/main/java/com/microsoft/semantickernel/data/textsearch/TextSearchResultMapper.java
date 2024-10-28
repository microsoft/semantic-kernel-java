// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.textsearch;

/**
 * Maps a search result to a {@link TextSearchResult}.
 */
public interface TextSearchResultMapper {
    /**
     * Maps a search result to a {@link TextSearchResult}.
     *
     * @param result The search result.
     * @return The {@link TextSearchResult}.
     */
    TextSearchResult fromResultToTextSearchResult(Object result);
}
