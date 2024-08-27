// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.exceptions;

import com.microsoft.semantickernel.localization.SemanticKernelResources;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * AI logic exception
 */
public class AIException extends SKException {

    /**
     * Error code
     */
    @Nonnull
    private final ErrorCodes errorCode;

    /**
     * Initializes a new instance of the {@link AIException} class.
     *
     * @param error The error code.
     */
    public AIException(@Nonnull ErrorCodes error) {
        this(error, null, null);
    }

    /**
     * Initializes a new instance of the {@link AIException} class.
     *
     * @param errorCode The error code.
     * @param message   The message.
     */
    public AIException(@Nonnull ErrorCodes errorCode, @Nullable String message) {
        this(errorCode, message, null);
    }

    /**
     * Initializes a new instance of the {@link AIException} class.
     *
     * @param errorCode      The error code.
     * @param message        The message.
     * @param innerException The cause of the exception.
     */
    public AIException(
        @Nonnull ErrorCodes errorCode,
        @Nullable String message,
        @Nullable Throwable innerException) {
        super(formatDefaultMessage(errorCode.getMessage(), message), innerException);
        this.errorCode = errorCode;
    }

    /**
     * Gets the error code.
     *
     * @return The error code.
     */
    public ErrorCodes getErrorCode() {
        return errorCode;
    }

    /**
     * Error codes
     */
    public enum ErrorCodes {
        /**
         * Unknown error.
         */
        UNKNOWN_ERROR(SemanticKernelResources.getString("unknown.error")),

        /**
         * No response.
         */
        NO_RESPONSE(SemanticKernelResources.getString("no.response")),
        /**
         * Access denied.
         */
        ACCESS_DENIED(SemanticKernelResources.getString("access.is.denied")),

        /**
         * Invalid request.
         */
        INVALID_REQUEST(SemanticKernelResources.getString("the.request.was.invalid")),
        /**
         * Invalid response.
         */
        INVALID_RESPONSE_CONTENT(
            SemanticKernelResources.getString("the.content.of.the.response.was.invalid")),

        /**
         * Throttling.
         */
        THROTTLING(SemanticKernelResources.getString("the.request.was.throttled")),
        /**
         * Request timeout.
         */
        REQUEST_TIMEOUT(SemanticKernelResources.getString("the.request.timed.out")),

        /**
         * Service error.
         */
        SERVICE_ERROR(SemanticKernelResources.getString("there.was.an.error.in.the.service")),

        /**
         * Model not available.
         */
        MODEL_NOT_AVAILABLE(
            SemanticKernelResources.getString("the.requested.model.is.not.available")),

        /**
         * Invalid configuration.
         */
        INVALID_CONFIGURATION(
            SemanticKernelResources.getString("the.supplied.configuration.was.invalid")),
        /**
         * Function type not supported.
         */
        FUNCTION_TYPE_NOT_SUPPORTED(
            SemanticKernelResources.getString("the.function.is.not.supported"));

        private final String message;

        ErrorCodes(String message) {
            this.message = message;
        }

        /**
         * Gets the error message.
         *
         * @return The error message.
         */
        public String getMessage() {
            return message;
        }
    }
}
