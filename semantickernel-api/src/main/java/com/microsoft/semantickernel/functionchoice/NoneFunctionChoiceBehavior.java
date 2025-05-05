// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.functionchoice;

import com.microsoft.semantickernel.semanticfunctions.KernelFunction;

import javax.annotation.Nullable;
import java.util.List;

public class NoneFunctionChoiceBehavior extends FunctionChoiceBehavior {

    /**
     * Create a new instance of NoneFunctionChoiceBehavior.
     */
    public NoneFunctionChoiceBehavior(@Nullable List<KernelFunction<?>> functions,
                                      @Nullable FunctionChoiceBehaviorOptions options) {
        super(functions, options);
    }
}
