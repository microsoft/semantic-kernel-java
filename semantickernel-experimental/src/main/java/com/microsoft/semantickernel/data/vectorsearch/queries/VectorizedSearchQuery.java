// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.vectorsearch.queries;

import com.microsoft.semantickernel.data.record.options.VectorSearchOptions;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class VectorizedSearchQuery extends VectorSearchQuery {

    private final List<Float> vector;
    @Nullable
    private final VectorSearchOptions searchOptions;

    public VectorizedSearchQuery(List<Float> vector, VectorSearchOptions searchOptions) {
        super(VectorSearchQueryType.VECTORIZED_SEARCH_QUERY, null);

        this.vector = Collections.unmodifiableList(vector);
        this.searchOptions = searchOptions;
    }

    public List<Float> getVector() {
        return vector;
    }

    public VectorSearchOptions getSearchOptions() {
        return searchOptions;
    }
}
