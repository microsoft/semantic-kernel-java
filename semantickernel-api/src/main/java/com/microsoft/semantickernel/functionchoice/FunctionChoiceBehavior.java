// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.functionchoice;

import com.microsoft.semantickernel.semanticfunctions.KernelFunction;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Defines the behavior of a tool call. Currently, the only tool available is function calling.
 */
public abstract class FunctionChoiceBehavior {
    private final Set<String> fullFunctionNames;

    protected final List<KernelFunction<?>> functions;
    protected final FunctionChoiceBehaviorOptions options;

    protected FunctionChoiceBehavior(List<KernelFunction<?>> functions,
                                     @Nullable FunctionChoiceBehaviorOptions options) {
        this.functions = functions;
        this.fullFunctionNames = new HashSet<>();

        if (functions != null) {
            functions.stream().filter(Objects::nonNull).forEach(
                    f -> this.fullFunctionNames
                            .add(formFullFunctionName(f.getPluginName(), f.getName())));
        }

        if (options != null) {
            this.options = options;
        } else {
            this.options = FunctionChoiceBehaviorOptions.builder().build();
        }
    }

    /**
     * Gets the functions that are allowed.
     *
     * @return The functions that are allowed.
     */
    public List<KernelFunction<?>> getFunctions() {
        return functions;
    }

    /**
     * Gets the options for the function choice behavior.
     *
     * @return The options for the function choice behavior.
     */
    public FunctionChoiceBehaviorOptions getOptions() {
        return options;
    }

    /**
     * Gets an instance of the FunctionChoiceBehavior that provides all the Kernel's plugins functions to the AI model to call.
     *
     * @param autoInvoke Indicates whether the functions should be automatically invoked by AI connectors
     *
     * @return A new ToolCallBehavior instance with all kernel functions allowed.
     */
    public static FunctionChoiceBehavior auto(boolean autoInvoke) {
        return new AutoFunctionChoiceBehavior(autoInvoke, null, null);
    }

    /**
     * Gets an instance of the FunctionChoiceBehavior that provides either all the Kernel's plugins functions to the AI model to call or specific functions.
     *
     * @param autoInvoke Enable or disable auto-invocation.
     *                   If auto-invocation is enabled, the model may request that the Semantic Kernel
     *                   invoke the kernel functions and return the value to the model.
     * @param functions Functions to provide to the model. If null, all the Kernel's plugins' functions are provided to the model.
     *                  If empty, no functions are provided to the model, which is equivalent to disabling function calling.
     * @param options   Options for the function choice behavior.
     *
     * @return A new FunctionChoiceBehavior instance with all kernel functions allowed.
     */
    public static FunctionChoiceBehavior auto(boolean autoInvoke,
                                              List<KernelFunction<?>> functions,
                                              @Nullable FunctionChoiceBehaviorOptions options) {
        return new AutoFunctionChoiceBehavior(autoInvoke, functions, options);
    }

    /**
     * Gets an instance of the FunctionChoiceBehavior that provides either all the Kernel's plugins functions to the AI model to call or specific functions.
     * <p>
     * This behavior forces the model to call the provided functions.
     * SK connectors will invoke a requested function or multiple requested functions if the model requests multiple ones in one request,
     * while handling the first request, and stop advertising the functions for the following requests to prevent the model from repeatedly calling the same function(s).
     *
     * @param functions Functions to provide to the model. If null, all the Kernel's plugins' functions are provided to the model.
     *                  If empty, no functions are provided to the model, which is equivalent to disabling function calling.
     * @return A new FunctionChoiceBehavior instance with the required function.
     */
    public static FunctionChoiceBehavior required(boolean autoInvoke,
                                                  List<KernelFunction<?>> functions,
                                                  @Nullable FunctionChoiceBehaviorOptions options) {
        return new RequiredFunctionChoiceBehavior(autoInvoke, functions, options);
    }

    /**
     * Gets an instance of the FunctionChoiceBehavior that provides either all the Kernel's plugins functions to the AI model to call or specific functions.
     * <p>
     * This behavior is useful if the user should first validate what functions the model will use.
     *
     * @param functions Functions to provide to the model. If null, all the Kernel's plugins' functions are provided to the model.
     *                  If empty, no functions are provided to the model, which is equivalent to disabling function calling.
     */
    public static FunctionChoiceBehavior none(List<KernelFunction<?>> functions,
                                              @Nullable FunctionChoiceBehaviorOptions options) {
        return new NoneFunctionChoiceBehavior(functions, options);
    }


    /**
     * The separator between the plugin name and the function name.
     */
    public static final String FUNCTION_NAME_SEPARATOR = "-";

    /**
     * Form the full function name.
     *
     * @param pluginName   The name of the plugin that the function is in.
     * @param functionName The name of the function.
     * @return The key for the function.
     */
    public static String formFullFunctionName(@Nullable String pluginName, String functionName) {
        if (pluginName == null) {
            pluginName = "";
        }
        return String.format("%s%s%s", pluginName, FUNCTION_NAME_SEPARATOR, functionName);
    }

    /**
     * Check whether the given function is allowed.
     *
     * @param function The function to check.
     * @return Whether the function is allowed.
     */
    public boolean isFunctionAllowed(KernelFunction<?> function) {
        return isFunctionAllowed(function.getPluginName(), function.getName());
    }

    /**
     * Check whether the given function is allowed.
     *
     * @param pluginName   The name of the plugin that the function is in.
     * @param functionName The name of the function.
     * @return Whether the function is allowed.
     */
    public boolean isFunctionAllowed(@Nullable String pluginName, String functionName) {
        // If no functions are provided, all functions are allowed.
        if (functions == null || functions.isEmpty()) {
            return true;
        }

        String key = formFullFunctionName(pluginName, functionName);
        return fullFunctionNames.contains(key);
    }
}
