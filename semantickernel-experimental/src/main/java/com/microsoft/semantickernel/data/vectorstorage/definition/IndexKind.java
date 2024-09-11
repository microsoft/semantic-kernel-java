// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.vectorstorage.definition;

public enum IndexKind {
    HNSW("Hnsw"), FLAT("Flat");

    private final String value;

    IndexKind(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Converts a string to an IndexKind.
     * If the string is null or empty, the method returns IndexKind.FLAT.
     *
     * @param text the string to convert
     * @return the IndexKind
     */
    public static IndexKind fromString(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        for (IndexKind b : IndexKind.values()) {
            if (b.value.equalsIgnoreCase(text)) {
                return b;
            }
        }
        throw new IllegalArgumentException("No index kind with value " + text + " found");
    }
}
