// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.services.chatcompletion.message;

import com.microsoft.semantickernel.builders.SemanticKernelBuilder;
import com.microsoft.semantickernel.exceptions.SKException;
import com.microsoft.semantickernel.orchestration.FunctionResultMetadata;
import com.microsoft.semantickernel.services.KernelContent;
import com.microsoft.semantickernel.services.chatcompletion.AuthorRole;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;
import java.nio.charset.Charset;
import java.util.List;
import javax.annotation.Nullable;

/**
 * Represents the content of a chat message which contains text
 */
public class ChatMessageTextContent extends ChatMessageContent<String> {

    /**
     * Creates a new instance of the {@link ChatMessageTextContent} class.
     *
     * @param authorRole the author role that generated the content
     * @param content    the content
     * @param modelId    the model id
     * @param encoding   the encoding of the content
     * @param metadata   the metadata
     */
    public ChatMessageTextContent(
        AuthorRole authorRole,
        String content,
        @Nullable String modelId,
        @Nullable Charset encoding,
        @Nullable FunctionResultMetadata metadata) {
        super(authorRole, content, modelId, null, encoding, metadata,
            ChatMessageContentType.TEXT);
    }

    /**
     * Create a new builder for the {@link ChatMessageTextContent} class.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    private static ChatMessageTextContent buildContent(AuthorRole role, String content) {
        return new Builder()
            .withAuthorRole(role)
            .withContent(content)
            .build();
    }

    /**
     * Create a message with the author role set to {@link AuthorRole#USER}
     *
     * @param content The content of the message
     * @return The message
     */
    public static ChatMessageTextContent userMessage(String content) {
        return buildContent(AuthorRole.USER, content);
    }

    /**
     * Create a message with the author role set to {@link AuthorRole#ASSISTANT}
     *
     * @param content The content of the message
     * @return The message
     */
    public static ChatMessageTextContent assistantMessage(String content) {
        return buildContent(AuthorRole.ASSISTANT, content);
    }

    /**
     * Create a message with the author role set to {@link AuthorRole#SYSTEM}
     *
     * @param content The content of the message
     * @return The message
     */
    public static ChatMessageTextContent systemMessage(String content) {
        return buildContent(AuthorRole.SYSTEM, content);
    }

    /**
     * Builder for the {@link ChatMessageTextContent} class.
     */
    public static class Builder implements SemanticKernelBuilder<ChatMessageTextContent> {

        @Nullable
        private String modelId = null;
        @Nullable
        private FunctionResultMetadata metadata = null;
        @Nullable
        private AuthorRole authorRole = null;
        @Nullable
        private String content = null;
        @Nullable
        private List<KernelContent<String>> items = null;
        @Nullable
        private Charset encoding = null;

        /**
         * Set the content of the message
         *
         * @param content The content of the message
         * @return The builder
         */
        public Builder withContent(String content) {
            this.content = content;
            return this;
        }

        /**
         * Set the model ID used to generate the content
         *
         * @param modelId The model ID
         * @return The builder
         */
        public Builder withModelId(String modelId) {
            this.modelId = modelId;
            return this;
        }

        /**
         * Set the metadata associated with the content
         *
         * @param metadata The metadata
         * @return The builder
         */
        public Builder withMetadata(FunctionResultMetadata metadata) {
            this.metadata = metadata;
            return this;
        }

        /**
         * Set the author role of the message
         *
         * @param authorRole The author role
         * @return The builder
         */
        public Builder withAuthorRole(AuthorRole authorRole) {
            this.authorRole = authorRole;
            return this;
        }

        /**
         * Set the encoding of the message
         *
         * @param encoding The encoding
         * @return The builder
         */
        public Builder withEncoding(Charset encoding) {
            this.encoding = encoding;
            return this;
        }

        @Override
        public ChatMessageTextContent build() {
            if (authorRole == null) {
                throw new SKException("Author role must be set");
            }
            if (content == null) {
                throw new SKException("Content must be set");
            }
            return new ChatMessageTextContent(
                authorRole,
                content,
                modelId,
                encoding,
                metadata);
        }
    }
}
