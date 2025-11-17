// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.aiservices.voyageai.reranking;

import com.microsoft.semantickernel.aiservices.voyageai.core.VoyageAIClient;
import com.microsoft.semantickernel.aiservices.voyageai.core.VoyageAIModels;
import com.microsoft.semantickernel.orchestration.PromptExecutionSettings;
import com.microsoft.semantickernel.services.reranking.RerankResult;
import com.microsoft.semantickernel.services.reranking.TextRerankingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * VoyageAI implementation of {@link TextRerankingService}.
 * Supports models like rerank-2, rerank-2-lite.
 */
public class VoyageAITextRerankingService implements TextRerankingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(VoyageAITextRerankingService.class);

    private final VoyageAIClient client;
    private final String modelId;
    private final String serviceId;
    private final Integer topK;

    /**
     * Creates a new instance of VoyageAI text reranking service.
     *
     * @param client    VoyageAI client
     * @param modelId   Model ID (e.g., "rerank-2")
     * @param serviceId Optional service ID
     * @param topK      Optional top K results to return
     */
    public VoyageAITextRerankingService(
        VoyageAIClient client,
        String modelId,
        @Nullable String serviceId,
        @Nullable Integer topK) {

        if (client == null) {
            throw new IllegalArgumentException("Client cannot be null");
        }
        if (modelId == null || modelId.trim().isEmpty()) {
            throw new IllegalArgumentException("Model ID cannot be null or empty");
        }

        this.client = client;
        this.modelId = modelId;
        this.serviceId = serviceId != null ? serviceId : PromptExecutionSettings.DEFAULT_SERVICE_ID;
        this.topK = topK;
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
     * Reranks documents based on their relevance to the query.
     *
     * @param query     The query to rank documents against
     * @param documents The list of documents to rerank
     * @return A Mono containing a list of {@link RerankResult} sorted by relevance score in descending order
     */
    @Override
    public Mono<List<RerankResult>> rerankAsync(String query, List<String> documents) {
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Query cannot be null or empty");
        }
        if (documents == null || documents.isEmpty()) {
            return Mono.just(Collections.emptyList());
        }

        LOGGER.debug("Reranking {} documents using model {}", documents.size(), modelId);

        VoyageAIModels.RerankRequest request = new VoyageAIModels.RerankRequest();
        request.setQuery(query);
        request.setDocuments(documents);
        request.setModel(modelId);
        request.setTopK(topK);
        request.setTruncation(true);

        return client.sendRequestAsync("rerank", request, VoyageAIModels.RerankResponse.class)
            .map(response -> {
                LOGGER.debug("Received {} reranked results from VoyageAI", response.getData().size());

                List<RerankResult> results = response.getData().stream()
                    .sorted(Comparator.comparingDouble(VoyageAIModels.RerankDataItem::getRelevanceScore).reversed())
                    .map(item -> new RerankResult(
                        item.getIndex(),
                        documents.get(item.getIndex()),
                        item.getRelevanceScore()
                    ))
                    .collect(Collectors.toList());

                return results;
            });
    }

    /**
     * Creates a builder for VoyageAI text reranking service.
     *
     * @return A new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for {@link VoyageAITextRerankingService}.
     */
    public static class Builder {
        private VoyageAIClient client;
        private String modelId;
        private String serviceId;
        private Integer topK;

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
         * @param modelId Model ID (e.g., "rerank-2")
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
         * Sets the top K results to return.
         *
         * @param topK Top K results
         * @return This builder
         */
        public Builder withTopK(Integer topK) {
            this.topK = topK;
            return this;
        }

        /**
         * Builds the VoyageAI text reranking service.
         *
         * @return A new instance of VoyageAITextRerankingService
         */
        public VoyageAITextRerankingService build() {
            return new VoyageAITextRerankingService(client, modelId, serviceId, topK);
        }
    }
}
