// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.vectorsearch.queries;

import com.microsoft.semantickernel.data.vectorsearch.options.VectorSearchOptions;

import javax.annotation.Nullable;

public class VectorizableTextSearchQuery extends VectorSearchQuery {

    private final String queryText;

    @Nullable
    private final VectorSearchOptions searchOptions;

    public VectorizableTextSearchQuery(String queryText,
        @Nullable VectorSearchOptions searchOptions) {
        super(VectorSearchQueryType.VECTORIZABLE_TEXT_SEARCH_QUERY, searchOptions);
        this.queryText = queryText;
        this.searchOptions = searchOptions;
    }

    public String getQueryText() {
        return queryText;
    }

    @Nullable
    public VectorSearchOptions getSearchOptions() {
        return searchOptions;
    }
}
