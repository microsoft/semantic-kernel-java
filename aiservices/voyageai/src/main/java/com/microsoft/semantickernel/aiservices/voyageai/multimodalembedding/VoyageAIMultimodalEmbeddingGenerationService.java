// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.aiservices.voyageai.multimodalembedding;

import com.microsoft.semantickernel.aiservices.voyageai.core.VoyageAIClient;
import com.microsoft.semantickernel.aiservices.voyageai.core.VoyageAIModels;
import com.microsoft.semantickernel.orchestration.PromptExecutionSettings;
import com.microsoft.semantickernel.services.textembedding.Embedding;
import com.microsoft.semantickernel.services.textembedding.TextEmbeddingGenerationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * VoyageAI multimodal embedding generation service.
 * Generates embeddings for text, images, or interleaved text and images.
 * Supports the voyage-multimodal-3 model.
 * <p>
 * Constraints:
 * - Maximum 1,000 inputs per request
 * - Images: ≤16 million pixels, ≤20 MB
 * - Total tokens per input: ≤32,000 (560 pixels = 1 token)
 * - Aggregate tokens across inputs: ≤320,000
 */
public class VoyageAIMultimodalEmbeddingGenerationService implements TextEmbeddingGenerationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(VoyageAIMultimodalEmbeddingGenerationService.class);

    private final VoyageAIClient client;
    private final String modelId;
    private final String serviceId;

    /**
     * Creates a new instance of VoyageAI multimodal embedding generation service.
     *
     * @param client    VoyageAI client
     * @param modelId   Model ID (e.g., "voyage-multimodal-3")
     * @param serviceId Optional service ID
     */
    public VoyageAIMultimodalEmbeddingGenerationService(
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
     * Generates multimodal embeddings for text and/or images.
     *
     * @param inputs List of multimodal inputs
     * @return A Mono containing a list of multimodal embeddings
     */
    public Mono<List<Embedding>> generateMultimodalEmbeddingsAsync(List<VoyageAIModels.MultimodalInput> inputs) {
        if (inputs == null || inputs.isEmpty()) {
            return Mono.just(Collections.emptyList());
        }

        LOGGER.debug("Generating multimodal embeddings for {} inputs using model {}", inputs.size(), modelId);

        VoyageAIModels.MultimodalEmbeddingRequest request = new VoyageAIModels.MultimodalEmbeddingRequest();
        request.setInputs(inputs);
        request.setModel(modelId);

        return client.sendRequestAsync("multimodalembeddings", request, VoyageAIModels.MultimodalEmbeddingResponse.class)
            .map(response -> {
                LOGGER.debug("Received {} multimodal embeddings from VoyageAI", response.getData().size());

                List<Embedding> embeddings = response.getData().stream()
                    .sorted(Comparator.comparingInt(VoyageAIModels.EmbeddingDataItem::getIndex))
                    .map(item -> new Embedding(item.getEmbedding()))
                    .collect(Collectors.toList());

                return embeddings;
            });
    }

    /**
     * Generates embeddings for the given text.
     * For text-only input, converts to multimodal format.
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
     * Converts text-only inputs to multimodal format.
     *
     * @param data The texts to generate embeddings for
     * @return A Mono that completes with the list of embeddings
     */
    @Override
    public Mono<List<Embedding>> generateEmbeddingsAsync(List<String> data) {
        if (data == null || data.isEmpty()) {
            return Mono.just(Collections.emptyList());
        }

        // Convert each text to multimodal input format
        List<VoyageAIModels.MultimodalInput> inputs = new ArrayList<>();
        for (String text : data) {
            VoyageAIModels.MultimodalContentItem contentItem =
                new VoyageAIModels.MultimodalContentItem("text", text);
            VoyageAIModels.MultimodalInput input =
                new VoyageAIModels.MultimodalInput(Arrays.asList(contentItem));
            inputs.add(input);
        }

        if (inputs.isEmpty()) {
            return Mono.just(Collections.emptyList());
        }

        LOGGER.debug("Generating multimodal embeddings for {} inputs using model {}", inputs.size(), modelId);

        VoyageAIModels.MultimodalEmbeddingRequest request = new VoyageAIModels.MultimodalEmbeddingRequest();
        request.setInputs(inputs);
        request.setModel(modelId);

        return client.sendRequestAsync("multimodalembeddings", request, VoyageAIModels.MultimodalEmbeddingResponse.class)
            .map(response -> {
                LOGGER.debug("Received {} multimodal embeddings from VoyageAI", response.getData().size());

                List<Embedding> embeddings = response.getData().stream()
                    .sorted(Comparator.comparingInt(VoyageAIModels.EmbeddingDataItem::getIndex))
                    .map(item -> new Embedding(item.getEmbedding()))
                    .collect(Collectors.toList());

                return embeddings;
            });
    }

    /**
     * Creates a builder for VoyageAI multimodal embedding generation service.
     *
     * @return A new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for {@link VoyageAIMultimodalEmbeddingGenerationService}.
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
         * @param modelId Model ID (e.g., "voyage-multimodal-3")
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
         * Builds the VoyageAI multimodal embedding generation service.
         *
         * @return A new instance of VoyageAIMultimodalEmbeddingGenerationService
         */
        public VoyageAIMultimodalEmbeddingGenerationService build() {
            return new VoyageAIMultimodalEmbeddingGenerationService(client, modelId, serviceId);
        }
    }
}
