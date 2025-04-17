package com.microsoft.semantickernel.agents;

public class AgentResponseItem<T> {
    private final T message;
    private final AgentThread thread;

    public AgentResponseItem(T message, AgentThread thread) {
        this.message = message;
        this.thread = thread;
    }

    public T getMessage() {
        return message;
    }

    public AgentThread getThread() {
        return thread;
    }
}
