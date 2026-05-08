// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.aiservices.voyageai.contextualizedembedding;

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
 * VoyageAI contextualized embedding generation service.
 * Generates embeddings that capture both local chunk details and global document-level metadata.
 * Supports models like voyage-3.
 */
public class VoyageAIContextualizedEmbeddingGenerationService implements TextEmbeddingGenerationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(VoyageAIContextualizedEmbeddingGenerationService.class);

    private final VoyageAIClient client;
    private final String modelId;
    private final String serviceId;

    /**
     * Creates a new instance of VoyageAI contextualized embedding generation service.
     *
     * @param client    VoyageAI client
     * @param modelId   Model ID (e.g., "voyage-3")
     * @param serviceId Optional service ID
     */
    public VoyageAIContextualizedEmbeddingGenerationService(
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
     * Generates contextualized embeddings for document chunks.
     *
     * @param inputs List of lists where each inner list contains document chunks
     * @return A Mono containing a list of embeddings for all chunks across all documents
     */
    public Mono<List<Embedding>> generateContextualizedEmbeddingsAsync(List<List<String>> inputs) {
        if (inputs == null || inputs.isEmpty()) {
            return Mono.just(Collections.emptyList());
        }

        LOGGER.debug("Generating contextualized embeddings for {} document groups using model {}",
            inputs.size(), modelId);

        VoyageAIModels.ContextualizedEmbeddingRequest request =
            new VoyageAIModels.ContextualizedEmbeddingRequest();
        request.setInputs(inputs);
        request.setModel(modelId);

        return client.sendRequestAsync(
            "contextualizedembeddings",
            request,
            VoyageAIModels.ContextualizedEmbeddingResponse.class)
            .map(response -> {
                List<Embedding> embeddings = new ArrayList<>();
                // Parse nested data structure: {"data":[{"data":[{"embedding":[...]}]}]}
                for (VoyageAIModels.ContextualizedEmbeddingDataList dataList : response.getData()) {
                    for (VoyageAIModels.EmbeddingDataItem item : dataList.getData()) {
                        embeddings.add(new Embedding(item.getEmbedding()));
                    }
                }

                LOGGER.debug("Received {} contextualized embeddings from VoyageAI", embeddings.size());
                return embeddings;
            });
    }

    /**
     * Generates embeddings for the given text.
     * For standard text embedding, wraps the data as a single input.
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
     * Each text is treated as a separate document for contextualized embeddings.
     *
     * @param data The texts to generate embeddings for
     * @return A Mono that completes with the list of embeddings
     */
    @Override
    public Mono<List<Embedding>> generateEmbeddingsAsync(List<String> data) {
        if (data == null || data.isEmpty()) {
            return Mono.just(Collections.emptyList());
        }

        // Convert each string to a single-element list for contextualized embeddings
        List<List<String>> inputs = new ArrayList<>();
        for (String text : data) {
            inputs.add(Arrays.asList(text));
        }

        return generateContextualizedEmbeddingsAsync(inputs);
    }

    /**
     * Creates a builder for VoyageAI contextualized embedding generation service.
     *
     * @return A new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for {@link VoyageAIContextualizedEmbeddingGenerationService}.
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
         * @param modelId Model ID (e.g., "voyage-3")
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
         * Builds the VoyageAI contextualized embedding generation service.
         *
         * @return A new instance of VoyageAIContextualizedEmbeddingGenerationService
         */
        public VoyageAIContextualizedEmbeddingGenerationService build() {
            return new VoyageAIContextualizedEmbeddingGenerationService(client, modelId, serviceId);
        }
    }
}
