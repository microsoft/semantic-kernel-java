// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.vectorstorage.definition;

public enum DistanceFunction {
    /**
     * Cosine (angular) similarity function.
     */
    COSINE_SIMILARITY("cosineSimilarity"),
    /**
     * Cosine distance function. 1 - cosine similarity.
     */
    COSINE_DISTANCE("cosineDistance"),
    /**
     * Dot product between two vectors.
     */
    DOT_PRODUCT("dotProduct"),
    /**
     * Euclidean distance function. Also known as L2 distance.
     */
    EUCLIDEAN_DISTANCE("euclidean");

    private final String value;

    DistanceFunction(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Converts a string to a DistanceFunction.
     * If the string is null or empty, the method returns DistanceFunction.COSINE_SIMILARITY.
     *
     * @param text the string to convert
     * @return the DistanceFunction
     */
    public static DistanceFunction fromString(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        for (DistanceFunction b : DistanceFunction.values()) {
            if (b.value.equalsIgnoreCase(text)) {
                return b;
            }
        }
        throw new IllegalArgumentException("No distance function with value " + text + " found");
    }
}
