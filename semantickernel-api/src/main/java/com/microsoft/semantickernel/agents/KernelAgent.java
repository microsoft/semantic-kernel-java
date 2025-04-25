package com.microsoft.semantickernel.agents;

import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.orchestration.InvocationContext;
import com.microsoft.semantickernel.orchestration.InvocationReturnMode;
import com.microsoft.semantickernel.orchestration.PromptExecutionSettings;
import com.microsoft.semantickernel.semanticfunctions.KernelArguments;
import com.microsoft.semantickernel.semanticfunctions.PromptTemplate;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public abstract class KernelAgent implements Agent {

    protected final String id;
    protected final String name;
    protected final String description;
    protected final Kernel kernel;
    protected final KernelArguments kernelArguments;
    protected final InvocationContext invocationContext;
    protected final String instructions;
    protected final PromptTemplate template;

    protected KernelAgent(
            String id,
            String name,
            String description,
            Kernel kernel,
            KernelArguments kernelArguments,
            InvocationContext invocationContext,
            String instructions,
            PromptTemplate template
    ) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.name = name;
        this.description = description;
        this.kernel = kernel;
        this.kernelArguments = kernelArguments != null ? kernelArguments.copy() : null;
        this.invocationContext = invocationContext != null
                ? invocationContext : InvocationContext.builder().withReturnMode(InvocationReturnMode.FULL_HISTORY).build();
        this.instructions = instructions;
        this.template = template;
    }

    /**
     * Gets the agent's ID.
     *
     * @return The agent's ID
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the agent's name.
     *
     * @return The agent's name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the agent's description.
     *
     * @return The agent's description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the kernel used by the agent.
     *
     * @return The kernel used by the agent
     */
    public Kernel getKernel() {
        return kernel;
    }

    /**
     * Gets the invocation context used by the agent.
     *
     * @return The invocation context used by the agent
     */
    @SuppressFBWarnings("EI_EXPOSE_REP")
    public KernelArguments getKernelArguments() {
        return kernelArguments;
    }

    /**
     * Gets the invocation context used by the agent.
     *
     * @return The invocation context used by the agent
     */
    public String getInstructions() {
        return instructions;
    }

    /**
     * Gets the invocation context used by the agent.
     *
     * @return The invocation context used by the agent
     */
    public PromptTemplate getTemplate() {
        return template;
    }


    /**
     * Merges the provided arguments with the current arguments.
     * Provided arguments will override the current arguments.
     *
     * @param arguments The arguments to merge with the current arguments.
     */
    protected KernelArguments mergeArguments(KernelArguments arguments) {
        if (arguments == null) {
            return kernelArguments;
        }

        Map<String, PromptExecutionSettings> executionSettings = new HashMap<>(kernelArguments.getExecutionSettings());
        executionSettings.putAll(arguments.getExecutionSettings());

        return KernelArguments.builder()
                .withVariables(kernelArguments)
                .withVariables(arguments)
                .withExecutionSettings(executionSettings)
                .build();
    }

    /**
     * Formats the instructions using the provided kernel, arguments, and context.
     *
     * @param kernel    The kernel to use for formatting.
     * @param arguments The arguments to use for formatting.
     * @param context   The context to use for formatting.
     * @return A Mono that resolves to the formatted instructions.
     */
    protected Mono<String> renderInstructionsAsync(Kernel kernel, KernelArguments arguments, InvocationContext context) {
        if (template != null) {
            return template.renderAsync(kernel, arguments, context);
        } else {
            return Mono.just(instructions);
        }
    }

    protected <T extends AgentThread> Mono<T> ensureThreadExistsWithMessagesAsync(List<ChatMessageContent<?>> messages, AgentThread thread, Supplier<T> threadSupplier) {
        return Mono.defer(() -> {
            // Check if the thread already exists
            // If it does, we can work with a copy of it
            AgentThread newThread = thread == null ? threadSupplier.get() : thread.copy();

            return  newThread.createAsync()
                    .thenMany(Flux.fromIterable(messages))
                    .concatMap(message -> {
                        return notifyThreadOfNewMessageAsync(newThread, message)
                                .then(Mono.just(message));
                    })
                    .then(Mono.just((T) newThread));
        });
    }
}
