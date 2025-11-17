// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.services.reranking;

import com.microsoft.semantickernel.services.AIService;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Interface for text reranking services that can reorder documents based on relevance to a query.
 */
public interface TextRerankingService extends AIService {

    /**
     * Reranks a list of documents based on their relevance to a query.
     *
     * @param query     The query to rank documents against
     * @param documents The list of documents to rerank
     * @return A Mono containing a list of {@link RerankResult} sorted by relevance score in descending order
     */
    Mono<List<RerankResult>> rerankAsync(String query, List<String> documents);
}
