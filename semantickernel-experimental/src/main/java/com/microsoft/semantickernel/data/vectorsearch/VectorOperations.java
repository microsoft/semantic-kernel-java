// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.vectorsearch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.microsoft.semantickernel.data.vectorstorage.definition.DistanceFunction;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordVectorField;
import com.microsoft.semantickernel.data.vectorstorage.options.VectorSearchOptions;
import com.microsoft.semantickernel.exceptions.SKException;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Operations for working with vectors.
 */
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
            throw new SKException("Vectors lengths must be equal");
        }

        float dotProduct = dot(x, y);
        float normX = dot(x, x);
        float normY = dot(y, y);

        if (normX == 0 || normY == 0) {
            throw new SKException("Vectors cannot have zero norm");
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
            throw new SKException("Vectors lengths must be equal");
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
            throw new SKException("Divisor cannot be NaN");
        }
        if (divisor == 0f) {
            throw new SKException("Divisor cannot be zero");
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
            throw new SKException("Vectors lengths must be equal");
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
            throw new SKException("Multiplier cannot be NaN");
        }
        if (Float.isInfinite(multiplier)) {
            throw new SKException("Multiplier cannot be infinite");
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

    /**
     * Performs an exact similarity search on a list of records using a vector field.
     *
     * @param records The records to search.
     * @param vector The vector to search for.
     * @param vectorField The vector field to use for the search.
     * @param distanceFunction The distance function to use for the search.
     * @param options The search options.
     * @param <Record> The type of the records.
     * @return The search results.
     */
    public static <Record> List<VectorSearchResult<Record>> exactSimilaritySearch(
        List<Record> records,
        List<Float> vector,
        VectorStoreRecordVectorField vectorField,
        DistanceFunction distanceFunction,
        VectorSearchOptions options) {
        List<VectorSearchResult<Record>> results = new ArrayList<>();

        for (Record record : records) {
            List<Float> recordVector;
            try {
                String json = new ObjectMapper().writeValueAsString(record);
                ArrayNode arrayNode = (ArrayNode) new ObjectMapper().readTree(json)
                    .get(vectorField.getEffectiveStorageName());

                recordVector = Stream.iterate(0, i -> i + 1)
                    .limit(arrayNode.size())
                    .map(i -> arrayNode.get(i).floatValue())
                    .collect(Collectors.toList());
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            double score;
            switch (distanceFunction) {
                case COSINE_SIMILARITY:
                    score = cosineSimilarity(vector, recordVector);
                    break;
                case COSINE_DISTANCE:
                    score = cosineDistance(vector, recordVector);
                    break;
                case EUCLIDEAN_DISTANCE:
                    score = euclideanDistance(vector, recordVector);
                    break;
                case DOT_PRODUCT:
                    score = dot(vector, recordVector);
                    break;
                default:
                    throw new SKException("Unsupported distance function");
            }

            results.add(new VectorSearchResult<>(record, score));
        }

        Comparator<VectorSearchResult<Record>> comparator = Comparator
            .comparingDouble(VectorSearchResult::getScore);
        // Higher scores are better
        if (distanceFunction == DistanceFunction.COSINE_SIMILARITY
            || distanceFunction == DistanceFunction.DOT_PRODUCT) {
            comparator = comparator.reversed();
        }

        return results.stream()
            .sorted(comparator)
            .skip(options.getOffset())
            .limit(options.getLimit())
            .collect(Collectors.toList());
    }
}
