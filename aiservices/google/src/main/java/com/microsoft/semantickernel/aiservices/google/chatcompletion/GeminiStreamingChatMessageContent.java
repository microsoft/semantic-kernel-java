// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.aiservices.google.chatcompletion;

import com.microsoft.semantickernel.orchestration.FunctionResultMetadata;
import com.microsoft.semantickernel.services.chatcompletion.AuthorRole;
import com.microsoft.semantickernel.services.chatcompletion.StreamingChatContent;
import java.nio.charset.Charset;
import java.util.List;
import javax.annotation.Nullable;

/**
 * Represents the content of a chat message.
 *
 * @param <T> The type of the inner content.
 */
public class GeminiStreamingChatMessageContent<T> extends GeminiChatMessageContent<T> implements
    StreamingChatContent<T> {

    private final String id;

    /**
     * Creates a new instance of the {@link GeminiChatMessageContent} class.
     *
     * @param authorRole          The author role that generated the content.
     * @param content             The content.
     * @param modelId             The model id.
     * @param innerContent        The inner content.
     * @param encoding            The encoding.
     * @param metadata            The metadata.
     * @param geminiFunctionCalls The function calls.
     */
    public GeminiStreamingChatMessageContent(AuthorRole authorRole, String content,
        @Nullable String modelId, @Nullable T innerContent, @Nullable Charset encoding,
        @Nullable FunctionResultMetadata metadata,
        @Nullable List<GeminiFunctionCall> geminiFunctionCalls,
        String id) {
        super(authorRole, content, modelId, innerContent, encoding, metadata, geminiFunctionCalls);
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }
}
