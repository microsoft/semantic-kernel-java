// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.recorddefinition;

public enum IndexKind {
    HNSW("Hnsw"), FLAT("Flat");

    private final String value;

    IndexKind(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static IndexKind fromString(String text) {
        if (text == null || text.isEmpty()) {
            return FLAT;
        }

        for (IndexKind b : IndexKind.values()) {
            if (b.value.equalsIgnoreCase(text)) {
                return b;
            }
        }
        throw new IllegalArgumentException("No index kind with value " + text + " found");
    }
}
