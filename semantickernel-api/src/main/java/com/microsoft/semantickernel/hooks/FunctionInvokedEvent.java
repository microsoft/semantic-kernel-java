// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.hooks;

import com.microsoft.semantickernel.orchestration.FunctionResult;
import com.microsoft.semantickernel.semanticfunctions.KernelFunction;
import com.microsoft.semantickernel.semanticfunctions.KernelArguments;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javax.annotation.Nullable;

/**
 * Represents a KernelHookEvent that is raised after a function is invoked.
 *
 * @param <T> The type of the function result
 */
public class FunctionInvokedEvent<T> implements KernelHookEvent {

    private final KernelFunction<T> function;
    @Nullable
    private final KernelArguments arguments;
    private final FunctionResult<T> result;

    /**
     * Creates a new instance of the {@link FunctionInvokedEvent} class.
     *
     * @param function  the function
     * @param arguments the arguments
     * @param result    the result
     */
    public FunctionInvokedEvent(
        KernelFunction<T> function,
        @Nullable KernelArguments arguments,
        FunctionResult<T> result) {
        this.function = function;
        this.arguments = KernelArguments.builder().withVariables(arguments).build();
        this.result = result;
    }

    /**
     * Gets the function that was invoked.
     *
     * @return the function
     */
    public KernelFunction<T> getFunction() {
        return function;
    }

    /**
     * Gets the arguments that were passed to the function.
     *
     * @return the arguments
     */
    @SuppressFBWarnings("EI_EXPOSE_REP")
    @Nullable
    public KernelArguments getArguments() {
        return arguments;
    }

    /**
     * Gets the result of the function invocation.
     *
     * @return the result
     */
    @SuppressFBWarnings("EI_EXPOSE_REP")
    public FunctionResult<T> getResult() {
        return result;
    }
}