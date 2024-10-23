// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.textsearch;

/**
 * Default implementation of {@link TextSearchResultMapper}.
 */
public class DefaultTextSearchResultMapper implements TextSearchResultMapper {
    /**
     * Maps a search result to a {@link TextSearchResult}.
     *
     * @param result The search result.
     * @return The {@link TextSearchResult}.
     */
    @Override
    public TextSearchResult fromResultToTextSearchResult(Object result) {
        return TextSearchResult.fromRecord(result);
    }
}
