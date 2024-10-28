// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.textsearch;

/**
 * Maps a search result to a string.
 */
public interface TextSearchStringMapper {
    /**
     * Maps a search result to a string.
     *
     * @param result The search result.
     * @return The string.
     */
    String fromResultToString(Object result);
}
