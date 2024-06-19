// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.memory.recorddefinition;

/**
 * Represents a field in a record.
 */
public class VectorStoreRecordField {
    private final String name;

    /**
     * Creates a new instance of the VectorStoreRecordField class.
     *
     * @param name the name of the field
     */
    public VectorStoreRecordField(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
