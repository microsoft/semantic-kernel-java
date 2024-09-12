// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.vectorstorage.options;

import com.microsoft.semantickernel.builders.SemanticKernelBuilder;

/**
 * Options for getting a record.
 */
public class GetRecordOptions {
    private final boolean includeVectors;

    private GetRecordOptions(boolean includeVectors) {
        this.includeVectors = includeVectors;
    }

    /**
     * Creates a new builder.
     *
     * @return the builder
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder implements SemanticKernelBuilder<GetRecordOptions> {
        private boolean includeVectors;

        /**
         * Sets whether to include vectors.
         *
         * @param includeVectors whether to include vectors
         * @return GetRecordOptions.Builder
         */
        public Builder includeVectors(boolean includeVectors) {
            this.includeVectors = includeVectors;
            return this;
        }

        /**
         * Builds the options.
         *
         * @return GetRecordOptions
         */
        @Override
        public GetRecordOptions build() {
            return new GetRecordOptions(includeVectors);
        }
    }

    /**
     * Gets whether to include vectors.
     *
     * @return whether to include vectors
     */
    public boolean includeVectors() {
        return includeVectors;
    }
}
