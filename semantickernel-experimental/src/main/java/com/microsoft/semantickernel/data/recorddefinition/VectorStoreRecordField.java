// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.recorddefinition;

import com.microsoft.semantickernel.builders.SemanticKernelBuilder;

/**
 * Represents a field in a record.
 */
public class VectorStoreRecordField {
    private final String name;
    private final String storageName;

    /**
     * Creates a new instance of the VectorStoreRecordField class.
     *
     * @param name the name of the field
     */
    public VectorStoreRecordField(String name,
        String storageName) {
        this.name = name;
        this.storageName = storageName;
    }

    public String getName() {
        return name;
    }

    public String getStorageName() {
        return storageName;
    }

    public abstract static class Builder<T, U extends Builder<T, U>>
        implements SemanticKernelBuilder<T> {
        protected String name;
        protected String storageName;

        public U withName(String name) {
            this.name = name;
            return (U) this;
        }

        public U withStorageName(String storageName) {
            this.storageName = storageName;
            return (U) this;
        }

        public abstract T build();
    }
}
