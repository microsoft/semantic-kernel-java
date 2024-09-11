// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.services;

import com.microsoft.semantickernel.orchestration.FunctionResultMetadata;
import javax.annotation.Nullable;

/**
 * Base class which represents the content returned by an AI service.
 *
 * @param <T> The type of the content.
 */
public abstract class KernelContentImpl<T> implements KernelContent<T> {

    /*
     * The inner content representation. Use this to bypass the current
     * abstraction. The usage of this property is considered "unsafe".
     * Use it only if strictly necessary.
     */
    @Nullable
    private final T innerContent;

    /**
     * The model ID used to generate the content.
     */
    @Nullable
    private final String modelId;

    /**
     * The metadata associated with the content.
     */
    @Nullable
    private final FunctionResultMetadata metadata;

    /**
     * Initializes a new instance of the {@link KernelContentImpl} class.
     *
     * @param innerContent The inner content representation.
     * @param modelId      The model identifier used to generate the content.
     * @param metadata     The metadata associated with the content.
     */
    public KernelContentImpl(
        @Nullable T innerContent,
        @Nullable String modelId,
        @Nullable FunctionResultMetadata metadata) {
        this.innerContent = innerContent;
        this.modelId = modelId;
        this.metadata = metadata;
    }

    /**
     * Gets the inner content representation.
     *
     * @return The inner content representation.
     */
    @Nullable
    @Override
    public T getInnerContent() {
        return innerContent;
    }

    /**
     * Gets the metadata associated with the content.
     *
     * @return The metadata associated with the content.
     */
    @Nullable
    @Override
    public FunctionResultMetadata getMetadata() {
        return metadata;
    }

}
