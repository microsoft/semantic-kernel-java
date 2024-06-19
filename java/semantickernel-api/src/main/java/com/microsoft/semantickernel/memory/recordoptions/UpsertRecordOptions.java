// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.memory.recordoptions;

import com.microsoft.semantickernel.builders.SemanticKernelBuilder;

public class UpsertRecordOptions {
    private String collectionName;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder implements SemanticKernelBuilder<UpsertRecordOptions> {
        private String collectionName;

        /**
         * Sets the collection name.
         * When a default collection name is not available, the collection name must be specified in the options.
         *
         * @param collectionName the collection name
         * @return UpsertRecordOptions.Builder
         */
        public Builder collectionName(String collectionName) {
            this.collectionName = collectionName;
            return this;
        }

        /**
         * Builds the options.
         *
         * @return UpsertRecordOptions
         */
        public UpsertRecordOptions build() {
            UpsertRecordOptions options = new UpsertRecordOptions();
            options.collectionName = collectionName;
            return options;
        }
    }

    public String getCollectionName() {
        return collectionName;
    }
}
