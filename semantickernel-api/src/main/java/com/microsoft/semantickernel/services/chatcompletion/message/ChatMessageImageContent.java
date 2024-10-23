// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.services.chatcompletion.message;

import com.microsoft.semantickernel.builders.SemanticKernelBuilder;
import com.microsoft.semantickernel.exceptions.SKException;
import com.microsoft.semantickernel.services.chatcompletion.AuthorRole;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;
import java.net.URL;
import java.util.Base64;
import javax.annotation.Nullable;

/**
 * Represents an image content in a chat message.
 *
 * @param <T> the type of the inner content within the message
 */
public class ChatMessageImageContent<T> extends ChatMessageContent<T> {

    private final ImageDetail detail;

    /**
     * Create a new instance of the {@link ChatMessageImageContent} class.
     * @param content The chat message content
     * @param modelId The LLM id to use for the chat
     * @param detail The detail level of the image to include in the chat message
     */
    public ChatMessageImageContent(
        String content,
        @Nullable String modelId,
        @Nullable ImageDetail detail) {
        super(
            AuthorRole.USER,
            content,
            modelId,
            null,
            null,
            null,
            ChatMessageContentType.IMAGE_URL);

        if (detail == null) {
            detail = ImageDetail.AUTO;
        }
        this.detail = detail;
    }

    /**
     * Get the detail level of the image to include in the chat message.
     *
     * @return the detail level of the image
     */
    public ImageDetail getDetail() {
        return detail;
    }

    /**
     * The detail level of the image to include in the chat message.
     */
    public enum ImageDetail {
        /**
         * Low detail
         */
        LOW,
        /**
         * High detail
         */
        HIGH,
        /**
         * Automatically determine the detail level
         */
        AUTO
    }

    /**
     * Create a new builder for the {@link ChatMessageImageContent} class.
     *
     * @param <T> the type of the inner content within the messages
     * @return a new builder
     */
    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    /**
     * Builder for the {@link ChatMessageImageContent} class.
     * @param <T> the type of the inner content within the message
     */
    public static class Builder<T> implements SemanticKernelBuilder<ChatMessageImageContent<T>> {

        @Nullable
        private String modelId = null;
        @Nullable
        private String content = null;
        @Nullable
        private ImageDetail detail = null;

        /**
         * Set the model ID to use for the chat message.
         *
         * @param modelId the model ID
         * @return {@code this} builder
         */
        public Builder<T> withModelId(String modelId) {
            this.modelId = modelId;
            return this;
        }

        /**
         * Set the image content to include in the chat message.
         * @param imageType For instance jpg or png. For known types known to OpenAI see: <a
         *                  href="https://platform.openai.com/docs/guides/vision/what-type-of-files-can-i-upload">docs</a>.
         * @param content   the image content
         * @return {@code this} builder
         */
        public Builder<T> withImage(
            String imageType,
            byte[] content) {
            this.content = String.format("data:image/%s;base64,%s",
                imageType,
                Base64.getEncoder().encodeToString(content));
            return this;
        }

        /**
         * Set the URL of the image to include in the chat message.
         *
         * @param url the URL of the image
         * @return {@code this} builder
         */
        public Builder<T> withImageUrl(String url) {
            this.content = url;
            return this;
        }

        /**
         * Set the URL of the image to include in the chat message.
         *
         * @param url the URL of the image
         * @return {@code this} builder
         */
        public Builder<T> withImageUrl(URL url) {
            this.content = url.toString();
            return this;
        }

        /**
         * Set the detail level of the image to include in the chat message.
         *
         * @param detail the detail level of the image
         * @return {@code this} builder
         */
        public Builder<T> withDetail(ImageDetail detail) {
            this.detail = detail;
            return this;
        }

        @Override
        public ChatMessageImageContent<T> build() {
            if (detail == null) {
                detail = ImageDetail.AUTO;
            }
            if (content == null) {
                throw new SKException("Image content is required");
            }
            return new ChatMessageImageContent<>(
                content,
                modelId,
                detail);
        }
    }
}
