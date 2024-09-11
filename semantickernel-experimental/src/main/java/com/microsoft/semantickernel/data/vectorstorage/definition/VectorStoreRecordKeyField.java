// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.vectorstorage.definition;

/**
 * Represents the key field in a record.
 */
public class VectorStoreRecordKeyField extends VectorStoreRecordField {
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a new instance of the VectorStoreRecordKeyField class.
     *
     * @param name the name of the field
     * @param storageName the storage name of the field
     * @param type the field type
     */
    public VectorStoreRecordKeyField(String name, String storageName, Class<?> type) {
        super(name, storageName, type);
    }

    public static class Builder
        extends VectorStoreRecordField.Builder<VectorStoreRecordKeyField, Builder> {
        @Override
        public VectorStoreRecordKeyField build() {
            if (name == null) {
                throw new IllegalArgumentException("name is required.");
            }
            if (fieldType == null) {
                throw new IllegalArgumentException("fieldType is required.");
            }
            return new VectorStoreRecordKeyField(name, storageName, fieldType);
        }
    }
}
