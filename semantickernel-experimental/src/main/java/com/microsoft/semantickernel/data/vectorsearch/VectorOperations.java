// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.vectorsearch;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class VectorOperations {

    /**
     * Calculates the cosine similarity of two vectors. The vectors must be equal in length and have
     * non-zero norm.
     *
     * @param x First vector, which is not modified
     * @param y Second vector, which is not modified
     * @return The cosine similarity of the two vectors
     */
    public static float cosineSimilarity(@Nonnull List<Float> x, @Nonnull List<Float> y) {
        Objects.requireNonNull(x);
        Objects.requireNonNull(y);

        if (x.size() != y.size()) {
            throw new IllegalArgumentException("Vectors lengths must be equal");
        }

        float dotProduct = dot(x, y);
        float normX = dot(x, x);
        float normY = dot(y, y);

        if (normX == 0 || normY == 0) {
            throw new IllegalArgumentException("Vectors cannot have zero norm");
        }

        return dotProduct / (float) (Math.sqrt(normX) * Math.sqrt(normY));
    }

    /**
     * Calculates the cosine distance of two vectors. The vectors must be equal in length and have
     * non-zero norm.
     *
     * @param x First vector, which is not modified
     * @param y Second vector, which is not modified
     * @return The cosine distance of the two vectors
     */
    public static double cosineDistance(List<Float> x, List<Float> y) {
        return 1.0 - cosineSimilarity(x, y);
    }

    /**
     * Calculates the Euclidean distance between two vectors.
     *
     * @param x First vector, which is not modified
     * @param y Second vector, which is not modified
     * @return The Euclidean distance between the two vectors
     */
    public static float euclideanDistance(@Nonnull List<Float> x, @Nonnull List<Float> y) {
        Objects.requireNonNull(x);
        Objects.requireNonNull(y);

        if (x.size() != y.size()) {
            throw new IllegalArgumentException("Vectors lengths must be equal");
        }

        float sumOfSquaredDifferences = 0.0f;

        for (int i = 0; i < x.size(); ++i) {
            float difference = x.get(i) - y.get(i);
            sumOfSquaredDifferences += difference * difference;
        }

        return (float) Math.sqrt(sumOfSquaredDifferences);
    }

    /**
     * Divides the elements of the vector by the divisor.
     *
     * @param vector Vector to divide, which is not modified
     * @param divisor Divisor to apply to each element of the vector
     * @return A new vector with the elements divided by the divisor
     */
    public static List<Float> divide(@Nonnull List<Float> vector, float divisor) {
        Objects.requireNonNull(vector);
        if (Float.isNaN(divisor)) {
            throw new IllegalArgumentException("Divisor cannot be NaN");
        }
        if (divisor == 0f) {
            throw new IllegalArgumentException("Divisor cannot be zero");
        }

        return vector.stream().map(x -> x / divisor).collect(Collectors.toList());
    }

    /**
     * Calculates the dot product of two vectors.
     *
     * @param x First vector, which is not modified
     * @param y Second vector, which is not modified
     * @return The dot product of the two vectors
     */
    public static float dot(@Nonnull List<Float> x, @Nonnull List<Float> y) {
        Objects.requireNonNull(x);
        Objects.requireNonNull(y);

        if (x.size() != y.size()) {
            throw new IllegalArgumentException("Vectors lengths must be equal");
        }

        float result = 0;
        for (int i = 0; i < x.size(); ++i) {
            result += x.get(i) * y.get(i);
        }

        return result;
    }

    /**
     * Calculates the Euclidean length of a vector.
     *
     * @param vector Vector to calculate the length of, which is not modified
     * @return The Euclidean length of the vector
     */
    public static float euclideanLength(@Nonnull List<Float> vector) {
        Objects.requireNonNull(vector);
        return (float) Math.sqrt(dot(vector, vector));
    }

    /**
     * Multiplies the elements of the vector by the multiplier.
     *
     * @param vector Vector to multiply, which is not modified
     * @param multiplier Multiplier to apply to each element of the vector
     * @return A new vector with the elements multiplied by the multiplier
     */
    public static List<Float> multiply(@Nonnull List<Float> vector, float multiplier) {
        Objects.requireNonNull(vector);
        if (Float.isNaN(multiplier)) {
            throw new IllegalArgumentException("Multiplier cannot be NaN");
        }
        if (Float.isInfinite(multiplier)) {
            throw new IllegalArgumentException("Multiplier cannot be infinite");
        }

        return vector.stream().map(x -> x * multiplier).collect(Collectors.toList());
    }

    /**
     * Normalizes the vector such that the Euclidean length is 1.
     *
     * @param vector Vector to normalize, which is not modified
     * @return A new, normalized vector
     */
    public static List<Float> normalize(@Nonnull List<Float> vector) {
        Objects.requireNonNull(vector);
        return divide(vector, euclideanLength(vector));
    }
}
