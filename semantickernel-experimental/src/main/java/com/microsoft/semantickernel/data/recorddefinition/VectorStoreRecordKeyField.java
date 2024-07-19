// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.recorddefinition;

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
     */
    public VectorStoreRecordKeyField(String name, String storageName) {
        super(name, storageName);
    }

    public static class Builder
        extends VectorStoreRecordField.Builder<VectorStoreRecordKeyField, Builder> {
        @Override
        public VectorStoreRecordKeyField build() {
            return new VectorStoreRecordKeyField(name, storageName);
        }
    }
}
