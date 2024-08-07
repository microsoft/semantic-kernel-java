// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.agents;

import java.util.List;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;

import reactor.core.publisher.Flux;

/**
 * A {@link AgentChannel} specialization that acts upon a {@link ChatHistoryHandler}.
 */
public class ChatHistoryChannel implements AgentChannel<ChatHistoryKernelAgent> {
    
    private final ChatHistory history;

    public ChatHistoryChannel(ChatHistory history) {
        this.history = history;
    }
    
    /**
     * Invokes the channel asynchronously.
     *
     * @param agent The agent.
     * @return An asynchronous stream of chat messages.
     */
    @Override
    public Flux<ChatMessageContent<?>> invokeAsync(ChatHistoryKernelAgent agent) {
        return Flux.error(new UnsupportedOperationException("Not implemented"));
    }

    /**
     * Receives chat messages asynchronously.
     *
     * @param history The chat message history.
     */
    @Override
    public void receiveAsync(List<ChatMessageContent<?>> history) {
        this.history.addAll(history);
    }

    /**
     * Gets the chat message history asynchronously.
     *
     * @return An asynchronous stream of chat messages.
     */
    @Override
    public Flux<ChatMessageContent<?>> getHistoryAsync() {
        return Flux.fromIterable(this.history.getMessages());
    }

    /**
     * Initializes a new instance of the {@link ChatHistoryChannel} class.
     */
    public ChatHistoryChannel() {
        this.history = new ChatHistory();
    }

}
