// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.data.azureaisearch;

import com.azure.search.documents.SearchDocument;
import com.microsoft.semantickernel.data.VectorStoreRecordMapper;
import com.microsoft.semantickernel.data.recorddefinition.VectorStoreRecordDefinition;

/**
 * Options for an Azure AI Search vector store.
 *
 * @param <Record> the record type
 */
public class AzureAISearchVectorStoreRecordCollectionOptions<Record> {
    private final Class<Record> recordClass;
    private final VectorStoreRecordMapper<Record, SearchDocument> vectorStoreRecordMapper;
    private final VectorStoreRecordDefinition recordDefinition;

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
     * Gets the record definition.
     *
     * @return the record definition
     */
    public VectorStoreRecordDefinition getRecordDefinition() {
        return recordDefinition;
    }

    /**
     * Gets the vector store record mapper.
     *
     * @return the vector store record mapper
     */
    public VectorStoreRecordMapper<Record, SearchDocument> getVectorStoreRecordMapper() {
        return vectorStoreRecordMapper;
    }

    private AzureAISearchVectorStoreRecordCollectionOptions(
        Class<Record> recordClass,
        VectorStoreRecordMapper<Record, SearchDocument> vectorStoreRecordMapper,
        VectorStoreRecordDefinition recordDefinition) {
        this.recordClass = recordClass;
        this.vectorStoreRecordMapper = vectorStoreRecordMapper;
        this.recordDefinition = recordDefinition;
    }

    /**
     * Builder for {@link AzureAISearchVectorStoreRecordCollectionOptions}.
     *
     * @param <Record> the record type
     */
    public static class Builder<Record> {
        private VectorStoreRecordMapper<Record, SearchDocument> vectorStoreRecordMapper;
        private Class<Record> recordClass;
        private VectorStoreRecordDefinition recordDefinition;

        public Builder<Record> withRecordClass(Class<Record> recordClass) {
            this.recordClass = recordClass;
            return this;
        }

        /**
         * Sets the vector store record mapper.
         *
         * @param vectorStoreRecordMapper the vector store record mapper
         * @return the builder
         */
        public Builder<Record> withVectorStoreRecordMapper(
            VectorStoreRecordMapper<Record, SearchDocument> vectorStoreRecordMapper) {
            this.vectorStoreRecordMapper = vectorStoreRecordMapper;
            return this;
        }

        /**
         * Sets the record definition.
         *
         * @param recordDefinition the record definition
         * @return the builder
         */
        public Builder<Record> withRecordDefinition(VectorStoreRecordDefinition recordDefinition) {
            this.recordDefinition = recordDefinition;
            return this;
        }

        /**
         * Builds the options.
         *
         * @return the options
         */
        public AzureAISearchVectorStoreRecordCollectionOptions<Record> build() {
            if (recordClass == null) {
                throw new IllegalArgumentException("recordClass must be provided");
            }

            return new AzureAISearchVectorStoreRecordCollectionOptions<>(
                recordClass,
                vectorStoreRecordMapper,
                recordDefinition);
        }
    }
}
