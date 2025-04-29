package com.microsoft.semantickernel.functionchoice;

import com.microsoft.semantickernel.semanticfunctions.KernelFunction;

import javax.annotation.Nullable;
import java.util.List;

/**
 * A set of allowed kernel functions. All kernel functions are allowed if allKernelFunctionsAllowed is true.
 * Otherwise, only the functions in allowedFunctions are allowed.
 * <p>
 * If a function is allowed, it may be called. If it is not allowed, it will not be called.
 */
public class AutoFunctionChoiceBehavior extends FunctionChoiceBehavior {
    private final boolean autoInvoke;

    /**
     * Create a new instance of AutoFunctionChoiceBehavior.
     *
     * @param autoInvoke Whether auto-invocation is enabled.
     * @param functions  A set of functions to advertise to the model.
     * @param options    Options for the function choice behavior.
     */
    public AutoFunctionChoiceBehavior(boolean autoInvoke,
                                      @Nullable List<KernelFunction<?>> functions,
                                      @Nullable FunctionChoiceBehaviorOptions options) {
        super(functions, options);
        this.autoInvoke = autoInvoke;
    }

    /**
     * Check whether the given function is allowed.
     *
     * @return Whether the function is allowed.
     */
    public boolean isAutoInvoke() {
        return autoInvoke;
    }
}