// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.aiservices.openai.textembedding;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.models.EmbeddingItem;
import com.azure.ai.openai.models.Embeddings;
import com.azure.ai.openai.models.EmbeddingsOptions;
import com.microsoft.semantickernel.aiservices.openai.OpenAiService;
import com.microsoft.semantickernel.exceptions.AIException;
import com.microsoft.semantickernel.services.openai.OpenAiServiceBuilder;
import com.microsoft.semantickernel.services.textcompletion.TextGenerationService;
import com.microsoft.semantickernel.services.textembedding.Embedding;
import com.microsoft.semantickernel.services.textembedding.TextEmbeddingGenerationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * An OpenAI implementation of a {@link TextEmbeddingGenerationService}.
 *
 */
public class OpenAITextEmbeddingGenerationService extends OpenAiService<OpenAIAsyncClient>
    implements TextEmbeddingGenerationService {
    private static final Logger LOGGER = LoggerFactory
        .getLogger(OpenAITextEmbeddingGenerationService.class);
    private static final int DEFAULT_DIMENSIONS = 1536;
    private final int dimensions;

    /**
     * Creates a new {@link OpenAITextEmbeddingGenerationService}.
     *
     * @param client OpenAI client
     * @param deploymentName deployment name
     * @param modelId OpenAI model id
     * @param serviceId Service id
     */
    public OpenAITextEmbeddingGenerationService(
        OpenAIAsyncClient client,
        String deploymentName,
        String modelId,
        @Nullable String serviceId,
        int dimensions) {
        super(client, serviceId, modelId, deploymentName);
        this.dimensions = dimensions;
    }

    /**
     * Creates a builder for creating a {@link OpenAITextEmbeddingGenerationService}.
     *
     * @return A new {@link OpenAITextEmbeddingGenerationService} builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Generates embeddings for the given data.
     *
     * @param data The data to generate embeddings for.
     * @return A Mono that completes with the embeddings.
     */
    @Override
    public Mono<List<Embedding>> generateEmbeddingsAsync(List<String> data) {
        return this.internalGenerateTextEmbeddingsAsync(data);
    }

    protected Mono<List<Embedding>> internalGenerateTextEmbeddingsAsync(List<String> data) {
        EmbeddingsOptions options = new EmbeddingsOptions(data)
            .setModel(getModelId())
            .setDimensions(dimensions)
            .setInputType("string");

        return getClient()
            .getEmbeddings(getModelId(), options)
            .flatMapIterable(Embeddings::getData)
            .mapNotNull(EmbeddingItem::getEmbedding)
            .map(ArrayList::new)
            .mapNotNull(Embedding::new)
            .collectList();
    }

    /**
     * A builder for creating a {@link OpenAITextEmbeddingGenerationService}.
     */
    public static class Builder extends
        OpenAiServiceBuilder<OpenAIAsyncClient, OpenAITextEmbeddingGenerationService, OpenAITextEmbeddingGenerationService.Builder> {
        private int dimensions = DEFAULT_DIMENSIONS;

        /**
         * Sets the dimensions for the embeddings.
         *
         * @param dimensions The dimensions for the embeddings.
         * @return The builder.
         */
        public Builder withDimensions(int dimensions) {
            this.dimensions = dimensions;
            return this;
        }

        @Override
        public OpenAITextEmbeddingGenerationService build() {
            if (this.client == null) {
                throw new AIException(AIException.ErrorCodes.INVALID_REQUEST,
                    "OpenAI client must be provided");
            }

            if (this.modelId == null || modelId.isEmpty()) {
                throw new AIException(AIException.ErrorCodes.INVALID_REQUEST,
                    "OpenAI model id must be provided");
            }

            if (deploymentName == null) {
                LOGGER.debug("Deployment name is not provided, using model id as deployment name");
                deploymentName = modelId;
            }

            return new OpenAITextEmbeddingGenerationService(client, deploymentName, modelId,
                serviceId, dimensions);
        }
    }
}
