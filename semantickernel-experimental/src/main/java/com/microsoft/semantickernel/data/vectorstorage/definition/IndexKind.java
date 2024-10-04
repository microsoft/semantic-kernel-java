// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.vectorstorage.definition;

public enum IndexKind {
    /**
     * Hierarchical Navigable Small World, which performs an approximate nearest neighbour (ANN) search.
     */
    HNSW("Hnsw"),

    /**
     * Flat index, which performs an exact nearest neighbour search.
     * Also referred to as exhaustive k nearest neighbor in some databases.
     * High recall accuracy, but slower and more expensive than HNSW.
     * Better with smaller datasets.
     */
    FLAT("Flat"),

    /**
     * Inverted file index, which performs an approximate nearest neighbour (ANN) search.
     */
    IVFFLAT("IVFFlat"),

    /**
     * No index specified. It will default to the database's default index.
     */
    UNDEFINED(null);

    private final String value;

    IndexKind(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
