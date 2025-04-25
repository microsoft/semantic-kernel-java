package com.microsoft.semantickernel.agents;

import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.builders.SemanticKernelBuilder;
import com.microsoft.semantickernel.orchestration.InvocationContext;
import com.microsoft.semantickernel.semanticfunctions.KernelArguments;

import javax.annotation.Nullable;

/**
 * Options for invoking an agent.
 */
public class AgentInvokeOptions {

    private final KernelArguments KernelArguments;
    private final Kernel kernel;
    private final String additionalInstructions;
    private final InvocationContext invocationContext;

    /**
     * Default constructor for AgentInvokeOptions.
     */
    public AgentInvokeOptions() {
        this(null, null, null, null);
    }

    /**
     * Constructor for AgentInvokeOptions.
     *
     * @param KernelArguments The arguments for the kernel function.
     * @param kernel The kernel to use.
     * @param additionalInstructions Additional instructions for the agent.
     * @param invocationContext The invocation context.
     */
    public AgentInvokeOptions(@Nullable KernelArguments KernelArguments,
                              @Nullable Kernel kernel,
                              @Nullable String additionalInstructions,
                              @Nullable InvocationContext invocationContext) {
        this.KernelArguments = KernelArguments;
        this.kernel = kernel;
        this.additionalInstructions = additionalInstructions;
        this.invocationContext = invocationContext;
    }

    public KernelArguments getKernelArguments() {
        return KernelArguments;
    }

    public Kernel getKernel() {
        return kernel;
    }

    public String getAdditionalInstructions() {
        return additionalInstructions;
    }

    public InvocationContext getInvocationContext() {
        return invocationContext;
    }



    /**
     * Builder for AgentInvokeOptions.
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder implements SemanticKernelBuilder<AgentInvokeOptions> {

        private KernelArguments kernelArguments;
        private Kernel kernel;
        private String additionalInstructions;
        private InvocationContext invocationContext;

        /**
         * Set the kernel arguments.
         *
         * @param kernelArguments The kernel arguments.
         * @return The builder.
         */
        public Builder withKernelArguments(KernelArguments kernelArguments) {
            this.kernelArguments = kernelArguments;
            return this;
        }

        /**
         * Set the kernel.
         *
         * @param kernel The kernel.
         * @return The builder.
         */
        public Builder withKernel(Kernel kernel) {
            this.kernel = kernel;
            return this;
        }

        /**
         * Set additional instructions.
         *
         * @param additionalInstructions The additional instructions.
         * @return The builder.
         */
        public Builder withAdditionalInstructions(String additionalInstructions) {
            this.additionalInstructions = additionalInstructions;
            return this;
        }

        /**
         * Set the invocation context.
         *
         * @param invocationContext The invocation context.
         * @return The builder.
         */
        public Builder withInvocationContext(InvocationContext invocationContext) {
            this.invocationContext = invocationContext;
            return this;
        }

        /**
         * Build the object.
         *
         * @return a constructed object.
         */
        @Override
        public AgentInvokeOptions build() {
            return new AgentInvokeOptions(
                kernelArguments,
                kernel,
                additionalInstructions,
                invocationContext
            );
        }
    }
}