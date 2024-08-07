// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.agents;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * A {@link KernelAgent} specialization bound to a {@link ChatHistoryChannel}.
 */
public abstract class ChatHistoryKernelAgent extends KernelAgent<ChatHistoryChannel> implements ChatHistoryHandler {

    /**
     * Construct a new {@link ChatHistoryKernelAgent} instance.
     *
     * @param id          The identifier of the agent.
     * @param name        The name of the agent.
     * @param description The description of the agent.
     * @param instructions The instructions for the agent.
     * @param kernel      The kernel.
     */
    public ChatHistoryKernelAgent(
            @Nullable String id,
            @Nullable String name,
            @Nullable String description,
            @Nullable String instructions,
            @Nullable Kernel kernel) {
        super(id, name, description, instructions, kernel);
    }

    @Override
    public List<String> getChannelKeys() {
        return Collections.singletonList(ChatHistoryChannel.class.getName());
    }

 
    @Override
    public Mono<ChatHistoryChannel> createChannelAsync() {
        return Mono.just(new ChatHistoryChannel());
    }

    /**
     * Invokes asynchronously.
     *
     * @param history  The chat history.
     * @return An asynchronous sequence of {@link ChatMessageContent}.
     */
    public abstract Flux<ChatMessageContent<?>> invokeAsync(ChatHistory history);

    public static abstract class Builder extends KernelAgent.Builder {
        // No additional properties.

        protected Builder() {
            super();
        }
    }

}
