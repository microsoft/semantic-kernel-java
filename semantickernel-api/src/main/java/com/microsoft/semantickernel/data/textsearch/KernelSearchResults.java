// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.textsearch;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The search results.
 *
 * @param <T> The type of the search results.
 */
public class KernelSearchResults<T> {
    private final List<T> results;
    private final long totalCount;
    private final Map<String, Object> metadata;

    /**
     * Creates a new instance of the KernelSearchResults class.
     *
     * @param results The search results.
     */
    public KernelSearchResults(List<T> results) {
        this(results, results.size(), Collections.emptyMap());
    }

    /**
     * Creates a new instance of the KernelSearchResults class.
     *
     * @param results    The search results.
     * @param totalCount  The total count of search results.
     * @param metadata The metadata.
     */
    public KernelSearchResults(List<T> results, long totalCount,
        Map<String, Object> metadata) {
        this.results = Collections.unmodifiableList(results);
        this.totalCount = totalCount;
        this.metadata = Collections.unmodifiableMap(metadata);
    }

    /**
     * Gets the total count of search results.
     * This value represents the total number of results that are available for the current query and not the number of results being returned.
     *
     * @return The total count of search results.
     */
    public long getTotalCount() {
        return totalCount;
    }

    /**
     * Gets the search results.
     *
     * @return The search results.
     */
    public List<T> getResults() {
        return results;
    }

    /**
     * Gets the metadata associated with the search results.
     *
     * @return The metadata.
     */
    public Map<String, Object> getMetadata() {
        return metadata;
    }

}
