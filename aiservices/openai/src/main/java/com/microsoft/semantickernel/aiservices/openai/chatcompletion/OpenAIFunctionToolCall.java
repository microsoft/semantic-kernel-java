// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.aiservices.openai.chatcompletion;

import com.microsoft.semantickernel.contents.FunctionCallContent;
import com.microsoft.semantickernel.semanticfunctions.KernelArguments;
import javax.annotation.Nullable;

/**
 * Represents a call to a function in the OpenAI tool.
 *
 * @deprecated Use {@link FunctionCallContent} instead.
 */
@Deprecated
public class OpenAIFunctionToolCall extends FunctionCallContent {

    /**
     * Creates a new instance of the {@link OpenAIFunctionToolCall} class.
     *
     * @param id           The ID of the tool call.
     * @param pluginName   The name of the plugin with which this function is associated, if any.
     * @param functionName The name of the function.
     * @param arguments    A name/value collection of the arguments to the function, if any.
     */
    public OpenAIFunctionToolCall(
        @Nullable String id,
        @Nullable String pluginName,
        String functionName,
        @Nullable KernelArguments arguments) {
        super(functionName, pluginName, id, arguments);
    }
}
