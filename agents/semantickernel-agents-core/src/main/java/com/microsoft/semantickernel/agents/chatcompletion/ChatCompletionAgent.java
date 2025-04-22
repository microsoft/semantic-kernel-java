package com.microsoft.semantickernel.agents.chatcompletion;

import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.agents.AgentInvokeOptions;
import com.microsoft.semantickernel.agents.AgentResponseItem;
import com.microsoft.semantickernel.agents.AgentThread;
import com.microsoft.semantickernel.agents.KernelAgent;
import com.microsoft.semantickernel.builders.SemanticKernelBuilder;
import com.microsoft.semantickernel.contextvariables.ContextVariable;
import com.microsoft.semantickernel.orchestration.InvocationContext;
import com.microsoft.semantickernel.orchestration.InvocationReturnMode;
import com.microsoft.semantickernel.orchestration.PromptExecutionSettings;
import com.microsoft.semantickernel.orchestration.ToolCallBehavior;
import com.microsoft.semantickernel.semanticfunctions.KernelArguments;
import com.microsoft.semantickernel.semanticfunctions.PromptTemplate;
import com.microsoft.semantickernel.semanticfunctions.PromptTemplateConfig;
import com.microsoft.semantickernel.semanticfunctions.PromptTemplateFactory;
import com.microsoft.semantickernel.services.ServiceNotFoundException;
import com.microsoft.semantickernel.services.chatcompletion.AuthorRole;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ChatCompletionAgent extends KernelAgent {

    ChatCompletionAgent(
        String id,
        String name,
        String description,
        Kernel kernel,
        KernelArguments kernelArguments,
        InvocationContext context,
        String instructions,
        PromptTemplate template
    ) {
        super(
            id,
            name,
            description,
            kernel,
            kernelArguments,
            context,
            instructions,
            template
        );
    }

    /**
     * Invoke the agent with the given chat history.
     *
     * @param messages The chat history to process
     * @param thread   The agent thread to use
     * @param options  The options for invoking the agent
     * @return A Mono containing the agent response
     */
    @Override
    public Mono<List<AgentResponseItem<ChatMessageContent<?>>>> invokeAsync(List<ChatMessageContent<?>> messages, AgentThread thread, AgentInvokeOptions options) {

        Mono<ChatHistory> chatHistoryFromThread = this.<ChatHistoryAgentThread>ensureThreadExistsAsync(messages, thread, ChatHistoryAgentThread::new)
            .cast(ChatHistoryAgentThread.class)
            .map(ChatHistoryAgentThread::getChatHistory)
            .flatMap(threadChatHistory -> {
                return Mono.just(new ChatHistory(threadChatHistory.getMessages()));
        });


        Mono<List<ChatMessageContent<?>>> updatedChatHistory = chatHistoryFromThread.flatMap(
            chatHistory -> internalInvokeAsync(
                this.getName(),
                chatHistory,
                options
            )
        );

        return updatedChatHistory.flatMap(chatMessageContents -> {
            return Flux.fromIterable(chatMessageContents)
                        .concatMap(chatMessageContent -> this.notifyThreadOfNewMessageAsync(thread, chatMessageContent))
                        .then(Mono.just(chatMessageContents)); // return the original list
        }).flatMap(chatMessageContents -> {
            return Mono.just(chatMessageContents.stream()
                .map(chatMessageContent -> {
                    return new AgentResponseItem<ChatMessageContent<?>>(
                            chatMessageContent,
                            thread);
                }).collect(Collectors.toList()));
        });
    }

    private Mono<List<ChatMessageContent<?>>> internalInvokeAsync(
        String agentName,
        ChatHistory history,
        AgentInvokeOptions options
    ) {
        final Kernel kernel = options.getKernel() != null ? options.getKernel() : this.kernel;
        final KernelArguments arguments = mergeArguments(options.getKernelArguments());
        final String additionalInstructions = options.getAdditionalInstructions();
        final InvocationContext invocationContext = options.getInvocationContext() != null ? options.getInvocationContext() : this.invocationContext;

        try {
            ChatCompletionService chatCompletionService = kernel.getService(ChatCompletionService.class, arguments);

            PromptExecutionSettings executionSettings = invocationContext != null && invocationContext.getPromptExecutionSettings() != null
                    ? invocationContext.getPromptExecutionSettings()
                    : kernelArguments.getExecutionSettings().get(chatCompletionService.getServiceId());

            ToolCallBehavior toolCallBehavior = invocationContext != null
                    ? invocationContext.getToolCallBehavior()
                    : ToolCallBehavior.allowAllKernelFunctions(false);

            // Build base invocation context
            InvocationContext.Builder builder = InvocationContext.builder()
                    .withPromptExecutionSettings(executionSettings)
                    .withToolCallBehavior(toolCallBehavior)
                    .withReturnMode(InvocationReturnMode.FULL_HISTORY);

            if (invocationContext != null) {
                builder = builder
                        .withTelemetry(invocationContext.getTelemetry())
                        .withContextVariableConverter(invocationContext.getContextVariableTypes())
                        .withKernelHooks(invocationContext.getKernelHooks());
            }

            InvocationContext agentInvocationContext = builder.build();

            return formatInstructionsAsync(kernel, arguments, agentInvocationContext).flatMap(
                instructions -> {
                    // Create a new chat history with the instructions
                    ChatHistory chat = new ChatHistory(
                        instructions
                    );

                    // Add agent additional instructions
                    if (additionalInstructions != null) {
                        chat.addMessage(new ChatMessageContent<>(
                                AuthorRole.SYSTEM,
                                additionalInstructions
                        ));
                    }

                    chat.addAll(history);
                    int previousHistorySize = chat.getMessages().size();

                    return chatCompletionService.getChatMessageContentsAsync(chat, kernel, agentInvocationContext)
                            .map(chatMessageContents -> {
                                return chatMessageContents.subList(
                                        previousHistorySize,
                                        chatMessageContents.size());
                            });
                }
            );


        } catch (ServiceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Builder for creating instances of ChatCompletionAgent.
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder implements SemanticKernelBuilder<ChatCompletionAgent> {
        private String id;
        private String name;
        private String description;
        private Kernel kernel;
        private KernelArguments KernelArguments;
        private InvocationContext invocationContext;
        private String instructions;
        private PromptTemplate template;

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder withKernel(Kernel kernel) {
            this.kernel = kernel;
            return this;
        }

        public Builder withKernelArguments(KernelArguments KernelArguments) {
            this.KernelArguments = KernelArguments;
            return this;
        }

        public Builder withInstructions(String instructions) {
            this.instructions = instructions;
            return this;
        }

        public Builder withInvocationContext(InvocationContext invocationContext) {
            this.invocationContext = invocationContext;
            return this;
        }

        public Builder withTemplate(PromptTemplate template) {
            this.template = template;
            return this;
        }

        public ChatCompletionAgent build() {
            return new ChatCompletionAgent(
                id,
                name,
                description,
                kernel,
                KernelArguments,
                invocationContext,
                instructions,
                template
            );
        }

        public ChatCompletionAgent build(PromptTemplateConfig promptTemplateConfig, PromptTemplateFactory promptTemplateFactory) {
            return new ChatCompletionAgent(
                id,
                name,
                description,
                kernel,
                KernelArguments,
                invocationContext,
                promptTemplateConfig.getTemplate(),
                promptTemplateFactory.tryCreate(promptTemplateConfig)
            );
        }
    }
}
