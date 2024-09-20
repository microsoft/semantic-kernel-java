// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.services;

import com.microsoft.semantickernel.orchestration.FunctionResultMetadata;
import javax.annotation.Nullable;

/**
 * Base class which represents the content returned by an AI service.
 *
 * @param <T> The type of the content.
 */
public interface KernelContent<T> {

    /*
     * The inner content representation. Use this to bypass the current
     * abstraction. The usage of this property is considered "unsafe".
     * Use it only if strictly necessary.
     */
    @Nullable
    T getInnerContent();

    /**
     * The metadata associated with the content.
     */
    @Nullable
    FunctionResultMetadata getMetadata();

    /**
     * Gets the content returned by the AI service.
     *
     * @return The content.
     */
    @Nullable
    String getContent();
}
