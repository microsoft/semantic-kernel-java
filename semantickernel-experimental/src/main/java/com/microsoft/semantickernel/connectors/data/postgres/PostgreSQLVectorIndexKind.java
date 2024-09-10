// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.data.postgres;

import com.microsoft.semantickernel.data.recorddefinition.IndexKind;

public enum PostgreSQLVectorIndexKind {
    HNSW("hnsw"), IVFFLAT("ivfflat");

    private final String value;

    PostgreSQLVectorIndexKind(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static PostgreSQLVectorIndexKind fromIndexKind(IndexKind indexKind) {
        if (indexKind == null) {
            return null;
        }

        switch (indexKind) {
            case HNSW:
                return HNSW;
            case FLAT:
                return IVFFLAT;
            default:
                throw new IllegalArgumentException("Unsupported index kind: " + indexKind);
        }
    }
}
