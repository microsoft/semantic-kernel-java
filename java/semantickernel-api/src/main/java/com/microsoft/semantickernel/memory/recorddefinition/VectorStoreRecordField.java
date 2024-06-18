package com.microsoft.semantickernel.memory.recorddefinition;

/**
 * Represents a field in a record.
 */
public class VectorStoreRecordField {
    private final String name;
    public VectorStoreRecordField(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
}
