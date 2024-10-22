// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.vectorstorage.definition;

import com.microsoft.semantickernel.builders.SemanticKernelBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents a field in a record.
 */
public class VectorStoreRecordField {
    private final String name;
    @Nullable
    private final String storageName;
    private final Class<?> fieldType;

    /**
     * Creates a new instance of the VectorStoreRecordField class.
     *
     * @param name the name of the field
     * @param storageName the storage name of the field
     * @param fieldType the field type
     */
    public VectorStoreRecordField(
        @Nonnull String name,
        @Nullable String storageName,
        @Nonnull Class<?> fieldType) {
        this.name = name;
        this.storageName = storageName;
        this.fieldType = fieldType;
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

    /**
     * Gets the effective storage name of the field.
     * <p>
     * If the storage name is not set, the name of the field is returned.
     * @return the effective storage name of the field
     */
    public String getEffectiveStorageName() {
        return storageName != null ? storageName : name;
    }

    /**
     * Gets the field type.
     *
     * @return the field type
     */
    public Class<?> getFieldType() {
        return fieldType;
    }

    /**
     * A builder for the VectorStoreRecordField class.
     * @param <T> the type of the field
     * @param <U> the type of the builder
     */
    public abstract static class Builder<T, U extends Builder<T, U>>
        implements SemanticKernelBuilder<T> {

        @Nullable
        protected String name;
        @Nullable
        protected String storageName;
        @Nullable
        protected Class<?> fieldType;

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
         * Sets the field type.
         *
         * @param fieldType the field type
         * @return the builder
         */
        public U withFieldType(Class<?> fieldType) {
            this.fieldType = fieldType;
            return (U) this;
        }

        /**
         * Builds the field.
         *
         * @return the field
         */
        @Override
        public abstract T build();
    }
}
