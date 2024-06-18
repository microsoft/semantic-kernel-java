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

        public Builder collectionName(String collectionName) {
            this.collectionName = collectionName;
            return this;
        }

        public Builder includeVectors(boolean includeVectors) {
            this.includeVectors = includeVectors;
            return this;
        }

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
