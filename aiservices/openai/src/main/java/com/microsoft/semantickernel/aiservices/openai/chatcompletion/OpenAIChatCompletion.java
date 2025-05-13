// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.aiservices.openai.chatcompletion;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.models.ChatChoice;
import com.azure.ai.openai.models.ChatCompletions;
import com.azure.ai.openai.models.ChatCompletionsFunctionToolCall;
import com.azure.ai.openai.models.ChatCompletionsFunctionToolDefinition;
import com.azure.ai.openai.models.ChatCompletionsFunctionToolDefinitionFunction;
import com.azure.ai.openai.models.ChatCompletionsJsonResponseFormat;
import com.azure.ai.openai.models.ChatCompletionsNamedToolSelection;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.ChatCompletionsTextResponseFormat;
import com.azure.ai.openai.models.ChatCompletionsToolCall;
import com.azure.ai.openai.models.ChatCompletionsToolDefinition;
import com.azure.ai.openai.models.ChatCompletionsToolSelection;
import com.azure.ai.openai.models.ChatCompletionsToolSelectionPreset;
import com.azure.ai.openai.models.ChatMessageImageContentItem;
import com.azure.ai.openai.models.ChatMessageImageDetailLevel;
import com.azure.ai.openai.models.ChatMessageImageUrl;
import com.azure.ai.openai.models.ChatRequestAssistantMessage;
import com.azure.ai.openai.models.ChatRequestFunctionMessage;
import com.azure.ai.openai.models.ChatRequestMessage;
import com.azure.ai.openai.models.ChatRequestSystemMessage;
import com.azure.ai.openai.models.ChatRequestToolMessage;
import com.azure.ai.openai.models.ChatRequestUserMessage;
import com.azure.ai.openai.models.ChatResponseMessage;
import com.azure.ai.openai.models.CompletionsUsage;
import com.azure.ai.openai.models.FunctionCall;
import com.azure.ai.openai.models.FunctionDefinition;
import com.azure.json.JsonOptions;
import com.azure.json.implementation.DefaultJsonReader;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ContainerNode;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.aiservices.openai.OpenAiService;
import com.microsoft.semantickernel.aiservices.openai.chatcompletion.responseformat.ChatCompletionsJsonSchemaResponseFormat;
import com.microsoft.semantickernel.aiservices.openai.implementation.OpenAIRequestSettings;
import com.microsoft.semantickernel.contents.FunctionCallContent;
import com.microsoft.semantickernel.contextvariables.ContextVariable;
import com.microsoft.semantickernel.contextvariables.ContextVariableTypes;
import com.microsoft.semantickernel.exceptions.AIException;
import com.microsoft.semantickernel.exceptions.AIException.ErrorCodes;
import com.microsoft.semantickernel.exceptions.SKCheckedException;
import com.microsoft.semantickernel.exceptions.SKException;
import com.microsoft.semantickernel.functionchoice.AutoFunctionChoiceBehavior;
import com.microsoft.semantickernel.functionchoice.FunctionChoiceBehavior;
import com.microsoft.semantickernel.functionchoice.NoneFunctionChoiceBehavior;
import com.microsoft.semantickernel.functionchoice.RequiredFunctionChoiceBehavior;
import com.microsoft.semantickernel.hooks.KernelHookEvent;
import com.microsoft.semantickernel.hooks.KernelHooks;
import com.microsoft.semantickernel.hooks.PostChatCompletionEvent;
import com.microsoft.semantickernel.hooks.PreChatCompletionEvent;
import com.microsoft.semantickernel.hooks.PreToolCallEvent;
import com.microsoft.semantickernel.implementation.CollectionUtil;
import com.microsoft.semantickernel.implementation.telemetry.ChatCompletionSpan;
import com.microsoft.semantickernel.implementation.telemetry.SemanticKernelTelemetry;
import com.microsoft.semantickernel.orchestration.FunctionResult;
import com.microsoft.semantickernel.orchestration.FunctionResultMetadata;
import com.microsoft.semantickernel.orchestration.InvocationContext;
import com.microsoft.semantickernel.orchestration.InvocationReturnMode;
import com.microsoft.semantickernel.orchestration.PromptExecutionSettings;
import com.microsoft.semantickernel.orchestration.ToolCallBehavior;
import com.microsoft.semantickernel.orchestration.responseformat.JsonResponseSchema;
import com.microsoft.semantickernel.orchestration.responseformat.JsonSchemaResponseFormat;
import com.microsoft.semantickernel.semanticfunctions.KernelFunction;
import com.microsoft.semantickernel.semanticfunctions.KernelArguments;
import com.microsoft.semantickernel.services.chatcompletion.AuthorRole;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;
import com.microsoft.semantickernel.services.chatcompletion.StreamingChatContent;
import com.microsoft.semantickernel.services.chatcompletion.message.ChatMessageContentType;
import com.microsoft.semantickernel.services.chatcompletion.message.ChatMessageImageContent;
import com.microsoft.semantickernel.services.openai.OpenAiServiceBuilder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * OpenAI chat completion service.
 */
