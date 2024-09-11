// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.vectorsearch;

import com.microsoft.semantickernel.data.vectorsearch.queries.VectorSearchQuery;
import reactor.core.publisher.Mono;

import java.util.List;

public interface VectorSearch<Record> {

    Mono<List<VectorSearchResult<Record>>> searchAsync(VectorSearchQuery query);
}
