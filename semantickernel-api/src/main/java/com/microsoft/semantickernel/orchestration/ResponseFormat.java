// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.orchestration;

/**
 * Represents format of a chat completion response.
 */
public enum ResponseFormat {

    /**
     * The response is in JSON format.
     * Only valid for openai chat completion, with GPT-4 and gpt-3.5-turbo-1106+ models.
     */
    JSON_OBJECT,
    /**
     * The response is in text format.
     */
    TEXT;
}
