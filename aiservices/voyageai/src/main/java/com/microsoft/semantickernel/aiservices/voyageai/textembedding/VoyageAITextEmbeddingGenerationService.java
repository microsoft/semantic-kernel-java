// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.aiservices.voyageai.textembedding;

import com.microsoft.semantickernel.aiservices.voyageai.core.VoyageAIClient;
import com.microsoft.semantickernel.aiservices.voyageai.core.VoyageAIModels;
import com.microsoft.semantickernel.orchestration.PromptExecutionSettings;
import com.microsoft.semantickernel.services.textembedding.Embedding;
import com.microsoft.semantickernel.services.textembedding.TextEmbeddingGenerationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * VoyageAI implementation of {@link TextEmbeddingGenerationService}.
 * Supports models like voyage-3-large, voyage-3.5, voyage-code-3, voyage-finance-2, voyage-law-2.
 */
public class VoyageAITextEmbeddingGenerationService implements TextEmbeddingGenerationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(VoyageAITextEmbeddingGenerationService.class);

    private final VoyageAIClient client;
    private final String modelId;
    private final String serviceId;

    /**
     * Creates a new instance of VoyageAI text embedding generation service.
     *
     * @param client    VoyageAI client
     * @param modelId   Model ID (e.g., "voyage-3-large")
     * @param serviceId Optional service ID
     */
    public VoyageAITextEmbeddingGenerationService(
        VoyageAIClient client,
        String modelId,
        @Nullable String serviceId) {

        if (client == null) {
            throw new IllegalArgumentException("Client cannot be null");
        }
        if (modelId == null || modelId.trim().isEmpty()) {
            throw new IllegalArgumentException("Model ID cannot be null or empty");
        }

        this.client = client;
        this.modelId = modelId;
        this.serviceId = serviceId != null ? serviceId : PromptExecutionSettings.DEFAULT_SERVICE_ID;
    }

    @Override
    public String getServiceId() {
        return serviceId;
    }

    @Override
    public String getModelId() {
        return modelId;
    }

    /**
     * Generates embeddings for the given text.
     *
     * @param data The text to generate embeddings for
     * @return A Mono that completes with the embedding
     */
    @Override
    public Mono<Embedding> generateEmbeddingAsync(String data) {
        return generateEmbeddingsAsync(Arrays.asList(data))
            .flatMap(embeddings -> {
                if (embeddings.isEmpty()) {
                    return Mono.empty();
                }
                return Mono.just(embeddings.get(0));
            });
    }

    /**
     * Generates embeddings for the given texts.
     *
     * @param data The texts to generate embeddings for
     * @return A Mono that completes with the list of embeddings
     */
    @Override
    public Mono<List<Embedding>> generateEmbeddingsAsync(List<String> data) {
        if (data == null || data.isEmpty()) {
            return Mono.just(Collections.emptyList());
        }

        LOGGER.debug("Generating embeddings for {} texts using model {}", data.size(), modelId);

        VoyageAIModels.EmbeddingRequest request = new VoyageAIModels.EmbeddingRequest();
        request.setInput(data);
        request.setModel(modelId);
        request.setTruncation(true);

        return client.sendRequestAsync("embeddings", request, VoyageAIModels.EmbeddingResponse.class)
            .map(response -> {
                LOGGER.debug("Received {} embeddings from VoyageAI", response.getData().size());

                List<Embedding> embeddings = response.getData().stream()
                    .sorted(Comparator.comparingInt(VoyageAIModels.EmbeddingDataItem::getIndex))
                    .map(item -> new Embedding(item.getEmbedding()))
                    .collect(Collectors.toList());

                return embeddings;
            });
    }

    /**
     * Creates a builder for VoyageAI text embedding generation service.
     *
     * @return A new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for {@link VoyageAITextEmbeddingGenerationService}.
     */
    public static class Builder {
        private VoyageAIClient client;
        private String modelId;
        private String serviceId;

        /**
         * Sets the VoyageAI client.
         *
         * @param client VoyageAI client
         * @return This builder
         */
        public Builder withClient(VoyageAIClient client) {
            this.client = client;
            return this;
        }

        /**
         * Sets the model ID.
         *
         * @param modelId Model ID (e.g., "voyage-3-large")
         * @return This builder
         */
        public Builder withModelId(String modelId) {
            this.modelId = modelId;
            return this;
        }

        /**
         * Sets the service ID.
         *
         * @param serviceId Service ID
         * @return This builder
         */
        public Builder withServiceId(String serviceId) {
            this.serviceId = serviceId;
            return this;
        }

        /**
         * Builds the VoyageAI text embedding generation service.
         *
         * @return A new instance of VoyageAITextEmbeddingGenerationService
         */
        public VoyageAITextEmbeddingGenerationService build() {
            return new VoyageAITextEmbeddingGenerationService(client, modelId, serviceId);
        }
    }
}
