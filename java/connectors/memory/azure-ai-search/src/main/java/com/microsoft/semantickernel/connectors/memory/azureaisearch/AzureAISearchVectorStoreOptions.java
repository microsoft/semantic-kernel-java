package com.microsoft.semantickernel.connectors.memory.azureaisearch;

import com.azure.search.documents.SearchDocument;
import com.microsoft.semantickernel.memory.VectorStoreRecordMapper;
import com.microsoft.semantickernel.memory.recorddefinition.VectorStoreRecordDefinition;

public class AzureAISearchVectorStoreOptions<Record> {
    private String defaultCollectionName;
    private Class<Record> recordClass;
    private VectorStoreRecordMapper<Record, SearchDocument> vectorStoreRecordMapper;
    private VectorStoreRecordDefinition recordDefinition;

    public static <Record> Builder<Record> builder() {
        return new Builder<>();
    }

    public String getDefaultCollectionName() {
        return defaultCollectionName;
    }

    public Class<Record> getRecordClass() {
        return recordClass;
    }

    public VectorStoreRecordDefinition getRecordDefinition() {
        return recordDefinition;
    }

    public void setRecordDefinition(VectorStoreRecordDefinition recordDefinition) {
        this.recordDefinition = recordDefinition;
    }

    public VectorStoreRecordMapper<Record, SearchDocument> getVectorStoreRecordMapper() {
        return vectorStoreRecordMapper;
    }

    public static class Builder<Record> {
        private String defaultCollectionName;
        private VectorStoreRecordMapper<Record, SearchDocument> vectorStoreRecordMapper;
        private Class<Record> recordClass;
        private VectorStoreRecordDefinition recordDefinition;

        public Builder<Record> withRecordClass(Class<Record> recordClass) {
            this.recordClass = recordClass;
            return this;
        }

        public Builder<Record> withDefaultCollectionName(String defaultCollectionName) {
            this.defaultCollectionName = defaultCollectionName;
            return this;
        }

        public Builder<Record> withVectorStoreRecordMapper(VectorStoreRecordMapper<Record, SearchDocument> vectorStoreRecordMapper) {
            this.vectorStoreRecordMapper = vectorStoreRecordMapper;
            return this;
        }

        public Builder<Record> withRecordDefinition(VectorStoreRecordDefinition recordDefinition) {
            this.recordDefinition = recordDefinition;
            return this;
        }

        public AzureAISearchVectorStoreOptions<Record> build() {
            if (recordClass == null) {
                throw new IllegalArgumentException("recordClass must be provided");
            }

            AzureAISearchVectorStoreOptions<Record> options = new AzureAISearchVectorStoreOptions<>();
            options.defaultCollectionName = defaultCollectionName;
            options.recordClass = recordClass;
            options.vectorStoreRecordMapper = vectorStoreRecordMapper;
            options.recordDefinition = recordDefinition;
            return options;
        }
    }
}
