// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.textcompletion;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.TextAIService;
import com.microsoft.semantickernel.builders.Buildable;
import com.microsoft.semantickernel.builders.SemanticKernelBuilder;
import com.microsoft.semantickernel.builders.ServiceLoadUtil;
import com.microsoft.semantickernel.orchestration.PromptExecutionSettings;
import java.util.List;
import javax.annotation.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Interface for text completion services
 */
public interface TextGenerationService extends Buildable, TextAIService {

    /**
     * Creates a completion for the prompt and settings.
     *
     * @param prompt            The prompt to complete.
     * @param executionSettings Request settings for the completion API
     * @return Text generated by the remote model
     */
    Mono<List<TextContent>> getTextContentsAsync(
        String prompt,
        @Nullable PromptExecutionSettings executionSettings,
        @Nullable Kernel kernel);

    /**
     * Get streaming results for the prompt using the specified execution settings. Each modality
     * may support for different types of streaming contents.
     *
     * @param prompt            The prompt to complete.
     * @param executionSettings The AI execution settings (optional).
     * @param kernel            The <see cref="Kernel"/> containing services, plugins, and other
     *                          state for use throughout the operation.
     * @return Streaming list of different completion streaming string updates generated by the
     * remote model
     */
    Flux<StreamingTextContent> getStreamingTextContentsAsync(
        String prompt,
        @Nullable PromptExecutionSettings executionSettings,
        @Nullable Kernel kernel);

    static Builder builder() {
        return ServiceLoadUtil.findServiceLoader(Builder.class,
                "com.microsoft.semantickernel.aiservices.openai.textcompletion.OpenAITextGenerationService$Builder")
            .get();
    }


    /**
     * Builder for a TextGenerationService
     */
    abstract class Builder implements SemanticKernelBuilder<TextGenerationService> {

        protected String modelId;
        protected OpenAIAsyncClient client;
        protected String serviceId;

        public Builder withModelId(String modelId) {
            this.modelId = modelId;
            return this;
        }

        public Builder withOpenAIAsyncClient(OpenAIAsyncClient client) {
            this.client = client;
            return this;
        }

        public Builder withServiceId(String serviceId) {
            this.serviceId = serviceId;
            return this;
        }

        public abstract TextGenerationService build();

    }
}
