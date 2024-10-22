// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.data.azureaisearch;

import javax.annotation.Nullable;

/**
 * Represents the options for the Azure AI Search vector store.
 */
public class AzureAISearchVectorStoreOptions {

    @Nullable
    private final AzureAISearchVectorStoreRecordCollectionFactory vectorStoreRecordCollectionFactory;

    /**
     * Creates a new instance of the Azure AI Search vector store options.
     *
     * @param vectorStoreRecordCollectionFactory The vector store record collection factory.
     */
    public AzureAISearchVectorStoreOptions(
        @Nullable AzureAISearchVectorStoreRecordCollectionFactory vectorStoreRecordCollectionFactory) {
        this.vectorStoreRecordCollectionFactory = vectorStoreRecordCollectionFactory;
    }

    /**
     * Creates a new instance of the Azure AI Search vector store options.
     */
    public AzureAISearchVectorStoreOptions() {
        this(null);
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
     * Gets the vector store record collection factory.
     *
     * @return the vector store record collection factory
     */
    @Nullable
    public AzureAISearchVectorStoreRecordCollectionFactory getVectorStoreRecordCollectionFactory() {
        return vectorStoreRecordCollectionFactory;
    }

    /**
     * Builder for Azure AI Search vector store options.
     *
     */
    public static class Builder {

        @Nullable
        private AzureAISearchVectorStoreRecordCollectionFactory vectorStoreRecordCollectionFactory;

        /**
         * Sets the vector store record collection factory.
         *
         * @param vectorStoreRecordCollectionFactory The vector store record collection factory.
         * @return The updated builder instance.
         */
        public Builder withVectorStoreRecordCollectionFactory(
            AzureAISearchVectorStoreRecordCollectionFactory vectorStoreRecordCollectionFactory) {
            this.vectorStoreRecordCollectionFactory = vectorStoreRecordCollectionFactory;
            return this;
        }

        /**
         * Builds the Azure AI Search vector store options.
         *
         * @return The Azure AI Search vector store options.
         */
        public AzureAISearchVectorStoreOptions build() {
            return new AzureAISearchVectorStoreOptions(vectorStoreRecordCollectionFactory);
        }
    }
}
