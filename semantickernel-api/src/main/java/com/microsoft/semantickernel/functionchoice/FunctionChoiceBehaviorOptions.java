// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.functionchoice;

import com.microsoft.semantickernel.builders.SemanticKernelBuilder;

public class FunctionChoiceBehaviorOptions {
    private final boolean parallelCallsAllowed;

    private FunctionChoiceBehaviorOptions(boolean parallelCallsAllowed) {
        this.parallelCallsAllowed = parallelCallsAllowed;
    }

    /**
     * Returns a new builder for {@link FunctionChoiceBehaviorOptions}.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Indicates whether parallel calls to functions are allowed.
     *
     * @return True if parallel calls are allowed; otherwise, false.
     */
    public boolean isParallelCallsAllowed() {
        return parallelCallsAllowed;
    }

    /**
     * Builder for {@link FunctionChoiceBehaviorOptions}.
     */
    public static class Builder implements SemanticKernelBuilder<FunctionChoiceBehaviorOptions> {
        private boolean allowParallelCalls = false;

        /**
         * Sets whether parallel calls to functions are allowed.
         *
         * @param allowParallelCalls True if parallel calls are allowed; otherwise, false.
         * @return The builder instance.
         */
        public Builder withParallelCallsAllowed(boolean allowParallelCalls) {
            this.allowParallelCalls = allowParallelCalls;
            return this;
        }

        public FunctionChoiceBehaviorOptions build() {
            return new FunctionChoiceBehaviorOptions(allowParallelCalls);
        }
    }
}
