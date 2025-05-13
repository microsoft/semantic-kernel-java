// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.aiservices.openai.chatcompletion;

import com.microsoft.semantickernel.contents.FunctionCallContent;
import com.microsoft.semantickernel.orchestration.FunctionResultMetadata;
import com.microsoft.semantickernel.services.KernelContent;
import com.microsoft.semantickernel.services.chatcompletion.AuthorRole;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

/**
 * Represents the content of a chat message.
 *
 * @param <T> The type of the inner content.
 */
public class OpenAIChatMessageContent<T> extends ChatMessageContent<T> {

    @Deprecated
    @Nullable
    private final List<OpenAIFunctionToolCall> toolCall;

    /**
     * Creates a new instance of the {@link OpenAIChatMessageContent} class.
     *
     * @param authorRole   The author role that generated the content.
     * @param content      The content.
     * @param modelId      The model id.
     * @param innerContent The inner content.
     * @param encoding     The encoding.
     * @param metadata     The metadata.
     * @param functionCalls     The tool call.
     */
    public OpenAIChatMessageContent(
        AuthorRole authorRole,
        String content,
        @Nullable String modelId,
        @Nullable T innerContent,
        @Nullable Charset encoding,
        @Nullable FunctionResultMetadata<?> metadata,
        @Nullable List<? extends FunctionCallContent> functionCalls) {
        super(authorRole, content, (List<? extends KernelContent<T>>) functionCalls, modelId,
            innerContent, encoding, metadata);

        if (functionCalls == null) {
            this.toolCall = null;
        } else {
            // Keep OpenAIFunctionToolCall list for legacy
            this.toolCall = Collections.unmodifiableList(functionCalls.stream().map(t -> {
                if (t instanceof OpenAIFunctionToolCall) {
                    return (OpenAIFunctionToolCall) t;
                } else {
                    return new OpenAIFunctionToolCall(
                        t.getId(),
                        t.getPluginName(),
                        t.getFunctionName(),
                        t.getArguments());
                }
            }).collect(Collectors.toList()));
        }
    }

    /**
     * Gets any tool calls requested.
     *
     * @return The tool call.
     * 
     * @deprecated Use {@link FunctionCallContent#getFunctionCalls(ChatMessageContent)} instead.
     */
    @Deprecated
    @Nullable
    public List<OpenAIFunctionToolCall> getToolCall() {
        return toolCall;
    }
}
