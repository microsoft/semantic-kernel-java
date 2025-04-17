package com.microsoft.semantickernel.agents;

public abstract class BaseAgentThread implements AgentThread {

    protected String id;
    protected boolean isDeleted;

    public BaseAgentThread() {
    }

    public BaseAgentThread(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }
    @Override
    public boolean isDeleted() {
        return isDeleted;
    }
}
