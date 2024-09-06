// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.vectorsearch.queries;

import com.microsoft.semantickernel.data.record.options.VectorSearchOptions;

public class VectorSearchQuery {

    private final VectorSearchQueryType queryType;
    private final Object searchOptions;

    public VectorSearchQuery(VectorSearchQueryType queryType, Object searchOptions) {
        this.queryType = queryType;
        this.searchOptions = searchOptions;
    }

    public VectorSearchQueryType getQueryType() {
        return queryType;
    }

    public Object getSearchOptions() {
        return searchOptions;
    }

    public static <Vector> VectorizedSearchQuery<Vector> createQuery(Vector vector,
        VectorSearchOptions options) {
        return new VectorizedSearchQuery<>(vector, options);
    }

    public static VectorizableTextSearchQuery createQuery(String searchText,
        VectorSearchOptions options) {
        return new VectorizableTextSearchQuery(searchText, options);
    }
}
