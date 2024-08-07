// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.agents;

import java.util.List;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;

import reactor.core.publisher.Flux;

/**
 * Point of interaction for one or more agents.
 * 
 * An AgentChat instance does not support concurrent invocation and
 * are synchronized using {@code java.util.concurrent.locks.Lock}. Any 
 * thread attempting to invoke a public method while another thread is
 * holding the lock will block until the lock is released.
 */
public abstract class AgentChat {


    protected AgentChat() {
    }
    
    /**
     * Exposes the internal history to subclasses.
     */
    protected ChatHistory getHistory() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Process a series of interactions between the agents participating in this chat.
     * 
     * @return Asynchronous enumeration of messages.
     */
    public abstract List<ChatMessageContent<?>> invokeAsync();

    /**
     * Retrieve the chat history.
     * 
     * @return The message history
     */
    public Flux<ChatMessageContent<?>> getChatMessagesAsync() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Retrieve the message history, either the primary history or
     * an agent specific version.
     * 
     * @param agent An optional agent, if requesting an agent history.
     * @return The message history
     * 
     * Any AgentChat instance does not support concurrent invocation and
     * will throw exception if concurrent activity is attempted.
     */
    public Flux<ChatMessageContent<?>> getChatMessagesAsync(Agent<?> agent) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Append a message to the conversation. Adding a message while an agent is active is not allowed.
     *
     * @param message A non-system message with which to append to the conversation.
     * @throws KernelException if a system message is present, without taking any other action
     * @throws KernelException chat has current activity.
     */
    public void addChatMessage(ChatMessageContent<?> message) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Append messages to the conversation. Adding messages while an agent is active is not allowed.
     *
     * @param messages Set of non-system messages with which to append to the conversation.
     * @throws KernelException if a system message is present, without taking any other action
     * @throws KernelException chat has current activity.
     */
    public void addChatMessages(List<ChatMessageContent<?>> messages) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Process a discrete incremental interaction between a single Agent an a AgentChat.
     *
     * @param agent The agent actively interacting with the chat.
     * @return Asynchronous enumeration of messages.
     */
    protected Flux<ChatMessageContent<?>> invokeAgentAsync(Agent<?> agent) {
        throw new UnsupportedOperationException("Not implemented");
    }

}
