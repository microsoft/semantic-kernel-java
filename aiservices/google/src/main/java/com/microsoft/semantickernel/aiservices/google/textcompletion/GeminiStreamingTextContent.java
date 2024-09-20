// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.aiservices.google.textcompletion;

import com.microsoft.semantickernel.services.StreamingTextContent;
import com.microsoft.semantickernel.services.textcompletion.TextContent;
import javax.annotation.Nullable;

/**
 * StreamingTextContent is a wrapper for TextContent that allows for streaming.
 */
public class GeminiStreamingTextContent extends StreamingTextContent<TextContent> {

    /**
     * Initializes a new instance of the {@code StreamingTextContent} class with a provided text
     * content.
     *
     * @param content The text content.
     */
    public GeminiStreamingTextContent(TextContent content) {
        super(content, 0, null, null);
    }

    @Override
    @Nullable
    public String getContent() {
        TextContent content = getInnerContent();
        if (content == null) {
            return null;
        }
        return content.getContent();
    }

}
