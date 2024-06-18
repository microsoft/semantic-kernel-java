package com.microsoft.semantickernel.memory.recordoptions;

import com.microsoft.semantickernel.builders.SemanticKernelBuilder;

public class UpsertRecordOptions {
    private String collectionName;
    public static Builder builder() {
        return new Builder();
    }
    public static class Builder implements SemanticKernelBuilder<UpsertRecordOptions> {
        private String collectionName;

        public Builder collectionName(String collectionName) {
            this.collectionName = collectionName;
            return this;
        }

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
