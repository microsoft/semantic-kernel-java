// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.recorddefinition;

/**
 * Represents the kind of index.
 */
public enum IndexKind {
    /**
     * A HNSW index.
     */
    HNSW("Hnsw"),
    /**
     * A flat index.
     */
    FLAT("Flat");

    private final String value;

    IndexKind(String value) {
        this.value = value;
    }

    /**
     * Gets the descriptive value of the IndexKind.
     *
     * @return the descriptive value of the IndexKind
     */
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
