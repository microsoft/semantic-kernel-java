// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.contents;

import com.microsoft.semantickernel.orchestration.FunctionResultMetadata;
import com.microsoft.semantickernel.semanticfunctions.KernelArguments;
import com.microsoft.semantickernel.services.KernelContentImpl;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents the content of a function call.
 * <p>
 * This class is used to represent a function call in the context of a chat message.
 */
public class FunctionCallContent extends KernelContentImpl {

    @Nullable
    private final String id;
    @Nullable
    private final String pluginName;
    private final String functionName;
    @Nullable
    private final KernelArguments arguments;

    /**
     * Creates a new instance of the {@link FunctionCallContent} class.
     *
     * @param functionName The name of the function.
     * @param pluginName   The name of the plugin with which this function is associated, if any.
     * @param id           The ID of the tool call.
     * @param arguments    A name/value collection of the arguments to the function, if any.
     */
    public FunctionCallContent(
        String functionName,
        @Nullable String pluginName,
        @Nullable String id,
        @Nullable KernelArguments arguments) {
        this.functionName = functionName;
        this.pluginName = pluginName;
        this.id = id;
        if (arguments == null) {
            this.arguments = null;
        } else {
            this.arguments = arguments.copy();
        }
    }

    /**
     * Gets the ID of the tool call.
     *
     * @return The ID of the tool call.
     */
    @Nullable
    public String getId() {
        return id;
    }

    /**
     * Gets the name of the plugin with which this function is associated, if any.
     *
     * @return The name of the plugin with which this function is associated, if any.
     */
    @Nullable
    public String getPluginName() {
        return pluginName;
    }

    /**
     * Gets the name of the function.
     *
     * @return The name of the function.
     */
    public String getFunctionName() {
        return functionName;
    }

    /**
     * Gets a name/value collection of the arguments to the function, if any.
     *
     * @return A name/value collection of the arguments to the function, if any.
     */
    @Nullable
    public KernelArguments getArguments() {
        if (arguments == null) {
            return null;
        }
        return arguments.copy();
    }

    /**
     * Gets list of function calls from the message content.
     *
     * @param messageContent The message content.
     * @return The function calls.
     */
    public static List<FunctionCallContent> getFunctionCalls(ChatMessageContent<?> messageContent) {
        if (messageContent.getItems() == null) {
            return null;
        }

        return messageContent.getItems().stream().filter(
            item -> item instanceof FunctionCallContent)
            .map(item -> (FunctionCallContent) item)
            .collect(Collectors.toList());
    }

    /**
     * Gets the content returned by the AI service.
     *
     * @return The content.
     */
    @Nullable
    @Override
    public String getContent() {
        return null;
    }
}
