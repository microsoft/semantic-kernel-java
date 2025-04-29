package com.microsoft.semantickernel.functionchoice;

import com.microsoft.semantickernel.semanticfunctions.KernelFunction;

import java.util.List;

public class NoneFunctionChoiceBehavior extends FunctionChoiceBehavior {

    /**
     * Create a new instance of NoneFunctionChoiceBehavior.
     */
    public NoneFunctionChoiceBehavior(List<KernelFunction<?>> functions, FunctionChoiceBehaviorOptions options) {
        super(functions, options);
    }
}
