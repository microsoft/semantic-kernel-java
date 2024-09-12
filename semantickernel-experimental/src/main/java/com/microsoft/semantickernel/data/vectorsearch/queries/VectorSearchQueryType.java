// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.vectorsearch.queries;

public enum VectorSearchQueryType {

    VECTORIZED_SEARCH_QUERY("VectorizedSearchQuery"), VECTORIZABLE_TEXT_SEARCH_QUERY(
        "VectorizableTextSearchQuery"), HYBRID_TEXT_VECTORIZED_SEARCH_QUERY(
            "HybridTextVectorizedSearchQuery"), HYBRID_VECTORIZABLE_TEXT_SEARCH_QUERY(
                "HybridVectorizableTextSearchQuery");

    private final String value;

    VectorSearchQueryType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
