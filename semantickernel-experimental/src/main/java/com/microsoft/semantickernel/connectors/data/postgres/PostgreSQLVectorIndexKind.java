// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.data.postgres;

import com.microsoft.semantickernel.data.vectorstorage.definition.IndexKind;

/**
 * Represents a PostgreSQL vector index kind.
 */
public enum PostgreSQLVectorIndexKind {
    /**
     * The vector is indexed using an HNSW algorithm.
     */
    HNSW("hnsw"),
    /**
     * The vector is indexed using a Flat algorithm.
     */
    IVFFLAT("ivfflat"),
    /**
     * The indexing algorithm is undefined.
     */
    UNDEFINED(null);

    private final String value;

    PostgreSQLVectorIndexKind(String value) {
        this.value = value;
    }

    /**
     * Gets the pgvector value of the index kind.
     * @return the pgvector value of the index kind
     */
    public String getValue() {
        return value;
    }

    /**
     * Converts an index kind to a PostgreSQL vector index kind.
     * @param indexKind the index kind
     * @return the PostgreSQL vector index kind
     */
    public static PostgreSQLVectorIndexKind fromIndexKind(IndexKind indexKind) {
        switch (indexKind) {
            case HNSW:
                return HNSW;
            case IVFFLAT:
                return IVFFLAT;
            case FLAT:
            case UNDEFINED:
                return UNDEFINED;
            default:
                throw new IllegalArgumentException("Unsupported index kind: " + indexKind);
        }
    }
}
