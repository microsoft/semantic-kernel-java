// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.data.postgres;

import com.microsoft.semantickernel.data.vectorstorage.definition.IndexKind;

public enum PostgreSQLVectorIndexKind {
    HNSW("hnsw"), IVFFLAT("ivfflat"), UNDEFINED(null);

    private final String value;

    PostgreSQLVectorIndexKind(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

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
