package com.microsoft.semantickernel.agents;

import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.builders.SemanticKernelBuilder;
import com.microsoft.semantickernel.orchestration.InvocationContext;
import com.microsoft.semantickernel.semanticfunctions.KernelArguments;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import javax.annotation.Nullable;

/**
 * Options for invoking an agent.
 */
public class AgentInvokeOptions {

    @Nullable
    private final KernelArguments kernelArguments;
    @Nullable
    private final Kernel kernel;
    @Nullable
    private final String additionalInstructions;
    @Nullable
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
     * @param kernelArguments The arguments for the kernel function.
     * @param kernel The kernel to use.
     * @param additionalInstructions Additional instructions for the agent.
     * @param invocationContext The invocation context.
     */
    public AgentInvokeOptions(@Nullable KernelArguments kernelArguments,
                              @Nullable Kernel kernel,
                              @Nullable String additionalInstructions,
                              @Nullable InvocationContext invocationContext) {
        this.kernelArguments = kernelArguments != null ? kernelArguments.copy() : null;
        this.kernel = kernel;
        this.additionalInstructions = additionalInstructions;
        this.invocationContext = invocationContext;
    }

    /**
     * Get the kernel arguments.
     *
     * @return The kernel arguments.
     */
    @SuppressFBWarnings("EI_EXPOSE_REP")
    public KernelArguments getKernelArguments() {
        return kernelArguments;
    }

    /**
     * Get the kernel.
     *
     * @return The kernel.
     */
    public Kernel getKernel() {
        return kernel;
    }

    /**
     * Get additional instructions.
     *
     * @return The additional instructions.
     */
    public String getAdditionalInstructions() {
        return additionalInstructions;
    }

    /**
     * Get the invocation context.
     *
     * @return The invocation context.
     */
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
        @SuppressFBWarnings("EI_EXPOSE_REP2")
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