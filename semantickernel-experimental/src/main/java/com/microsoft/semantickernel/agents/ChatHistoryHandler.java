// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.agents;

import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;

import reactor.core.publisher.Flux;

/**
 * Contract for an agent that utilizes a {@link ChatHistoryChannel}.
 */
public interface ChatHistoryHandler extends AgentChannel<ChatHistoryKernelAgent> {
    
    /**
     * Entry point for calling into an agent from a {@link ChatHistoryChannel}.
     *
     * @param history          The chat history at the point the channel is created.
     * @return A {@link Publisher} of {@link ChatMessageContent}.
     */
   Flux<ChatMessageContent<?>> invokeAsync(ChatHistory history);

}
