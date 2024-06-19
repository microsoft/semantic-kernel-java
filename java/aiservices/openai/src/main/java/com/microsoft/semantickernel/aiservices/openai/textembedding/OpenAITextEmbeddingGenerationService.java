// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.aiservices.openai.textembedding;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.models.EmbeddingItem;
import com.azure.ai.openai.models.Embeddings;
import com.azure.ai.openai.models.EmbeddingsOptions;
import com.microsoft.semantickernel.aiservices.openai.OpenAiService;
import com.microsoft.semantickernel.aiservices.openai.chatcompletion.OpenAIChatCompletion;
import com.microsoft.semantickernel.exceptions.AIException;
import com.microsoft.semantickernel.services.openai.OpenAiServiceBuilder;
import com.microsoft.semantickernel.services.textembedding.Embedding;
import com.microsoft.semantickernel.services.textembedding.TextEmbeddingGenerationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class OpenAITextEmbeddingGenerationService extends OpenAiService
    implements TextEmbeddingGenerationService {
    private static final Logger LOGGER = LoggerFactory
        .getLogger(OpenAITextEmbeddingGenerationService.class);

    public OpenAITextEmbeddingGenerationService(
        OpenAIAsyncClient client,
        String deploymentName,
        String modelId,
        @Nullable String serviceId) {
        super(client, serviceId, modelId, deploymentName);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public Mono<List<Embedding>> generateEmbeddingsAsync(List<String> data) {
        return this.internalGenerateTextEmbeddingsAsync(data);
    }

    protected Mono<List<Embedding>> internalGenerateTextEmbeddingsAsync(List<String> data) {
        EmbeddingsOptions options = new EmbeddingsOptions(data)
            .setModel(getModelId())
            .setInputType("string");

        return getClient()
            .getEmbeddings(getModelId(), options)
            .flatMapIterable(Embeddings::getData)
            .mapNotNull(EmbeddingItem::getEmbedding)
            .map(ArrayList::new)
            .mapNotNull(Embedding::new)
            .collectList();
    }

    public static class Builder extends
        OpenAiServiceBuilder<OpenAITextEmbeddingGenerationService, OpenAITextEmbeddingGenerationService.Builder> {
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
                serviceId);
        }
    }
}
