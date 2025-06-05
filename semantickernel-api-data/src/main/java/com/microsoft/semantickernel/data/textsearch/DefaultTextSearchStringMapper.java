// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.textsearch;

/**
 * Default implementation of {@link TextSearchStringMapper}.
 */
public class DefaultTextSearchStringMapper implements TextSearchStringMapper {
    /**
     * Maps a search result to a string.
     *
     * @param result The search result.
     * @return The string.
     */
    @Override
    public String fromResultToString(Object result) {
        TextSearchResult textSearchResult = TextSearchResult.fromRecord(result);
        return textSearchResult.getValue();
    }
}
