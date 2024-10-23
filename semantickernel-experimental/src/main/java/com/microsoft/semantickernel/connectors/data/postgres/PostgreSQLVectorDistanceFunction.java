// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.data.postgres;

import com.microsoft.semantickernel.data.vectorstorage.definition.DistanceFunction;

/**
 * Represents a PostgreSQL vector distance function.
 */
public enum PostgreSQLVectorDistanceFunction {
    /**
     * Euclidean L2 distance function.
     */
    L2("vector_l2_ops", "<->"),
    /**
     * The cosine distance function.
     */
    COSINE("vector_cosine_ops", "<=>"),
    /**
     * The inner product distance function.
     */
    INNER_PRODUCT("vector_ip_ops", "<#>"),
    /**
     * The distance function is undefined.
     */
    UNDEFINED(null, null);

    private final String value;
    private final String operator;

    PostgreSQLVectorDistanceFunction(String value, String operator) {
        this.value = value;
        this.operator = operator;
    }

    /**
     * Gets the value of the distance function.
     * @return the value of the distance function
     */
    public String getValue() {
        return value;
    }

    /**
     * Gets the operator of the distance function.
     * @return the operator of the distance function
     */
    public String getOperator() {
        return operator;
    }

    /**
     * Converts a distance function to a PostgreSQL vector distance function.
     * @param function the distance function
     * @return the PostgreSQL vector distance function
     */
    public static PostgreSQLVectorDistanceFunction fromDistanceFunction(DistanceFunction function) {
        switch (function) {
            case EUCLIDEAN_DISTANCE:
                return L2;
            case COSINE_DISTANCE:
                return COSINE;
            case DOT_PRODUCT:
                return INNER_PRODUCT;
            case UNDEFINED:
                return UNDEFINED;
            default:
                throw new IllegalArgumentException("Unsupported distance function: " + function);
        }
    }
}
