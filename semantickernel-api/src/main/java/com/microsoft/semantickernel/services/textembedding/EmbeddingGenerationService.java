// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.services.textembedding;

import com.microsoft.semantickernel.builders.SemanticKernelBuilder;
import com.microsoft.semantickernel.services.AIService;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 *  Interface for text embedding generation services 
 * @param <TValue> The type of the data to generate embeddings for
 */
public interface EmbeddingGenerationService<TValue> extends AIService {
    /**
     * Generates a list of embeddings associated to the data
     *
     * @param data List of texts to generate embeddings for
     * @return List of embeddings of each data point
     */
    Mono<List<Embedding>> generateEmbeddingsAsync(List<TValue> data);
}
