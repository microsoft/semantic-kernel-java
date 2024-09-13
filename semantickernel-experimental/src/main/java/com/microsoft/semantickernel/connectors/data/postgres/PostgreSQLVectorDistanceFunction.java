// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.data.postgres;

import com.microsoft.semantickernel.data.vectorstorage.definition.DistanceFunction;

public enum PostgreSQLVectorDistanceFunction {
    L2("vector_l2_ops", "<->"), COSINE("vector_cosine_ops", "<=>"), INNER_PRODUCT("vector_ip_ops",
        "<#>");

    private final String value;
    private final String operator;

    PostgreSQLVectorDistanceFunction(String value, String operator) {
        this.value = value;
        this.operator = operator;
    }

    public String getValue() {
        return value;
    }

    public String getOperator() {
        return operator;
    }

    public static PostgreSQLVectorDistanceFunction fromDistanceFunction(DistanceFunction function) {
        if (function == null) {
            return null;
        }

        switch (function) {
            case EUCLIDEAN_DISTANCE:
                return L2;
            case COSINE_DISTANCE:
                return COSINE;
            case DOT_PRODUCT:
                return INNER_PRODUCT;
            default:
                throw new IllegalArgumentException("Unsupported distance function: " + function);
        }
    }
}
