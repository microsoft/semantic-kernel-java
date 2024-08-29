// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.record.definition;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class VectorStoreRecordDataField extends VectorStoreRecordField {
    private final boolean isFilterable;
    private final boolean isFullTextSearchable;

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a new instance of the VectorStoreRecordDataField class.
     *
     * @param name the name of the field
     * @param storageName the storage name of the field
     * @param fieldType the field type
     * @param isFilterable a value indicating whether the field is filterable
     */
    public VectorStoreRecordDataField(
        @Nonnull String name,
        @Nullable String storageName,
        @Nonnull Class<?> fieldType,
        boolean isFilterable,
        boolean isFullTextSearchable) {
        super(name, storageName, fieldType);
        this.isFilterable = isFilterable;
        this.isFullTextSearchable = isFullTextSearchable;
    }

    /**
     * Gets a value indicating whether the field is filterable.
     *
     * @return a value indicating whether the field is filterable
     */
    public boolean isFilterable() {
        return isFilterable;
    }

    /**
     * Gets a value indicating whether the field is full text searchable.
     *
     * @return a value indicating whether the field is full text searchable
     */
    public boolean isFullTextSearchable() {
        return isFullTextSearchable;
    }

    public static class Builder
        extends VectorStoreRecordField.Builder<VectorStoreRecordDataField, Builder> {
        private boolean isFilterable;
        private boolean isFullTextSearchable;

        /**
         * Sets a value indicating whether the field is filterable.
         *
         * @param isFilterable a value indicating whether the field is filterable
         * @return the builder
         */
        public Builder isFilterable(boolean isFilterable) {
            this.isFilterable = isFilterable;
            return this;
        }

        /**
         * Sets a value indicating whether the field is full text searchable.
         *
         * @param isFullTextSearchable a value indicating whether the field is full text searchable
         * @return the builder
         */
        public Builder isFullTextSearchable(boolean isFullTextSearchable) {
            this.isFullTextSearchable = isFullTextSearchable;
            return this;
        }

        /**
         * Builds a new instance of the VectorStoreRecordDataField class.
         *
         * @return a new instance of the VectorStoreRecordDataField class
         */
        @Override
        public VectorStoreRecordDataField build() {
            if (name == null) {
                throw new IllegalArgumentException("name is required");
            }
            if (fieldType == null) {
                throw new IllegalArgumentException("fieldType is required");
            }

            return new VectorStoreRecordDataField(
                name,
                storageName,
                fieldType,
                isFilterable,
                isFullTextSearchable);
        }
    }

}
