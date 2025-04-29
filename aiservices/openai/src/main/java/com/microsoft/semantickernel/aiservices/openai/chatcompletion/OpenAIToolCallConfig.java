package com.microsoft.semantickernel.aiservices.openai.chatcompletion;

import com.azure.ai.openai.models.ChatCompletionsToolDefinition;
import com.azure.ai.openai.models.ChatCompletionsToolSelection;
import com.microsoft.semantickernel.functionchoice.FunctionChoiceBehaviorOptions;

import javax.annotation.Nullable;
import java.util.List;

public class OpenAIToolCallConfig {
    private final List<ChatCompletionsToolDefinition> tools;
    private final ChatCompletionsToolSelection toolChoice;
    private final boolean autoInvoke;
    private final FunctionChoiceBehaviorOptions options;

    /**
     * Creates a new instance of the {@link OpenAIToolCallConfig} class.
     *
     * @param tools       The list of tools available for the call.
     * @param toolChoice  The tool selection strategy.
     * @param autoInvoke  Indicates whether to automatically invoke the tool.
     * @param options     Additional options for function choice behavior.
     */
    public OpenAIToolCallConfig(
            List<ChatCompletionsToolDefinition> tools,
            ChatCompletionsToolSelection toolChoice,
            boolean autoInvoke,
            @Nullable FunctionChoiceBehaviorOptions options) {
        this.tools = tools;
        this.toolChoice = toolChoice;
        this.autoInvoke = autoInvoke;
        this.options = options;
    }

    /**
     * Gets the list of tools available for the call.
     *
     * @return The list of tools.
     */
    public List<ChatCompletionsToolDefinition> getTools() {
        return tools;
    }

    /**
     * Gets the tool selection strategy.
     *
     * @return The tool selection strategy.
     */
    public ChatCompletionsToolSelection getToolChoice() {
        return toolChoice;
    }

    /**
     * Indicates whether to automatically invoke the tool.
     *
     * @return True if auto-invocation is enabled; otherwise, false.
     */
    public boolean isAutoInvoke() {
        return autoInvoke;
    }

    /**
     * Gets additional options for function choice behavior.
     *
     * @return The function choice behavior options.
     */
    public FunctionChoiceBehaviorOptions getOptions() {
        return options;
    }
}
