// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.ai.openai.chatcompletion;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.models.ChatCompletions;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.ChatMessage;
import com.azure.ai.openai.models.ChatRole;
import com.microsoft.semantickernel.Verify;
import com.microsoft.semantickernel.ai.AIException;
import com.microsoft.semantickernel.chatcompletion.ChatCompletion;
import com.microsoft.semantickernel.chatcompletion.ChatHistory;
import com.microsoft.semantickernel.chatcompletion.ChatRequestSettings;
import com.microsoft.semantickernel.connectors.ai.openai.azuresdk.ClientBase;
import com.microsoft.semantickernel.textcompletion.CompletionRequestSettings;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** OpenAI chat completion client. */
public class OpenAIChatCompletion extends ClientBase implements ChatCompletion<OpenAIChatHistory> {
    public OpenAIChatCompletion(OpenAIAsyncClient client, String modelId) {
        super(client, modelId);
    }

    @Override
    public Mono<List<String>> completeAsync(
            @Nonnull String text, @Nonnull CompletionRequestSettings requestSettings) {
        ChatRequestSettings chatRequestSettings = new ChatRequestSettings(requestSettings);
        return generateMessageAsync(createNewChat(text), chatRequestSettings).map(Arrays::asList);
    }

    public static class Builder implements ChatCompletion.Builder {
        public Builder() {}

        @Override
        public ChatCompletion<OpenAIChatHistory> build(OpenAIAsyncClient client, String modelId) {
            return new OpenAIChatCompletion(client, modelId);
        }
    }

    /**
     * Generate a new chat message
     *
     * @param chat Chat history
     * @param requestSettings AI request settings
     * @return The response generated by the request
     */
    @Override
    public Mono<String> generateMessageAsync(
            OpenAIChatHistory chat, @Nullable ChatRequestSettings requestSettings) {

        if (requestSettings == null) {
            requestSettings = new ChatRequestSettings();
        }
        return this.internalGenerateChatMessageAsync(chat, requestSettings);
    }

    /**
     * Generate a new chat message
     *
     * @param chat Chat history
     * @param requestSettings AI request settings
     * @return The response generated by the request
     */
    private Mono<String> internalGenerateChatMessageAsync(
            ChatHistory chat, ChatRequestSettings requestSettings) {
        Verify.notNull(chat);
        Verify.notNull(requestSettings);

        validateMaxTokens(requestSettings.getMaxTokens());
        ChatCompletionsOptions options = createChatCompletionsOptions(requestSettings, chat);

        return getClient()
                .getChatCompletions(getModelId(), options)
                .flatMap(
                        response -> {
                            if (response == null || response.getChoices().isEmpty()) {
                                return Mono.error(
                                        new AIException(
                                                AIException.ErrorCodes.InvalidResponseContent,
                                                "Chat completions not found"));
                            } else {
                                return Mono.just(
                                        response.getChoices().get(0).getMessage().getContent());
                            }
                        });
    }

    private static ChatCompletionsOptions createChatCompletionsOptions(
            ChatRequestSettings requestSettings, ChatHistory chat) {
        List<ChatMessage> messages =
                chat.getMessages().stream()
                        .map(
                                it ->
                                        new ChatMessage(
                                                toChatRole(it.getAuthorRoles()), it.getContent()))
                        .collect(Collectors.toList());

        ChatCompletionsOptions options = new ChatCompletionsOptions(messages);

        options.setMaxTokens(requestSettings.getMaxTokens());
        options.setTemperature(requestSettings.getTemperature());
        options.setTopP(requestSettings.getTopP());
        options.setFrequencyPenalty(requestSettings.getFrequencyPenalty());
        options.setPresencePenalty(requestSettings.getPresencePenalty());
        options.setLogitBias(new HashMap<>());
        options.setN(requestSettings.getBestOf());
        options.setUser(requestSettings.getUser());

        return options;
    }

    private static ChatRole toChatRole(ChatHistory.AuthorRoles authorRoles) {
        switch (authorRoles) {
            case System:
                return ChatRole.SYSTEM;
            case User:
                return ChatRole.USER;
            case Assistant:
                return ChatRole.ASSISTANT;
            default:
                throw new IllegalArgumentException(
                        "Invalid chat message author: " + authorRoles.name());
        }
    }

    @Override
    public OpenAIChatHistory createNewChat(@Nullable String instructions) {
        return internalCreateNewChat(instructions);
    }

    @Override
    public Flux<String> generateMessageStream(
            ChatHistory chat, @Nullable ChatRequestSettings requestSettings) {
        return this.getStreamingChatCompletionsAsync(chat, requestSettings)
                .concatMap(
                        chatCompletionResult -> {
                            return Flux.fromIterable(chatCompletionResult.getChoices());
                        })
                .map(
                        chatChoice -> {
                            ChatMessage message = chatChoice.getDelta();
                            if (message == null || message.getContent() == null) {
                                return "";
                            }
                            return message.getContent();
                        });
    }

    @Override
    public Flux<ChatCompletions> getStreamingChatCompletionsAsync(
            ChatHistory chat, ChatRequestSettings requestSettings) {
        return internalGetChatStreamingResultsAsync(chat, requestSettings);
    }

    private Flux<ChatCompletions> internalGetChatStreamingResultsAsync(
            ChatHistory chat, @Nullable ChatRequestSettings requestSettings) {
        Verify.notNull(chat);
        if (requestSettings == null) {
            requestSettings = new ChatRequestSettings();
        }

        ClientBase.validateMaxTokens(requestSettings.getMaxTokens());

        ChatCompletionsOptions options = createChatCompletionsOptions(requestSettings, chat);
        options = options.setStream(true);

        return getClient().getChatCompletionsStream(getModelId(), options);
    }

    /**
     * Create a new empty chat instance
     *
     * @param instructions Optional chat instructions for the AI service
     * @return Chat object
     */
    private static OpenAIChatHistory internalCreateNewChat(@Nullable String instructions) {
        if (instructions == null) {
            instructions = "";
        }
        return new OpenAIChatHistory(instructions);
    }
}
