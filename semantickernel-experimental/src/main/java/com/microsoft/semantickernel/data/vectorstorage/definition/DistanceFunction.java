// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.vectorstorage.definition;

/**
 * Distance functions for vector storage.
 */
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
    EUCLIDEAN_DISTANCE("euclidean"),
    /**
     * No distance function specified. It will default to the database's default distance function.
     */
    UNDEFINED(null);

    private final String value;

    DistanceFunction(String value) {
        this.value = value;
    }

    /**
     * Gets the function name.
     * @return The function name.
     */
    public String getValue() {
        return value;
    }
}
