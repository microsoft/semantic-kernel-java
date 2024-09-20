// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.aiservices.openai.chatcompletion;

import com.microsoft.semantickernel.orchestration.FunctionResultMetadata;
import com.microsoft.semantickernel.services.chatcompletion.AuthorRole;
import com.microsoft.semantickernel.services.chatcompletion.StreamingChatContent;
import java.nio.charset.Charset;
import java.util.List;
import javax.annotation.Nullable;

public class OpenAIStreamingChatMessageContent<T> extends OpenAIChatMessageContent<T> implements
    StreamingChatContent<T> {

    private final String id;

    public OpenAIStreamingChatMessageContent(
        String id,
        AuthorRole authorRole,
        String content,
        @Nullable String modelId,
        @Nullable T innerContent,
        @Nullable Charset encoding,
        @Nullable FunctionResultMetadata metadata,
        @Nullable List<OpenAIFunctionToolCall> toolCall) {
        super(
            authorRole,
            content,
            modelId,
            innerContent,
            encoding,
            metadata,
            toolCall);

        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }
}
