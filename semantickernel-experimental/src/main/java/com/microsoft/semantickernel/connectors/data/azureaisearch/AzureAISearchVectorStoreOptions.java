// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.data.azureaisearch;

public class AzureAISearchVectorStoreOptions<Record> {
    private final Class<Record> recordClass;
    private final AzureAISearchVectorStoreRecordCollectionFactory<Record> vectorStoreRecordCollectionFactory;

    /**
     * Creates a new instance of the Azure AI Search vector store options.
     *
     * @param recordClass The record class.
     * @param vectorStoreRecordCollectionFactory The vector store record collection factory.
     */
    public AzureAISearchVectorStoreOptions(Class<Record> recordClass,
        AzureAISearchVectorStoreRecordCollectionFactory<Record> vectorStoreRecordCollectionFactory) {
        this.recordClass = recordClass;
        this.vectorStoreRecordCollectionFactory = vectorStoreRecordCollectionFactory;
    }

    /**
     * Creates a new builder.
     *
     * @param <Record> the record type
     * @return the builder
     */
    public static <Record> Builder<Record> builder() {
        return new Builder<>();
    }

    /**
     * Gets the record class.
     *
     * @return the record class
     */
    public Class<Record> getRecordClass() {
        return recordClass;
    }

    /**
     * Gets the vector store record collection factory.
     *
     * @return the vector store record collection factory
     */
    public AzureAISearchVectorStoreRecordCollectionFactory<Record> getVectorStoreRecordCollectionFactory() {
        return vectorStoreRecordCollectionFactory;
    }

    /**
     * Builder for Azure AI Search vector store options.
     *
     * @param <Record> the record type
     */
    public static class Builder<Record> {
        private Class<Record> recordClass;
        private AzureAISearchVectorStoreRecordCollectionFactory<Record> vectorStoreRecordCollectionFactory;

        /**
         * Sets the record class.
         *
         * @param recordClass The record class.
         * @return The updated builder instance.
         */
        public Builder<Record> withRecordClass(Class<Record> recordClass) {
            this.recordClass = recordClass;
            return this;
        }

        /**
         * Sets the vector store record collection factory.
         *
         * @param vectorStoreRecordCollectionFactory The vector store record collection factory.
         * @return The updated builder instance.
         */
        public Builder<Record> withVectorStoreRecordCollectionFactory(
            AzureAISearchVectorStoreRecordCollectionFactory<Record> vectorStoreRecordCollectionFactory) {
            this.vectorStoreRecordCollectionFactory = vectorStoreRecordCollectionFactory;
            return this;
        }

        /**
         * Builds the Azure AI Search vector store options.
         *
         * @return The Azure AI Search vector store options.
         */
        public AzureAISearchVectorStoreOptions<Record> build() {
            return new AzureAISearchVectorStoreOptions<>(recordClass,
                vectorStoreRecordCollectionFactory);
        }
    }
}
