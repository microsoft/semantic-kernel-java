// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.hooks;

import com.microsoft.semantickernel.semanticfunctions.KernelFunction;
import com.microsoft.semantickernel.semanticfunctions.KernelFunctionArguments;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javax.annotation.Nullable;

/**
 * Represents a KernelHookEvent that is raised after a prompt is rendered.
 */
public class PromptRenderingEvent implements KernelHookEvent {

    private final KernelFunction<?> function;
    private final KernelFunctionArguments arguments;

    /**
     * Creates a new instance of the {@link PromptRenderingEvent} class.
     *
     * @param function  the function
     * @param arguments the arguments
     */
    public PromptRenderingEvent(KernelFunction<?> function,
        @Nullable KernelFunctionArguments arguments) {
        this.function = function;
        this.arguments = KernelFunctionArguments.builder().withVariables(arguments).build();
    }

    /**
     * Gets the function that was invoked.
     *
     * @return the function
     */
    public KernelFunction<?> getFunction() {
        return function;
    }

    /**
     * Gets the arguments that were passed to the function.
     *
     * @return the arguments
     */
    @SuppressFBWarnings("EI_EXPOSE_REP")
    public KernelFunctionArguments getArguments() {
        return arguments;
    }
}
