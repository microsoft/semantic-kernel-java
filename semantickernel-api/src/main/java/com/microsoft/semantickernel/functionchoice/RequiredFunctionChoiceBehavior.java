// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.functionchoice;

import com.microsoft.semantickernel.semanticfunctions.KernelFunction;

import javax.annotation.Nullable;
import java.util.List;

public class RequiredFunctionChoiceBehavior extends AutoFunctionChoiceBehavior {

    /**
     * Create a new instance of RequiredFunctionChoiceBehavior.
     *
     * @param autoInvoke Whether auto-invocation is enabled.
     * @param functions  A set of functions to advertise to the model.
     * @param options    Options for the function choice behavior.
     */
    public RequiredFunctionChoiceBehavior(boolean autoInvoke,
        @Nullable List<KernelFunction<?>> functions,
        @Nullable FunctionChoiceBehaviorOptions options) {
        super(autoInvoke, functions, options);
    }
}
