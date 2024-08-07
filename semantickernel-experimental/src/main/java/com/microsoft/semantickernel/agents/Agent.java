// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.agents;

import java.util.List;

import javax.annotation.Nullable;

import reactor.core.publisher.Mono;

/**
 * Base abstraction for all Semantic Kernel agents. An agent instance
 * may participate in one or more conversations, or {@link AgentChat}.
 * A conversation may include one or more agents.
 *
 * In addition to identity and descriptive meta-data, an {@link Agent}
 * must define its communication protocol, or {@link AgentChannel}.
 * 
 * @param <TChannel> The type of {@code AgentChannel} associated with the agent.
 */
public abstract class Agent<TChannel extends AgentChannel<? extends Agent<TChannel>>> {
    
    /**
     * The description of the agent (optional)
     */
    private final String description;

    /**
     * The identifier of the agent (optional).
     * Default to a random guid value, but may be overridden.
     */
    private final String id;

    /**
     * The name of the agent (optional)
     */
    private final String name;

    /**
     * Construct a new {@link Agent} instance.
     * @param id The identifier of the agent.
     * @param name The name of the agent.
     * @param description The description of the agent.
     */
    protected Agent(
        @Nullable String id,
        @Nullable String name,
        @Nullable String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    /**
     * Get the description of the agent.
     * @return The description of the agent.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get the identifier of the agent.
     * @return The identifier of the agent.
     */
    public String getId() {
        return id;
    }
    
    /**
     * Get the name of the agent.
     * @return The name of the agent.
     */
    public String getName() {
        return name;
    }

    /**
     * Set of keys to establish channel affinity.
     * Two specific agents of the same type may each require their own channel. This is
     * why the channel type alone is insufficient.
     * For example, two OpenAI Assistant agents each targeting a different Azure OpenAI endpoint
     * would require their own channel. In this case, the endpoint could be expressed as an additional key.
     */
    protected abstract List<String> getChannelKeys();

    /**
     * Produce the an {@link AgentChannel} appropriate for the agent type.
     * Every agent conversation, or {@link AgentChat}, will establish one or more
     * {@link AgentChannel} objects according to the specific {@link Agent} type.
     * 
     * @return An {@link AgentChannel} appropriate for the agent type.
     */
    protected abstract Mono<TChannel> createChannelAsync();

    /** 
     * Base class for agent builders.
     */
    public abstract static class Builder<TAgent extends Agent<?>> {
        
        protected String id;
        protected String name;
        protected String description;

        public Builder<TAgent> withId(String id) {
            this.id = id;
            return this;
        }

        public Builder<TAgent> withName(String name) {
            this.name = name;
            return this;
        }

        public Builder<TAgent> withDescription(String description) {
            this.description = description;
            return this;
        }

        public abstract TAgent build();

        protected Builder() {
        }
    }
}
