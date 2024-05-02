// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.services.textcompletion;

import com.microsoft.semantickernel.orchestration.FunctionResultMetadata;
import com.microsoft.semantickernel.services.KernelContent;

/**
 * Content from a text completion service.
 */
public class TextContent extends KernelContent<String> {

    private final String content;

    /**
     * Initializes a new instance of the {@code TextContent} class with a provided content, model
     * ID, and metadata.
     *
     * @param content  The content.
     * @param modelId  The model ID.
     * @param metadata The metadata.
     */
    public TextContent(
        String content,
        String modelId,
        FunctionResultMetadata metadata) {
        super(content, modelId, metadata);
        this.content = content;
    }

    /**
     * Gets the content.
     *
     * @return The content.
     */
    public String getValue() {
        return content;
    }

    @Override
    public String getContent() {
        return content;
    }
}
