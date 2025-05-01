// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.agents;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class AgentResponseItem<T> {
    private final T message;
    private final AgentThread thread;

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public AgentResponseItem(T message, AgentThread thread) {
        this.message = message;
        this.thread = thread;
    }

    /**
     * Gets the agent response message.
     *
     * @return The message.
     */
    public T getMessage() {
        return message;
    }

    /**
     * Gets the thread.
     *
     * @return The thread.
     */
    @SuppressFBWarnings("EI_EXPOSE_REP")
    public AgentThread getThread() {
        return thread;
    }
}
