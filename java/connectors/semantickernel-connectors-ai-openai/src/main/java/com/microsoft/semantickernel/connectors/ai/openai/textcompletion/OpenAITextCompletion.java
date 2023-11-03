// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.ai.openai.textcompletion;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.models.Choice;
import com.azure.ai.openai.models.Completions;
import com.azure.ai.openai.models.CompletionsOptions;
import com.microsoft.semantickernel.Verify;
import com.microsoft.semantickernel.ai.AIException;
import com.microsoft.semantickernel.chatcompletion.ChatRequestSettings;
import com.microsoft.semantickernel.connectors.ai.openai.azuresdk.ClientBase;
import com.microsoft.semantickernel.exceptions.NotSupportedException;
import com.microsoft.semantickernel.exceptions.NotSupportedException.ErrorCodes;
import com.microsoft.semantickernel.textcompletion.CompletionRequestSettings;
import com.microsoft.semantickernel.textcompletion.CompletionType;
import com.microsoft.semantickernel.textcompletion.TextCompletion;
import jakarta.inject.Inject;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/// <summary>
/// OpenAI text completion service.
/// TODO: forward ETW logging to ILogger, see
// https://learn.microsoft.com/en-us/dotnet/azure/sdk/logging
/// </summary>
public class OpenAITextCompletion extends ClientBase implements TextCompletion {

    private final CompletionType defaultCompletionType;

    /// <summary>
    /// Create an instance of the OpenAI text completion connector
    /// </summary>
    /// <param name="modelId">Model name</param>
    /// <param name="apiKey">OpenAI API Key</param>
    /// <param name="organization">OpenAI Organization Id (usually optional)</param>
    /// <param name="handlerFactory">Retry handler factory for HTTP
    /// requests.</param>
    /// <param name="log">Application logger</param>
    @Inject
    public OpenAITextCompletion(OpenAIAsyncClient client, String modelId) {
        super(client, modelId);
        defaultCompletionType = CompletionType.STREAMING;
    }

    public OpenAITextCompletion(
            OpenAIAsyncClient client, String modelId, CompletionType defaultCompletionType) {
        super(client, modelId);

        this.defaultCompletionType = defaultCompletionType;
    }

    @Override
    public Mono<List<String>> completeAsync(
            @Nonnull String text, @Nonnull CompletionRequestSettings requestSettings) {
        return this.internalCompleteTextAsync(text, requestSettings);
    }

    @Override
    public Flux<String> completeStreamAsync(
            @Nonnull String text, @Nonnull CompletionRequestSettings requestSettings) {
        CompletionsOptions completionsOptions = getCompletionsOptions(text, requestSettings);

        return generateMessageStream(completionsOptions);
    }

    @Override
    public CompletionType defaultCompletionType() {
        return defaultCompletionType;
    }

    private Flux<String> generateMessageStream(CompletionsOptions completionsOptions) {
        return getClient()
                .getCompletionsStream(getModelId(), completionsOptions)
                .groupBy(Completions::getId)
                .concatMap(
                        completionResult -> {
                            return completionResult
                                    .concatMap(
                                            completion ->
                                                    Flux.fromIterable(completion.getChoices()))
                                    .reduce("", accumulateString());
                        });
    }

    private static BiFunction<String, Choice, String> accumulateString() {
        return (newText, choice) -> {
            String message = choice.getText();
            if (!Verify.isNullOrEmpty(message)) {
                return newText + message;
            }
            return newText;
        };
    }

    protected Mono<List<String>> internalCompleteTextAsync(
            String text, CompletionRequestSettings requestSettings) {
        CompletionsOptions completionsOptions = getCompletionsOptions(text, requestSettings);

        return getClient()
                .getCompletions(getModelId(), completionsOptions)
                .flatMapIterable(Completions::getChoices)
                .mapNotNull(Choice::getText)
                .collectList();
    }

    private CompletionsOptions getCompletionsOptions(
            String text, CompletionRequestSettings requestSettings) {
        if (requestSettings.getMaxTokens() < 1) {
            throw new AIException(AIException.ErrorCodes.INVALID_REQUEST, "Max tokens must be >0");
        }

        CompletionsOptions options =
                new CompletionsOptions(Collections.singletonList(text))
                        .setMaxTokens(requestSettings.getMaxTokens())
                        .setTemperature(requestSettings.getTemperature())
                        .setTopP(requestSettings.getTopP())
                        .setFrequencyPenalty(requestSettings.getFrequencyPenalty())
                        .setPresencePenalty(requestSettings.getPresencePenalty())
                        .setModel(getModelId())
                        .setUser(requestSettings.getUser())
                        .setBestOf(requestSettings.getBestOf())
                        .setLogitBias(new HashMap<>());

        if (requestSettings instanceof ChatRequestSettings) {
            options = options.setStop(requestSettings.getStopSequences());
        }
        return options;
    }

    public static final class Builder implements TextCompletion.Builder {

        @Nullable private OpenAIAsyncClient client;
        @Nullable private String modelId;
        private CompletionType defaultCompletionType = CompletionType.STREAMING;

        public Builder withOpenAIClient(OpenAIAsyncClient client) {
            this.client = client;
            return this;
        }

        public Builder withModelId(String modelId) {
            this.modelId = modelId;
            return this;
        }

        @Override
        public Builder withDefaultCompletionType(CompletionType completionType) {
            this.defaultCompletionType = completionType;
            return this;
        }

        @Override
        public TextCompletion build() {
            if (client == null) {
                throw new NotSupportedException(ErrorCodes.NOT_SUPPORTED, "OpenAI client not set");
            }
            if (modelId == null) {
                throw new NotSupportedException(ErrorCodes.NOT_SUPPORTED, "Model ID not set");
            }
            return new OpenAITextCompletion(client, modelId, defaultCompletionType);
        }
    }
}
