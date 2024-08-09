// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.agents;

import javax.annotation.Nullable;

import com.microsoft.semantickernel.Kernel;

/**
 * Base class for agents utilizing {@link Microsoft.SemanticKernel.Kernel} plugins or services.
 * @param  The type of {@code AgentChannel} associated with the agent.
 */
public abstract class KernelAgent extends Agent  {
    
    private final String instructions;
    private final Kernel kernel;
   
    protected KernelAgent(
        @Nullable String id, 
            @Nullable String name,
            @Nullable String description,
            @Nullable String instructions,
            Kernel kernel) {
        super(id, name, description);
        this.instructions = instructions;
        this.kernel = kernel;
    }

    /**
     * The instructions of the agent (optional).
     */
    public String getInstructions() {
        return instructions;
    }

    /**
     * The {@link Kernel} containing services, plugins, and filters for use throughout the agent lifetime.
     * Defaults to empty Kernel, but may be overridden.
     */
    public Kernel getKernel() {
        return kernel;

    }

    /**
     * Builder for {@link KernelAgent} instances.
     */
    public abstract static class Builder extends Agent.Builder<KernelAgent> {
       
        protected String instructions;
        protected Kernel kernel;

        public Builder withInstructions(String instructions) {
            this.instructions = instructions;
            return this;
        }

        public Builder withKernel(Kernel kernel) {
            this.kernel = kernel;
            return this;
        }

        protected Builder() {
            super();
        }
    }
}
