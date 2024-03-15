// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.orchestration;

import com.microsoft.semantickernel.contextvariables.ContextVariable;
import com.microsoft.semantickernel.contextvariables.ContextVariableType;
import javax.annotation.Nullable;

/**
 * The result of a function invocation.
 * <p>
 * This class is used to return the result of a function invocation. It contains the result of the
 * function invocation and metadata about the result.
 *
 * @param <T> The type of the result of the function invocation.
 */
public class FunctionResult<T> {

    private final ContextVariable<T> result;
    private final FunctionResultMetadata metadata;
    @Nullable
    private final Object unconvertedResult;

    /**
     * Create a new instance of FunctionResult.
     *
     * @param result            The result of the function invocation.
     * @param metadata          Metadata about the result of the function invocation.
     * @param unconvertedResult The result of the function invocation before it was converted to the
     *                          expected type.
     */
    public FunctionResult(
        ContextVariable<T> result,
        @Nullable FunctionResultMetadata metadata,
        @Nullable Object unconvertedResult) {
        this.result = result;
        this.metadata = metadata == null ? FunctionResultMetadata.empty() : metadata;
        this.unconvertedResult = unconvertedResult;
    }

    /**
     * Create a new instance of FunctionResult with no metadata.
     *
     * @param of                The result of the function invocation.
     * @param unconvertedResult The result of the function invocation before it was converted to the
     *                          expected type.
     */
    public FunctionResult(ContextVariable<T> of,
        @Nullable Object unconvertedResult) {
        this(of, FunctionResultMetadata.empty(), unconvertedResult);
    }

    /**
     * Get the result of the function invocation.
     * <em>NOTE: If you get a ClassCastException from this method,
     * try adding a result type with {@link FunctionInvocation#withResultType(ContextVariableType)}
     * )}</em>
     *
     * @return The result of the function invocation.
     * @throws ClassCastException If the result is not of the expected type.
     */
    @Nullable
    public T getResult() {
        return result.getValue();
    }

    /**
     * Get the result of the function invocation.
     *
     * @return The result of the function invocation.
     */
    public ContextVariable<T> getResultVariable() {
        return result;
    }

    /**
     * Get the metadata about the result of the function invocation.
     *
     * @return The metadata about the result of the function invocation.
     */
    public FunctionResultMetadata getMetadata() {
        return metadata;
    }

    /**
     * Get the result of the function invocation before it was converted to the expected type.
     *
     * @return The result of the function invocation before it was converted to the expected type.
     */
    @Nullable
    public Object getUnconvertedResult() {
        return unconvertedResult;
    }
}
