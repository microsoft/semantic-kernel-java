package com.microsoft.semantickernel.agents;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.orchestration.InvocationContext;
import com.microsoft.semantickernel.orchestration.PromptExecutionSettings;
import com.microsoft.semantickernel.semanticfunctions.KernelArguments;
import com.microsoft.semantickernel.semanticfunctions.PromptTemplate;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Interface for a semantic kernel agent.
 */
public interface Agent {

    /**
     * Gets the agent's ID.
     *
     * @return The agent's ID
     */
    String getId();

    /**
     * Gets the agent's name.
     *
     * @return The agent's name
     */
    String getName();

    /**
     * Gets the agent's description.
     *
     * @return The agent's description
     */
    String getDescription();

    /**
     * Invokes the agent with the given message.
     *
     * @param message The message to process
     * @return A Mono containing the agent response
     */
    Mono<List<AgentResponseItem<ChatMessageContent<?>>>> invokeAsync(ChatMessageContent<?> message);

    /**
     * Invokes the agent with the given message and thread.
     *
     * @param message The message to process
     * @param thread The agent thread to use
     * @return A Mono containing the agent response
     */
    Mono<List<AgentResponseItem<ChatMessageContent<?>>>> invokeAsync(ChatMessageContent<?> message, AgentThread thread);

    /**
     * Invokes the agent with the given message, thread, and options.
     *
     * @param message The message to process
     * @param thread The agent thread to use
     * @param options The options for invoking the agent
     * @return A Mono containing the agent response
     */
    Mono<List<AgentResponseItem<ChatMessageContent<?>>>> invokeAsync(ChatMessageContent<?> message, AgentThread thread, AgentInvokeOptions options);

    /**
     * Invoke the agent with the given chat history.
     *
     * @param messages The chat history to process
     * @param thread The agent thread to use
     * @param options The options for invoking the agent
     * @return A Mono containing the agent response
     */
    Mono<List<AgentResponseItem<ChatMessageContent<?>>>> invokeAsync(List<ChatMessageContent<?>> messages, AgentThread thread, AgentInvokeOptions options);

    /**
     * Notifies the agent of a new message.
     *
     * @param thread The agent thread to use
     */
    Mono<Void> notifyThreadOfNewMessageAsync(AgentThread thread, ChatMessageContent<?> newMessage);
}