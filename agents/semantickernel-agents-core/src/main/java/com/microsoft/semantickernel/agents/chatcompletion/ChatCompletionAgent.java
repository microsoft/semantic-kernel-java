package com.microsoft.semantickernel.agents.chatcompletion;

import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.agents.AgentInvokeOptions;
import com.microsoft.semantickernel.agents.AgentResponseItem;
import com.microsoft.semantickernel.agents.AgentThread;
import com.microsoft.semantickernel.agents.KernelAgent;
import com.microsoft.semantickernel.builders.SemanticKernelBuilder;
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

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

public class ChatCompletionAgent extends KernelAgent {

    private ChatCompletionAgent(
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
    public Mono<List<AgentResponseItem<ChatMessageContent<?>>>> invokeAsync(
            List<ChatMessageContent<?>> messages,
            AgentThread thread,
            @Nullable AgentInvokeOptions options
    ) {
        return ensureThreadExistsWithMessagesAsync(messages, thread, ChatHistoryAgentThread::new)
                .cast(ChatHistoryAgentThread.class)
                .flatMap(agentThread -> {
                    // Extract the chat history from the thread
                    ChatHistory history = new ChatHistory(
                        agentThread.getChatHistory().getMessages()
                    );

                    // Invoke the agent with the chat history
                    return internalInvokeAsync(
                            history,
                            options
                    )
                    .flatMapMany(Flux::fromIterable)
                    // notify on the new thread instance
                    .concatMap(agentMessage -> {
                        // Set the author name for the message
                        agentMessage.setAuthorName(this.name);

                        return this.notifyThreadOfNewMessageAsync(agentThread, agentMessage).thenReturn(agentMessage);
                    })
                    .collectList()
                    .map(chatMessageContents ->
                            chatMessageContents.stream()
                                    .map(message -> new AgentResponseItem<ChatMessageContent<?>>(message, agentThread))
                                    .collect(Collectors.toList())
                    );
                });
    }

    private Mono<List<ChatMessageContent<?>>> internalInvokeAsync(
        ChatHistory history,
        @Nullable AgentInvokeOptions options
    ) {
        if (options == null) {
            options = new AgentInvokeOptions();
        }

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
                    : ToolCallBehavior.allowAllKernelFunctions(true);

            // Build base invocation context
            InvocationContext.Builder builder = InvocationContext.builder()
                    .withPromptExecutionSettings(executionSettings)
                    .withToolCallBehavior(toolCallBehavior)
                    .withReturnMode(InvocationReturnMode.NEW_MESSAGES_ONLY);

            if (invocationContext != null) {
                builder = builder
                        .withTelemetry(invocationContext.getTelemetry())
                        .withContextVariableConverter(invocationContext.getContextVariableTypes())
                        .withKernelHooks(invocationContext.getKernelHooks());
            }

            InvocationContext agentInvocationContext = builder.build();

            return renderInstructionsAsync(kernel, arguments, agentInvocationContext).flatMap(
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

                    // Add the chat history to the new chat
                    chat.addAll(history);

                    return chatCompletionService.getChatMessageContentsAsync(chat, kernel, agentInvocationContext);
                }
            );

        } catch (ServiceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public Mono<Void> notifyThreadOfNewMessageAsync(AgentThread thread, ChatMessageContent<?> message) {
        return Mono.defer(() -> {
            return thread.onNewMessageAsync(message);
        });
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
        private KernelArguments kernelArguments;
        private InvocationContext invocationContext;
        private String instructions;
        private PromptTemplate template;

        /**
         * Set the ID of the agent.
         *
         * @param id The ID of the agent.
         */
        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        /**
         * Set the name of the agent.
         *
         * @param name The name of the agent.
         */
        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        /**
         * Set the description of the agent.
         *
         * @param description The description of the agent.
         */
        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }

        /**
         * Set the kernel to use for the agent.
         *
         * @param kernel The kernel to use.
         */
        public Builder withKernel(Kernel kernel) {
            this.kernel = kernel;
            return this;
        }

        /**
         * Set the kernel arguments to use for the agent.
         *
         * @param KernelArguments The kernel arguments to use.
         */
        public Builder withKernelArguments(KernelArguments KernelArguments) {
            this.kernelArguments = KernelArguments;
            return this;
        }

        /**
         * Set the instructions for the agent.
         *
         * @param instructions The instructions for the agent.
         */
        public Builder withInstructions(String instructions) {
            this.instructions = instructions;
            return this;
        }

        /**
         * Set the invocation context for the agent.
         *
         * @param invocationContext The invocation context to use.
         */
        public Builder withInvocationContext(InvocationContext invocationContext) {
            this.invocationContext = invocationContext;
            return this;
        }

        /**
         * Set the template for the agent.
         *
         * @param template The template to use.
         */
        public Builder withTemplate(PromptTemplate template) {
            this.template = template;
            return this;
        }

        /**
         * Build the ChatCompletionAgent instance.
         *
         * @return The ChatCompletionAgent instance.
         */
        public ChatCompletionAgent build() {
            return new ChatCompletionAgent(
                id,
                name,
                description,
                kernel,
                    kernelArguments,
                invocationContext,
                instructions,
                template
            );
        }

        /**
         * Build the ChatCompletionAgent instance with the given prompt template config and factory.
         *
         * @param promptTemplateConfig The prompt template config to use.
         * @param promptTemplateFactory The prompt template factory to use.
         * @return The ChatCompletionAgent instance.
         */
        public ChatCompletionAgent build(PromptTemplateConfig promptTemplateConfig, PromptTemplateFactory promptTemplateFactory) {
            return new ChatCompletionAgent(
                id,
                name,
                description,
                kernel,
                    kernelArguments,
                invocationContext,
                promptTemplateConfig.getTemplate(),
                promptTemplateFactory.tryCreate(promptTemplateConfig)
            );
        }
    }
}
