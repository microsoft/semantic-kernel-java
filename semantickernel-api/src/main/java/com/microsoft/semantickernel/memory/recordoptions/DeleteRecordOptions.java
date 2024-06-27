// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.memory.recordoptions;

import com.microsoft.semantickernel.builders.SemanticKernelBuilder;

public class DeleteRecordOptions {
    private final String collectionName;

    private DeleteRecordOptions(String collectionName) {
        this.collectionName = collectionName;
    }

    /**
     * Creates a new builder.
     *
     * @return the builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Gets the collection name.
     *
     * @return the collection name
     */
    public String getCollectionName() {
        return collectionName;
    }

    public static class Builder implements SemanticKernelBuilder<DeleteRecordOptions> {
        private String collectionName;

        /**
         * Sets the collection name.
         * When a default collection name is not available, the collection name must be specified in the options.
         *
         * @param collectionName the collection name
         * @return DeleteRecordOptions.Builder
         */
        public Builder collectionName(String collectionName) {
            this.collectionName = collectionName;
            return this;
        }

        /**
         * Builds the options.
         *
         * @return DeleteRecordOptions
         */
        @Override
        public DeleteRecordOptions build() {
            return new DeleteRecordOptions(collectionName);
        }
    }
}
