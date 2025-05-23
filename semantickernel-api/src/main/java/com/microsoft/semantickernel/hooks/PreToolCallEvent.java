// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.hooks;

import com.microsoft.semantickernel.contextvariables.ContextVariableTypes;
import com.microsoft.semantickernel.semanticfunctions.KernelFunction;
import com.microsoft.semantickernel.semanticfunctions.KernelArguments;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javax.annotation.Nullable;

/**
 * Represents a KernelHookEvent that is raised before a tool call is invoked.
 */
public class PreToolCallEvent implements KernelHookEvent {

    private final ContextVariableTypes contextVariableTypes;
    private final String functionName;
    @Nullable
    private final KernelArguments arguments;
    private final KernelFunction<?> function;

    /**
     * Creates a new instance of the {@link PreToolCallEvent} class.
     *
     * @param functionName         the name of the function
     * @param arguments            the arguments
     * @param function             the function
     * @param contextVariableTypes the context variable types
     */
    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public PreToolCallEvent(
        String functionName,
        @Nullable KernelArguments arguments,
        KernelFunction<?> function,
        ContextVariableTypes contextVariableTypes) {
        this.functionName = functionName;
        this.arguments = arguments;
        this.function = function;
        this.contextVariableTypes = contextVariableTypes;
    }

    /**
     * Gets the tool call arguments.
     * @return The tool call arguments.
     */
    @SuppressFBWarnings("EI_EXPOSE_REP")
    @Nullable
    public KernelArguments getArguments() {
        return arguments;
    }

    /**
     * Get the tool call function.
     * @return The tool call function.
     */
    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public KernelFunction<?> getFunction() {
        return function;
    }
}
