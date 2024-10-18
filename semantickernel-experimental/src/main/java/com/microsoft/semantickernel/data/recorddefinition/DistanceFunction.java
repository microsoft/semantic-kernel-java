// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.recorddefinition;

/**
 * The distance function to use for similarity calculations.
 */
public enum DistanceFunction {
    /**
     * Cosine similarity.
     */
    COSINE_SIMILARITY("cosineSimilarity"),
    /**
     * Dot product.
     */
    DOT_PRODUCT("dotProduct"),
    /**
     * Euclidean distance.
     */
    EUCLIDEAN("euclidean");

    private final String value;

    DistanceFunction(String value) {
        this.value = value;
    }

    /**
     * Gets the descriptive value of the DistanceFunction.
     *
     * @return the descriptive value of the DistanceFunction
     */
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
            return COSINE_SIMILARITY;
        }

        for (DistanceFunction b : DistanceFunction.values()) {
            if (b.value.equalsIgnoreCase(text)) {
                return b;
            }
        }
        throw new IllegalArgumentException("No distance function with value " + text + " found");
    }
}
