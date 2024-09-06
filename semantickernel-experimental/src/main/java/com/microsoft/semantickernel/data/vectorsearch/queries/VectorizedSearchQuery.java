// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.vectorsearch.queries;

import com.microsoft.semantickernel.data.record.options.VectorSearchOptions;

import javax.annotation.Nullable;

public class VectorizedSearchQuery<Vector> extends VectorSearchQuery {

    private final Vector vector;
    @Nullable
    private final VectorSearchOptions searchOptions;

    public VectorizedSearchQuery(Vector vector, VectorSearchOptions searchOptions) {
        super(VectorSearchQueryType.VECTORIZED_SEARCH_QUERY, null);

        this.vector = vector;
        this.searchOptions = searchOptions;
    }

    public Vector getVector() {
        return vector;
    }

    public VectorSearchOptions getSearchOptions() {
        return searchOptions;
    }
}
