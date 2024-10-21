// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.services.textembedding;

import com.microsoft.semantickernel.services.AIService;
import java.util.List;
import reactor.core.publisher.Mono;

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

    /**
     * Generates an embedding associated to the data
     *
     * @param data Text to generate embedding for
     * @return Embedding of the data
     */

    Mono<Embedding> generateEmbeddingAsync(TValue data);

}
