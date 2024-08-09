// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.agents;

import java.util.List;

import org.reactivestreams.Publisher;

import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;

import reactor.core.publisher.Mono;

/**
 * Contract for an agent that utilizes a {@link ChatHistoryChannel}.
 */
public interface ChatHistoryHandler extends AgentChannel {
    
    /**
     * Entry point for calling into an agent from a {@link ChatHistoryChannel}.
     *
     * @param history          The chat history at the point the channel is created.
     * @return A {@link Publisher} of {@link ChatMessageContent}.
     */
   Mono<List<ChatMessageContent<?>>> invokeAsync(ChatHistory history);

}
