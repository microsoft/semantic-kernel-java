package com.microsoft.semantickernel.memory.recordoptions;

import com.microsoft.semantickernel.builders.SemanticKernelBuilder;

public class DeleteRecordOptions {
    private String collectionName;
    public String getCollectionName() {
        return collectionName;
    }
    public static class Builder implements SemanticKernelBuilder<DeleteRecordOptions> {
        private String collectionName;

        public Builder collectionName(String collectionName) {
            this.collectionName = collectionName;
            return this;
        }

        public DeleteRecordOptions build() {
            DeleteRecordOptions options = new DeleteRecordOptions();
            options.collectionName = collectionName;
            return options;
        }
    }
}
