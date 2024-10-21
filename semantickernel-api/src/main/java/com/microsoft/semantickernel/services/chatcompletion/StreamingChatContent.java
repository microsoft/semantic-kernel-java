// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.services.chatcompletion;

import com.microsoft.semantickernel.services.StreamingKernelContent;

/**
 * Base class which represents the content returned by a chat completion service.
 * @param <T> The type of the content.
 */
public interface StreamingChatContent<T> extends StreamingKernelContent<T> {

    /**
     * Gets the ID of the content.
     * @return The ID.
     */
    public String getId();
}