public class OpenAIChatCompletion extends OpenAiService<OpenAIAsyncClient>
    implements ChatCompletionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenAIChatCompletion.class);

    protected OpenAIChatCompletion(
        OpenAIAsyncClient client,
        String deploymentName,
        String modelId,
        @Nullable String serviceId) {
        super(client, serviceId, modelId, deploymentName);
    }

    /**
     * Create a new instance of {@link OpenAIChatCompletion.Builder}.
     *
     * @return a new instance of {@link OpenAIChatCompletion.Builder}
     */
    public static OpenAIChatCompletion.Builder builder() {
        return new OpenAIChatCompletion.Builder();
    }

    @Override
    public Mono<List<ChatMessageContent<?>>> getChatMessageContentsAsync(
        ChatHistory chatHistory,
        @Nullable Kernel kernel,
        @Nullable InvocationContext invocationContext) {

        List<ChatRequestMessage> chatRequestMessages = getChatRequestMessages(chatHistory);

        ChatMessages messages = new ChatMessages(chatRequestMessages);

        return internalChatMessageContentsAsync(
            messages,
            kernel,
            invocationContext)
            .flatMap(history -> {
                try {
                    ChatHistory chatHistoryResult;

                    if (invocationContext != null
                        && invocationContext.returnMode() == InvocationReturnMode.FULL_HISTORY) {
                        chatHistoryResult = new ChatHistory(chatHistory.getMessages());
                    } else {
                        chatHistoryResult = new ChatHistory();
                    }

                    chatHistoryResult.addAll(
                        new ChatHistory(toOpenAIChatMessageContent(history.newMessages)));
                    chatHistoryResult.addAll(new ChatHistory(history.newChatMessageContent));

                    if (invocationContext != null
                        && invocationContext
                            .returnMode() == InvocationReturnMode.LAST_MESSAGE_ONLY) {
                        chatHistoryResult = new ChatHistory(
                            Collections.singletonList(
                                CollectionUtil.getLastOrNull(chatHistoryResult.getMessages())));
                    }

                    return Mono.just(chatHistoryResult.getMessages());
                } catch (Exception e) {
                    return Mono.error(e);
                }
            });
    }

    @Override
    public Mono<List<ChatMessageContent<?>>> getChatMessageContentsAsync(
        String prompt,
        @Nullable Kernel kernel,
        @Nullable InvocationContext invocationContext) {
        ParsedPrompt parsedPrompt = OpenAiXMLPromptParser.parse(prompt);

        ChatMessages messages = new ChatMessages(parsedPrompt.getChatRequestMessages());

        return internalChatMessageContentsAsync(
            messages,
            kernel,
            invocationContext)
            .flatMap(m -> {
                try {
                    ChatHistory result = new ChatHistory(toOpenAIChatMessageContent(m.allMessages));

                    result.addAll(new ChatHistory(m.newChatMessageContent));

                    if (invocationContext != null
                        && invocationContext
                            .returnMode() == InvocationReturnMode.LAST_MESSAGE_ONLY) {
                        result = new ChatHistory(
                            Collections.singletonList(
                                CollectionUtil.getLastOrNull(result.getMessages())));
                    }

                    return Mono.just(result.getMessages());
                } catch (SKCheckedException e) {
                    return Mono.error(e);
                }
            });
    }

    @Override
    public Flux<StreamingChatContent<?>> getStreamingChatMessageContentsAsync(
        ChatHistory chatHistory,
        @Nullable Kernel kernel,
        @Nullable InvocationContext invocationContext) {
        if (invocationContext != null &&
            invocationContext.getToolCallBehavior() != null &&
            invocationContext.getToolCallBehavior().isAutoInvokeAllowed()) {
            throw new SKException(
                "ToolCallBehavior auto-invoke is not supported for streaming chat message contents");
        }

        if (invocationContext != null &&
            invocationContext.getFunctionChoiceBehavior() != null &&
            invocationContext.getFunctionChoiceBehavior() instanceof AutoFunctionChoiceBehavior &&
            ((AutoFunctionChoiceBehavior) invocationContext.getFunctionChoiceBehavior())
                .isAutoInvoke()) {
            throw new SKException(
                "FunctionChoiceBehavior auto-invoke is not supported for streaming chat message contents");
        }

        if (invocationContext != null
            && invocationContext.returnMode() != InvocationReturnMode.NEW_MESSAGES_ONLY) {
            throw new SKException(
                "Streaming chat message contents only supports NEW_MESSAGES_ONLY return mode");
        }

        List<ChatRequestMessage> chatRequestMessages = getChatRequestMessages(chatHistory);

        ChatMessages messages = new ChatMessages(chatRequestMessages);

        List<OpenAIFunction> functions = new ArrayList<>();
        if (kernel != null) {
            kernel.getPlugins()
                .forEach(plugin -> plugin.getFunctions().forEach((name, function) -> functions
                    .add(OpenAIFunction.build(function.getMetadata(), plugin.getName()))));
        }

        OpenAIToolCallConfig toolCallConfig = getToolCallConfig(
            invocationContext,
            functions,
            messages.allMessages,
            0);

        ChatCompletionsOptions options = executeHook(
            invocationContext,
            kernel,
            new PreChatCompletionEvent(
                getCompletionsOptions(
                    this,
                    messages.allMessages,
                    invocationContext,
                    toolCallConfig)))
            .getOptions();

        return getClient()
            .getChatCompletionsStreamWithResponse(
                getDeploymentName(),
                options,
                OpenAIRequestSettings.getRequestOptions())
            .flatMap(completionsResult -> {
                if (completionsResult.getStatusCode() >= 400) {
                    //SemanticKernelTelemetry.endSpanWithError(span);
                    return Mono.error(new AIException(ErrorCodes.SERVICE_ERROR,
                        "Request failed: " + completionsResult.getStatusCode()));
                }
                //SemanticKernelTelemetry.endSpanWithUsage(span, completionsResult.getValue().getUsage());

                return Mono.just(completionsResult.getValue());
            })
            .flatMap(completions -> {
                return Flux.fromIterable(completions.getChoices())
                    .map(message -> {
                        AuthorRole role = message.getDelta().getRole() == null
                            ? AuthorRole.ASSISTANT
                            : AuthorRole.valueOf(message.getDelta().getRole().toString()
                                .toUpperCase(Locale.ROOT));

                        return new OpenAIStreamingChatMessageContent<>(
                            completions.getId(),
                            role,
                            message.getDelta().getContent(),
                            getModelId(),
                            null,
                            null,
                            null,
                            Arrays.asList());
                    });
            });
    }

    @Override
    public Flux<StreamingChatContent<?>> getStreamingChatMessageContentsAsync(
        String prompt,
        @Nullable Kernel kernel,
        @Nullable InvocationContext invocationContext) {
        return getStreamingChatMessageContentsAsync(
            new ChatHistory().addUserMessage(prompt),
            kernel,
            invocationContext);
    }

    // Holds messages temporarily as we build up our result
    private static class ChatMessages {

        private final List<ChatRequestMessage> newMessages;
        private final List<ChatRequestMessage> allMessages;
        private final List<OpenAIChatMessageContent<?>> newChatMessageContent;

        public ChatMessages(List<ChatRequestMessage> allMessages) {
            this.allMessages = Collections.unmodifiableList(allMessages);
            this.newMessages = Collections.unmodifiableList(new ArrayList<>());
            this.newChatMessageContent = Collections.unmodifiableList(new ArrayList<>());
        }

        private ChatMessages(
            List<ChatRequestMessage> allMessages,
            List<ChatRequestMessage> newMessages,
            List<OpenAIChatMessageContent<?>> newChatMessageContent) {
            this.allMessages = Collections.unmodifiableList(allMessages);
            this.newMessages = Collections.unmodifiableList(newMessages);
            this.newChatMessageContent = Collections.unmodifiableList(newChatMessageContent);
        }

        @CheckReturnValue
        public ChatMessages addAll(List<ChatRequestMessage> requestMessage) {
            List<ChatRequestMessage> tmpAllMessages = new ArrayList<>(allMessages);
            List<ChatRequestMessage> tmpNewMessages = new ArrayList<>(newMessages);
            tmpAllMessages.addAll(requestMessage);
            tmpNewMessages.addAll(requestMessage);
            return new ChatMessages(
                tmpAllMessages,
                tmpNewMessages,
                newChatMessageContent);
        }

        @CheckReturnValue
        public ChatMessages add(ChatRequestMessage requestMessage) {
            return addAll(Arrays.asList(requestMessage));
        }

        @CheckReturnValue
        public ChatMessages addChatMessage(List<OpenAIChatMessageContent<?>> chatMessageContent) {
            ArrayList<OpenAIChatMessageContent<?>> tmpChatMessageContent = new ArrayList<>(
                newChatMessageContent);
            tmpChatMessageContent.addAll(chatMessageContent);

            return new ChatMessages(
                allMessages,
                newMessages,
                tmpChatMessageContent);
        }

        /**
         * Checks that the two messages have a similar history
         *
         * @param messages The messages to merge in
         * @return The merged chat messages
         */
        boolean assertCommonHistory(List<ChatRequestMessage> messages) {
            int index = 0;
            while (index < messages.size() && index < this.allMessages.size()) {
                ChatRequestMessage a = messages.get(index);
                ChatRequestMessage b = this.allMessages.get(index);

                boolean matches = false;
                if (a instanceof ChatRequestAssistantMessage
                    && b instanceof ChatRequestAssistantMessage) {
                    matches = Objects.equals(((ChatRequestAssistantMessage) a).getContent(),
                        ((ChatRequestAssistantMessage) b).getContent());
                } else if (a instanceof ChatRequestSystemMessage
                    && b instanceof ChatRequestSystemMessage) {
                    matches = Objects.equals(((ChatRequestSystemMessage) a).getContent(),
                        ((ChatRequestSystemMessage) b).getContent());
                } else if (a instanceof ChatRequestUserMessage
                    && b instanceof ChatRequestUserMessage) {
                    matches = Objects.equals(((ChatRequestUserMessage) a).getContent(),
                        ((ChatRequestUserMessage) b).getContent());
                } else if (a instanceof ChatRequestFunctionMessage
                    && b instanceof ChatRequestFunctionMessage) {
                    matches = Objects.equals(((ChatRequestFunctionMessage) a).getContent(),
                        ((ChatRequestFunctionMessage) b).getContent());
                } else if (a instanceof ChatRequestToolMessage
                    && b instanceof ChatRequestToolMessage) {
                    matches = Objects.equals(((ChatRequestToolMessage) a).getContent(),
                        ((ChatRequestToolMessage) b).getContent());
                }

                if (!matches) {
                    LOGGER.warn("Messages do not match at index: " + index
                        + " you might be merging unrelated message histories");
                    return false;
                }

                index++;
            }

            return true;

        }
    }

    private Mono<ChatMessages> internalChatMessageContentsAsync(
        ChatMessages messages,
        @Nullable Kernel kernel,
        @Nullable InvocationContext invocationContext) {

        List<OpenAIFunction> functions = new ArrayList<>();
        if (kernel != null) {
            kernel.getPlugins()
                .forEach(plugin -> plugin.getFunctions().forEach((name, function) -> functions
                    .add(OpenAIFunction.build(function.getMetadata(), plugin.getName()))));
        }

        return internalChatMessageContentsAsync(
            messages,
            kernel,
            functions,
            invocationContext,
            0);
    }

    private Mono<ChatMessages> internalChatMessageContentsAsync(
        ChatMessages messages,
        @Nullable Kernel kernel,
        List<OpenAIFunction> functions,
        @Nullable InvocationContext invocationContext,
        int requestIndex) {

        OpenAIToolCallConfig toolCallConfig = getToolCallConfig(
            invocationContext,
            functions,
            messages.allMessages,
            requestIndex);

        ChatCompletionsOptions options = executeHook(
            invocationContext,
            kernel,
            new PreChatCompletionEvent(
                getCompletionsOptions(
                    this,
                    messages.allMessages,
                    invocationContext,
                    toolCallConfig)))
            .getOptions();

        return Mono.deferContextual(contextView -> {
            ChatCompletionSpan span = ChatCompletionSpan.startChatCompletionSpan(
                SemanticKernelTelemetry.getTelemetry(invocationContext),
                contextView,
                getModelId(),
                SemanticKernelTelemetry.OPEN_AI_PROVIDER,
                options.getMaxTokens(),
                options.getTemperature(),
                options.getTopP());

            return getClient()
                .getChatCompletionsWithResponse(getDeploymentName(), options,
                    OpenAIRequestSettings.getRequestOptions())
                .contextWrite(span.getReactorContextModifier())
                .flatMap(completionsResult -> {
                    if (completionsResult.getStatusCode() >= 400) {
                        return Mono.error(new AIException(ErrorCodes.SERVICE_ERROR,
                            "Request failed: " + completionsResult.getStatusCode()));
                    }

                    return Mono.just(completionsResult.getValue());
                })
                .doOnError(span::endSpanWithError)
                .doOnSuccess(span::endSpanWithUsage)
                .doOnTerminate(span::close);
        })
            .flatMap(completions -> {
                List<ChatResponseMessage> responseMessages = completions
                    .getChoices()
                    .stream()
                    .map(ChatChoice::getMessage)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

                // execute post chat completion hook
                executeHook(invocationContext, kernel, new PostChatCompletionEvent(completions));

                // Just return the result:
                // If auto-invoking is not enabled
                // Or if we are auto-invoking, but we somehow end up with other than 1 choice even though only 1 was requested
                if (toolCallConfig == null || !toolCallConfig.isAutoInvoke()
                    || responseMessages.size() != 1) {
                    List<OpenAIChatMessageContent<?>> chatMessageContents = getChatMessageContentsAsync(
                        completions);
                    return Mono.just(messages.addChatMessage(chatMessageContents));
                }

                // Or if there are no tool calls to be done
                ChatResponseMessage response = responseMessages.get(0);
                List<ChatCompletionsToolCall> toolCalls = response.getToolCalls();
                if (toolCalls == null || toolCalls.isEmpty()) {
                    List<OpenAIChatMessageContent<?>> chatMessageContents = getChatMessageContentsAsync(
                        completions);
                    return Mono.just(messages.addChatMessage(chatMessageContents));
                }

                ChatRequestAssistantMessage requestMessage = new ChatRequestAssistantMessage(
                    response.getContent());
                requestMessage.setToolCalls(toolCalls);

                // Add the original assistant message to the chat options; this is required for the service
                // to understand the tool call responses
                ChatMessages messagesWithToolCall = messages.add(requestMessage);

                return Flux
                    .fromIterable(toolCalls)
                    .reduce(
                        Mono.just(messagesWithToolCall),
                        (requestMessages, toolCall) -> {
                            if (toolCall instanceof ChatCompletionsFunctionToolCall) {
                                return performToolCall(kernel, invocationContext, requestMessages,
                                    toolCall);
                            }

                            return requestMessages;
                        })
                    .flatMap(it -> it)
                    .flatMap(msgs -> {
                        return internalChatMessageContentsAsync(msgs, kernel, functions,
                            invocationContext, requestIndex + 1);
                    })
                    .onErrorResume(e -> {

                        LOGGER.warn("Tool invocation attempt failed: ", e);

                        // If FunctionInvocationError occurred and there are still attempts left, retry, else exit
                        if (requestIndex < MAXIMUM_INFLIGHT_AUTO_INVOKES) {
                            ChatMessages currentMessages = messages;
                            if (e instanceof FunctionInvocationError) {
                                currentMessages.assertCommonHistory(
                                    ((FunctionInvocationError) e).getMessages());

                                currentMessages = new ChatMessages(
                                    ((FunctionInvocationError) e).getMessages());
                            }
                            return internalChatMessageContentsAsync(
                                currentMessages,
                                kernel,
                                functions,
                                invocationContext,
                                requestIndex + 1);
                        } else {
                            return Mono.error(e);
                        }
                    });
            });
    }

    private Mono<ChatMessages> performToolCall(
        @Nullable Kernel kernel,
        @Nullable InvocationContext invocationContext,
        Mono<ChatMessages> requestMessages,
        ChatCompletionsToolCall toolCall) {

        return requestMessages
            .flatMap(messages -> {
                try {
                    // OpenAI only supports function tool call at the moment
                    ChatCompletionsFunctionToolCall functionToolCall = (ChatCompletionsFunctionToolCall) toolCall;
                    if (kernel == null) {
                        return Mono.error(new SKException(
                            "A tool call was requested, but no kernel was provided to the invocation, this is a unsupported configuration"));
                    }

                    ContextVariableTypes contextVariableTypes = invocationContext == null
                        ? new ContextVariableTypes()
                        : invocationContext.getContextVariableTypes();

                    return invokeFunctionTool(
                        kernel,
                        invocationContext,
                        functionToolCall,
                        contextVariableTypes)
                        .map(functionResult -> {
                            // Add chat request tool message to the chat options
                            ChatRequestMessage requestToolMessage = new ChatRequestToolMessage(
                                functionResult.getResult(),
                                functionToolCall.getId());

                            return messages.add(requestToolMessage);
                        })
                        .switchIfEmpty(Mono.fromSupplier(
                            () -> {
                                ChatRequestMessage requestToolMessage = new ChatRequestToolMessage(
                                    "Completed successfully with no return value",
                                    functionToolCall.getId());

                                return messages.add(requestToolMessage);
                            }))
                        .onErrorResume(e -> emitError(toolCall, messages, e));
                } catch (Exception e) {
                    return emitError(toolCall, messages, e);
                }
            });
    }

    private Mono<ChatMessages> emitError(
        ChatCompletionsToolCall toolCall,
        ChatMessages msgs,
        Throwable e) {
        msgs = msgs.add(new ChatRequestToolMessage(
            "Call failed: " + e.getMessage(),
            toolCall.getId()));

        return Mono.error(new FunctionInvocationError(e, msgs.allMessages));
    }

    /**
     * Exception to be thrown when a function invocation fails.
     */
    private static class FunctionInvocationError extends SKException {

        private final List<ChatRequestMessage> messages;

        public FunctionInvocationError(Throwable e, List<ChatRequestMessage> msgs) {
            super(e.getMessage(), e);
            this.messages = msgs;
        }

        public List<ChatRequestMessage> getMessages() {
            return messages;
        }
    }

    @SuppressWarnings("StringSplitter")
    private Mono<FunctionResult<String>> invokeFunctionTool(
        Kernel kernel,
        @Nullable InvocationContext invocationContext,
        ChatCompletionsFunctionToolCall toolCall,
        ContextVariableTypes contextVariableTypes) {

        try {
            FunctionCallContent FunctionCallContent = extractFunctionCallContent(toolCall);
            String pluginName = FunctionCallContent.getPluginName();
            if (pluginName == null || pluginName.isEmpty()) {
                return Mono.error(
                    new SKException("Plugin name is required for function tool call"));
            }

            KernelFunction<?> function = kernel.getFunction(
                pluginName,
                FunctionCallContent.getFunctionName());

            PreToolCallEvent hookResult = executeHook(invocationContext, kernel,
                new PreToolCallEvent(
                    FunctionCallContent.getFunctionName(),
                    FunctionCallContent.getArguments(),
                    function,
                    contextVariableTypes));

            function = hookResult.getFunction();
            KernelArguments arguments = hookResult.getArguments();

            return function
                .invokeAsync(kernel)
                .withArguments(arguments)
                .withTypes(invocationContext.getContextVariableTypes())
                .withTypes(contextVariableTypes)
                .withResultType(contextVariableTypes.getVariableTypeForClass(String.class));
        } catch (JsonProcessingException e) {
            return Mono.error(new SKException("Failed to parse tool arguments", e));
        }
    }

    private static <T extends KernelHookEvent> T executeHook(
        @Nullable InvocationContext invocationContext,
        @Nullable Kernel kernel,
        T event) {
        KernelHooks kernelHooks = null;
        if (kernel == null) {
            if (invocationContext != null) {
                kernelHooks = invocationContext.getKernelHooks();
            }
        } else {
            kernelHooks = KernelHooks.merge(
                kernel.getGlobalKernelHooks(),
                invocationContext != null ? invocationContext.getKernelHooks() : null);
        }
        if (kernelHooks == null) {
            return event;
        }
        return kernelHooks.executeHooks(event);
    }

    @SuppressWarnings("StringSplitter")
    private FunctionCallContent extractFunctionCallContent(
        ChatCompletionsFunctionToolCall toolCall)
        throws JsonProcessingException {

        // Split the full name of a function into plugin and function name
        String name = toolCall.getFunction().getName();
        String[] parts = name.split(OpenAIFunction.getNameSeparator());
        String pluginName = parts.length > 1 ? parts[0] : "";
        String fnName = parts.length > 1 ? parts[1] : parts[0];

        KernelArguments arguments = KernelArguments.builder().build();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonToolCallArguments = mapper.readTree(toolCall.getFunction().getArguments());

        jsonToolCallArguments.fields().forEachRemaining(
            entry -> {
                if (entry.getValue() instanceof ContainerNode) {
                    arguments.put(entry.getKey(),
                        ContextVariable.of(entry.getValue().toPrettyString()));
                } else {
                    arguments.put(entry.getKey(),
                        ContextVariable.of(entry.getValue().asText()));
                }
            });

        return new FunctionCallContent(
            fnName,
            pluginName,
            toolCall.getId(),
            arguments);
    }

    private List<OpenAIChatMessageContent<?>> getChatMessageContentsAsync(
        ChatCompletions completions) {
        FunctionResultMetadata<CompletionsUsage> completionMetadata = FunctionResultMetadata.build(
            completions.getId(),
            completions.getUsage(),
            completions.getCreatedAt());

        List<ChatResponseMessage> responseMessages = completions
            .getChoices()
            .stream()
            .map(ChatChoice::getMessage)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        List<OpenAIChatMessageContent<?>> chatMessageContent = responseMessages
            .stream()
            .map(response -> {
                try {
                    return new OpenAIChatMessageContent<>(
                        AuthorRole.ASSISTANT,
                        response.getContent(),
                        this.getModelId(),
                        null,
                        null,
                        completionMetadata,
                        formFunctionCallContents(response));
                } catch (SKCheckedException e) {
                    LOGGER.warn("Failed to form chat message content", e);
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        return chatMessageContent;
    }

    private List<ChatMessageContent<?>> toOpenAIChatMessageContent(
        List<ChatRequestMessage> requestMessages) throws SKCheckedException {
        try {
            return requestMessages
                .stream()
                .map(message -> {
                    if (message instanceof ChatRequestUserMessage) {
                        return new OpenAIChatMessageContent<>(
                            AuthorRole.USER,
                            BinaryDataUtils
                                .toString(((ChatRequestUserMessage) message).getContent()),
                            null,
                            null,
                            null,
                            null,
                            null);
                    } else if (message instanceof ChatRequestSystemMessage) {
                        return new OpenAIChatMessageContent<>(
                            AuthorRole.SYSTEM,
                            BinaryDataUtils
                                .toString(((ChatRequestSystemMessage) message).getContent()),
                            null,
                            null,
                            null,
                            null,
                            null);
                    } else if (message instanceof ChatRequestAssistantMessage) {
                        try {
                            List<FunctionCallContent> calls = getFunctionCallContents(
                                ((ChatRequestAssistantMessage) message).getToolCalls());
                            return new OpenAIChatMessageContent<>(
                                AuthorRole.ASSISTANT,
                                BinaryDataUtils
                                    .toString(((ChatRequestAssistantMessage) message).getContent()),
                                null,
                                null,
                                null,
                                null,
                                calls);
                        } catch (SKCheckedException e) {
                            throw SKException.build("Failed to form assistant message", e);
                        }
                    } else if (message instanceof ChatRequestToolMessage) {
                        return new OpenAIChatMessageContent<>(
                            AuthorRole.TOOL,
                            BinaryDataUtils
                                .toString(((ChatRequestToolMessage) message).getContent()),
                            null,
                            null,
                            null,
                            FunctionResultMetadata.build(
                                ((ChatRequestToolMessage) message).getToolCallId(),
                                null,
                                null),
                            null);
                    }

                    throw new SKException(
                        "Unknown message type: " + message.getClass().getSimpleName());
                })
                .collect(Collectors.toList());
        } catch (SKException e) {
            throw SKCheckedException.build("Failed to form OpenAI chat message content", e);
        }
    }

    @Nullable
    private List<FunctionCallContent> getFunctionCallContents(
        @Nullable List<ChatCompletionsToolCall> toolCalls) throws SKCheckedException {
        if (toolCalls == null || toolCalls.isEmpty()) {
            return null;
        }

        try {
            return toolCalls
                .stream()
                .map(call -> {
                    if (call instanceof ChatCompletionsFunctionToolCall) {
                        try {
                            return extractFunctionCallContent(
                                (ChatCompletionsFunctionToolCall) call);
                        } catch (JsonProcessingException e) {
                            throw SKException.build("Failed to parse tool arguments", e);
                        }
                    } else {
                        throw new SKException(
                            "Unknown tool call type: " + call.getClass().getSimpleName());
                    }
                })
                .collect(Collectors.toList());
        } catch (SKException e) {
            throw SKCheckedException.build("Failed to form tool call", e);
        }
    }

    @Nullable
    private List<FunctionCallContent> formFunctionCallContents(
        ChatResponseMessage response) throws SKCheckedException {
        if (response.getToolCalls() == null || response.getToolCalls().isEmpty()) {
            return null;
        }
        try {
            return response
                .getToolCalls()
                .stream()
                .map(call -> {
                    if (call instanceof ChatCompletionsFunctionToolCall) {
                        try {
                            return extractFunctionCallContent(
                                (ChatCompletionsFunctionToolCall) call);
                        } catch (JsonProcessingException e) {
                            throw SKException.build("Failed to parse tool arguments", e);
                        }
                    } else {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        } catch (SKException e) {
            throw SKCheckedException.build("Failed to form tool call", e);
        }
    }

    private static ChatCompletionsOptions getCompletionsOptions(
        ChatCompletionService chatCompletionService,
        List<ChatRequestMessage> chatRequestMessages,
        @Nullable InvocationContext invocationContext,
        @Nullable OpenAIToolCallConfig toolCallConfig) {

        chatRequestMessages = chatRequestMessages
            .stream()
            .map(OpenAiXMLPromptParser::unescapeRequest)
            .collect(Collectors.toList());

        ChatCompletionsOptions options = new ChatCompletionsOptions(chatRequestMessages)
            .setModel(chatCompletionService.getModelId());

        if (toolCallConfig != null) {
            options.setTools(toolCallConfig.getTools());
            options.setToolChoice(toolCallConfig.getToolChoice());

            if (toolCallConfig.getOptions() != null) {
                options.setParallelToolCalls(toolCallConfig.getOptions().isParallelCallsAllowed());
            }
        }

        PromptExecutionSettings promptExecutionSettings = invocationContext != null
            ? invocationContext.getPromptExecutionSettings()
            : null;

        if (promptExecutionSettings == null) {
            return options;
        }

        if (promptExecutionSettings.getResultsPerPrompt() < 1
            || promptExecutionSettings.getResultsPerPrompt() > MAX_RESULTS_PER_PROMPT) {
            throw new AIException(AIException.ErrorCodes.INVALID_REQUEST,
                String.format("Results per prompt must be in range between 1 and %d, inclusive.",
                    MAX_RESULTS_PER_PROMPT));
        }

        Map<String, Integer> logit = null;
        if (promptExecutionSettings.getTokenSelectionBiases() != null) {
            logit = promptExecutionSettings
                .getTokenSelectionBiases()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                    entry -> entry.getKey().toString(),
                    Map.Entry::getValue));
        }

        options
            .setTemperature(promptExecutionSettings.getTemperature())
            .setTopP(promptExecutionSettings.getTopP())
            .setPresencePenalty(promptExecutionSettings.getPresencePenalty())
            .setFrequencyPenalty(promptExecutionSettings.getFrequencyPenalty())
            .setPresencePenalty(promptExecutionSettings.getPresencePenalty())
            .setMaxTokens(promptExecutionSettings.getMaxTokens())
            .setN(promptExecutionSettings.getResultsPerPrompt())
            // Azure OpenAI WithData API does not allow to send empty array of stop sequences
            // Gives back "Validation error at #/stop/str: Input should be a valid string\nValidation error at #/stop/list[str]: List should have at least 1 item after validation, not 0"
            .setStop(promptExecutionSettings.getStopSequences() == null
                || promptExecutionSettings.getStopSequences().isEmpty() ? null
                    : promptExecutionSettings.getStopSequences())
            .setUser(promptExecutionSettings.getUser())
            .setLogitBias(logit);

        if (promptExecutionSettings.getResponseFormat() != null) {
            switch (promptExecutionSettings.getResponseFormat().getType()) {
                case JSON_SCHEMA:
                    JsonResponseSchema schema = ((JsonSchemaResponseFormat) promptExecutionSettings
                        .getResponseFormat())
                        .getJsonSchema();

                    options.setResponseFormat(new ChatCompletionsJsonSchemaResponseFormat(schema));
                    break;
                case JSON_OBJECT:
                    options.setResponseFormat(new ChatCompletionsJsonResponseFormat());
                    break;
                case TEXT:
                    options.setResponseFormat(new ChatCompletionsTextResponseFormat());
                    break;

                default:
                    throw new SKException(
                        "Unknown response format: " + promptExecutionSettings.getResponseFormat());
            }
        }

        return options;
    }

    @Nullable
    private static OpenAIToolCallConfig getToolCallConfig(
        @Nullable InvocationContext invocationContext,
        @Nullable List<OpenAIFunction> functions,
        List<ChatRequestMessage> chatRequestMessages,
        int requestIndex) {

        if (invocationContext == null || functions == null || functions.isEmpty()) {
            return null;
        }

        if (invocationContext.getFunctionChoiceBehavior() == null
            && invocationContext.getToolCallBehavior() == null) {
            return null;
        }

        if (invocationContext.getFunctionChoiceBehavior() != null) {
            return getFunctionChoiceBehaviorConfig(
                invocationContext.getFunctionChoiceBehavior(),
                functions,
                requestIndex);
        } else {
            return getToolCallBehaviorConfig(
                invocationContext.getToolCallBehavior(),
                functions,
                chatRequestMessages,
                requestIndex);
        }
    }

    @Nullable
    private static OpenAIToolCallConfig getFunctionChoiceBehaviorConfig(
        @Nullable FunctionChoiceBehavior functionChoiceBehavior,
        @Nullable List<OpenAIFunction> functions,
        int requestIndex) {
        if (functionChoiceBehavior == null) {
            return null;
        }

        if (functions == null || functions.isEmpty()) {
            return null;
        }

        ChatCompletionsToolSelection toolChoice;
        boolean autoInvoke;

        if (functionChoiceBehavior instanceof RequiredFunctionChoiceBehavior) {
            // After first request a required function must have been called already
            if (requestIndex >= 1) {
                return null;
            }

            toolChoice = new ChatCompletionsToolSelection(
                ChatCompletionsToolSelectionPreset.REQUIRED);
            autoInvoke = ((RequiredFunctionChoiceBehavior) functionChoiceBehavior).isAutoInvoke();
        } else if (functionChoiceBehavior instanceof AutoFunctionChoiceBehavior) {
            toolChoice = new ChatCompletionsToolSelection(ChatCompletionsToolSelectionPreset.AUTO);
            autoInvoke = ((AutoFunctionChoiceBehavior) functionChoiceBehavior).isAutoInvoke()
                && requestIndex < MAXIMUM_INFLIGHT_AUTO_INVOKES;
        } else if (functionChoiceBehavior instanceof NoneFunctionChoiceBehavior) {
            toolChoice = new ChatCompletionsToolSelection(ChatCompletionsToolSelectionPreset.NONE);
            autoInvoke = false;
        } else {
            throw new SKException(
                "Unsupported function choice behavior: " + functionChoiceBehavior);
        }

        // List of functions advertised to the model
        List<ChatCompletionsToolDefinition> toolDefinitions = functions.stream()
            .filter(function -> functionChoiceBehavior.isFunctionAllowed(function.getPluginName(),
                function.getName()))
            .map(OpenAIFunction::getFunctionDefinition)
            .map(it -> new ChatCompletionsFunctionToolDefinitionFunction(it.getName())
                .setDescription(it.getDescription())
                .setParameters(it.getParameters()))
            .map(ChatCompletionsFunctionToolDefinition::new)
            .collect(Collectors.toList());

        return new OpenAIToolCallConfig(
            toolDefinitions,
            toolChoice,
            autoInvoke,
            functionChoiceBehavior.getOptions());
    }

    @Nullable
    private static OpenAIToolCallConfig getToolCallBehaviorConfig(
        @Nullable ToolCallBehavior toolCallBehavior,
        @Nullable List<OpenAIFunction> functions,
        List<ChatRequestMessage> chatRequestMessages,
        int requestIndex) {

        if (toolCallBehavior == null) {
            return null;
        }

        if (functions == null || functions.isEmpty()) {
            return null;
        }

        List<ChatCompletionsToolDefinition> toolDefinitions;
        ChatCompletionsToolSelection toolChoice;

        // If a specific function is required to be called
        if (toolCallBehavior instanceof ToolCallBehavior.RequiredKernelFunction) {
            KernelFunction<?> requiredFunction = ((ToolCallBehavior.RequiredKernelFunction) toolCallBehavior)
                .getRequiredFunction();

            String toolChoiceName = String.format("%s%s%s",
                requiredFunction.getPluginName(),
                OpenAIFunction.getNameSeparator(),
                requiredFunction.getName());

            // If required tool call has already been called dont ask for it again
            boolean hasBeenExecuted = hasToolCallBeenExecuted(chatRequestMessages, toolChoiceName);
            if (hasBeenExecuted) {
                return null;
            }

            FunctionDefinition function = OpenAIFunction.toFunctionDefinition(
                requiredFunction.getMetadata(),
                requiredFunction.getPluginName());

            toolDefinitions = new ArrayList<>();
            toolDefinitions.add(new ChatCompletionsFunctionToolDefinition(
                new ChatCompletionsFunctionToolDefinitionFunction(function.getName())
                    .setDescription(function.getDescription())
                    .setParameters(function.getParameters())));

            try {
                String json = String.format(
                    "{\"type\":\"function\",\"function\":{\"name\":\"%s\"}}", toolChoiceName);

                toolChoice = new ChatCompletionsToolSelection(
                    ChatCompletionsNamedToolSelection.fromJson(
                        DefaultJsonReader.fromString(
                            json,
                            new JsonOptions())));
            } catch (JsonProcessingException e) {
                throw SKException.build("Failed to parse tool choice", e);
            } catch (IOException e) {
                throw new SKException(e);
            }
        }
        // If a set of functions are enabled to be called
        else {
            toolChoice = new ChatCompletionsToolSelection(ChatCompletionsToolSelectionPreset.AUTO);

            ToolCallBehavior.AllowedKernelFunctions enabledKernelFunctions = (ToolCallBehavior.AllowedKernelFunctions) toolCallBehavior;
            toolDefinitions = functions.stream()
                .filter(function -> {
                    // check if all kernel functions are enabled
                    if (enabledKernelFunctions.isAllKernelFunctionsAllowed()) {
                        return true;
                    }
                    // otherwise, check for the specific function
                    return enabledKernelFunctions.isFunctionAllowed(function.getPluginName(),
                        function.getName());
                })
                .map(OpenAIFunction::getFunctionDefinition)
                .map(it -> new ChatCompletionsFunctionToolDefinitionFunction(it.getName())
                    .setDescription(it.getDescription())
                    .setParameters(it.getParameters()))
                .map(ChatCompletionsFunctionToolDefinition::new)
                .collect(Collectors.toList());

            if (toolDefinitions.isEmpty()) {
                return null;
            }
        }

        return new OpenAIToolCallConfig(
            toolDefinitions,
            toolChoice,
            toolCallBehavior.isAutoInvokeAllowed()
                && requestIndex < Math.min(MAXIMUM_INFLIGHT_AUTO_INVOKES,
                    toolCallBehavior.getMaximumAutoInvokeAttempts()),
            null);
    }

    private static boolean hasToolCallBeenExecuted(List<ChatRequestMessage> chatRequestMessages,
        String toolChoiceName) {
        return chatRequestMessages
            .stream()
            .flatMap(message -> {
                // Extract tool calls
                if (message instanceof ChatRequestAssistantMessage) {
                    return ((ChatRequestAssistantMessage) message).getToolCalls().stream();
                }
                return Stream.empty();
            })
            .filter(toolCall -> {
                // Filter if tool call has correct name
                if (toolCall instanceof ChatCompletionsFunctionToolCall) {
                    return ((ChatCompletionsFunctionToolCall) toolCall).getFunction().getName()
                        .equals(toolChoiceName);
                }
                return false;
            })
            .allMatch(toolcall -> {
                String id = toolcall.getId();
                // True if tool call id has a response message
                return chatRequestMessages
                    .stream()
                    .filter(
                        chatRequestMessage -> chatRequestMessage instanceof ChatRequestToolMessage)
                    .anyMatch(
                        chatRequestMessage -> ((ChatRequestToolMessage) chatRequestMessage)
                            .getToolCallId()
                            .equals(id));
            });
    }

    private static List<ChatRequestMessage> getChatRequestMessages(
        List<? extends ChatMessageContent<?>> messages) {
        if (messages == null || messages.isEmpty()) {
            return new ArrayList<>();
        }
        return messages.stream()
            .map(OpenAIChatCompletion::getChatRequestMessage)
            .collect(Collectors.toList());
    }

    private static List<ChatRequestMessage> getChatRequestMessages(ChatHistory chatHistory) {
        return getChatRequestMessages(chatHistory.getMessages());
    }

    private static ChatRequestMessage getChatRequestMessage(
        ChatMessageContent<?> message) {

        AuthorRole authorRole = message.getAuthorRole();
        String content = message.getContent();

        if (message.getContentType() == ChatMessageContentType.IMAGE_URL && content != null) {
            return formImageMessage(message, content);
        }

        switch (authorRole) {
            case ASSISTANT:
                return formAssistantMessage(message, content);
            case SYSTEM:
                return new ChatRequestSystemMessage(content);
            case USER:
                return new ChatRequestUserMessage(content);
            case TOOL:
                String id = null;

                if (message.getMetadata() != null) {
                    id = message.getMetadata().getId();
                }

                if (id == null) {
                    throw new SKException(
                        "Require to create a tool call message, but no tool call id is available");
                }
                return new ChatRequestToolMessage(content, id);
            default:
                LOGGER.debug("Unexpected author role: {}", authorRole);
                throw new SKException("Unexpected author role: " + authorRole);
        }
    }

    private static ChatRequestUserMessage formImageMessage(ChatMessageContent<?> message,
        String content) {
        ChatMessageImageUrl imageUrl = new ChatMessageImageUrl(content);
        if (message instanceof ChatMessageImageContent) {
            ChatMessageImageDetailLevel detail = ChatMessageImageDetailLevel.fromString(
                ((ChatMessageImageContent<?>) message).getDetail().toString());
            imageUrl.setDetail(detail);
        }

        return new ChatRequestUserMessage(
            Collections.singletonList(new ChatMessageImageContentItem(imageUrl)));
    }

    private static ChatRequestAssistantMessage formAssistantMessage(
        ChatMessageContent<?> message,
        @Nullable String content) {
        // TODO: handle tools other than function calls
        ChatRequestAssistantMessage asstMessage = new ChatRequestAssistantMessage(content);

        List<FunctionCallContent> toolCalls = FunctionCallContent.getFunctionCalls(message);

        if (toolCalls != null) {
            asstMessage.setToolCalls(
                toolCalls.stream()
                    .map(toolCall -> {
                        KernelArguments arguments = toolCall.getArguments();

                        String args = arguments != null && !arguments.isEmpty()
                            ? arguments.entrySet().stream()
                                .map(entry -> String.format("\"%s\": \"%s\"",
                                    StringEscapeUtils.escapeJson(entry.getKey()),
                                    StringEscapeUtils.escapeJson(
                                        entry.getValue().toPromptString())))
                                .collect(Collectors.joining(",", "{", "}"))
                            : "{}";

                        String prefix = "";
                        if (toolCall.getPluginName() != null) {
                            prefix = toolCall.getPluginName() + OpenAIFunction.getNameSeparator();
                        }
                        String name = prefix + toolCall.getFunctionName();

                        FunctionCall fnCall = new FunctionCall(name, args);
                        return new ChatCompletionsFunctionToolCall(toolCall.getId(),
                            fnCall);
                    })
                    .collect(Collectors.toList()));
        }
        return asstMessage;
    }

    static ChatRequestMessage getChatRequestMessage(
        AuthorRole authorRole,
        String content) {

        switch (authorRole) {
            case ASSISTANT:
                return new ChatRequestAssistantMessage(content);
            case SYSTEM:
                return new ChatRequestSystemMessage(content);
            case USER:
                return new ChatRequestUserMessage(content);
            case TOOL:
                return new ChatRequestToolMessage(content, null);
            default:
                LOGGER.debug("Unexpected author role: " + authorRole);
                throw new SKException("Unexpected author role: " + authorRole);
        }

    }

    /**
     * Builder for creating a new instance of {@link OpenAIChatCompletion}.
     */
    public static class Builder
        extends OpenAiServiceBuilder<OpenAIAsyncClient, OpenAIChatCompletion, Builder> {

        @Override
        public OpenAIChatCompletion build() {

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

            return new OpenAIChatCompletion(client, deploymentName, modelId, serviceId);
        }
    }
}
