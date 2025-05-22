// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.agents;

import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;
import reactor.core.publisher.Mono;

/**
 * Interface for an agent thread.
 */
public interface AgentThread {
    /**
     * Get the thread ID.
     *
     * @return The thread ID.
     */
    String getId();

    /**
     * Create a new thread.
     *
     * @return A Mono containing the thread ID.
     */
    Mono<String> createAsync();

    /**
     * Delete the thread.
     *
     * @return A Mono indicating completion.
     */
    Mono<Void> deleteAsync();

    /**
     * Check if the thread is deleted.
     *
     * @return A Mono containing true if the thread is deleted, false otherwise.
     */
    boolean isDeleted();

    /**
     * Create a copy of the thread.
     *
     * @return A new instance of the thread.
     */
    AgentThread copy();

    /**
     * Handle a new message in the thread.
     *
     * @param newMessage The new message to handle.
     * @return A Mono indicating completion.
     */
    Mono<Void> onNewMessageAsync(ChatMessageContent<?> newMessage);
}