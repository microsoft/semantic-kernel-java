// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.memory.recordoptions;

import com.microsoft.semantickernel.builders.SemanticKernelBuilder;

public class GetRecordOptions {
    private String collectionName;
    private boolean includeVectors;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder implements SemanticKernelBuilder<GetRecordOptions> {
        private String collectionName;
        private boolean includeVectors;

        /**
         * Sets the collection name.
         * When a default collection name is not available, the collection name must be specified in the options.
         *
         * @param collectionName the collection name
         * @return GetRecordOptions.Builder
         */
        public Builder collectionName(String collectionName) {
            this.collectionName = collectionName;
            return this;
        }

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
            GetRecordOptions options = new GetRecordOptions();
            options.collectionName = collectionName;
            options.includeVectors = includeVectors;
            return options;
        }
    }

    public String getCollectionName() {
        return collectionName;
    }

    public boolean includeVectors() {
        return includeVectors;
    }
}
