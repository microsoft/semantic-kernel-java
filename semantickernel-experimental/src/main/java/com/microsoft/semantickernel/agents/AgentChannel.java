// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.agents;

import java.util.List;

import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;

import reactor.core.publisher.Flux;

/**
 * Defines the communication protocol for a particular {@code Agent} type.
 * An {@code Agent} provides its own {@code AgentChannel} via
 * {@link Agent#createChannelAsync()}.
 * @param <TAgent> The type of agent that this channel is associated with.
 */
public interface AgentChannel<TAgent extends Agent<? extends AgentChannel<TAgent>>> {


    /**
     * Receive the conversation messages. Used when joining a conversation and also during each agent interaction.
     * @param history The chat history at the point the channel is created.
     */
    void receiveAsync(List<ChatMessageContent<?>> history);

    /**
     * Perform a discrete incremental interaction between a single Agent and AgentChat.
     * @param agent The agent actively interacting with the chat.
     * @return Asynchronous enumeration of messages.
     */
    Flux<ChatMessageContent<?>> invokeAsync(TAgent agent);

    /**
     * Retrieve the message history specific to this channel.
     * @return Asynchronous enumeration of messages.
     */
    Flux<ChatMessageContent<?>> getHistoryAsync();
}
