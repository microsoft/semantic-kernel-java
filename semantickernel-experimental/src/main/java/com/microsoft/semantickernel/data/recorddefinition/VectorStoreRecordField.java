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

    /**
     * Gets the name of the field.
     *
     * @return the name of the field
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the storage name of the field.
     *
     * @return the storage name of the field
     */
    public String getStorageName() {
        return storageName;
    }

    public abstract static class Builder<T, U extends Builder<T, U>>
        implements SemanticKernelBuilder<T> {
        protected String name;
        protected String storageName;

        /**
         * Sets the name of the field.
         *
         * @param name the name of the field
         * @return the builder
         */
        public U withName(String name) {
            this.name = name;
            return (U) this;
        }

        /**
         * Sets the storage name of the field.
         *
         * @param storageName the storage name of the field
         * @return the builder
         */
        public U withStorageName(String storageName) {
            this.storageName = storageName;
            return (U) this;
        }

        /**
         * Builds the field.
         *
         * @return the field
         */
        public abstract T build();
    }
}
