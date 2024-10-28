package com.microsoft.semantickernel.tests.data.jdbc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.semantickernel.data.vectorstorage.annotations.VectorStoreRecordData;
import com.microsoft.semantickernel.data.vectorstorage.annotations.VectorStoreRecordKey;
import com.microsoft.semantickernel.data.vectorstorage.annotations.VectorStoreRecordVector;
import com.microsoft.semantickernel.data.vectorstorage.definition.DistanceFunction;
import com.microsoft.semantickernel.data.vectorstorage.definition.IndexKind;

import java.util.List;

public class Hotel {
    @JsonProperty("hotelId")
    @VectorStoreRecordKey
    private final String id;

    @VectorStoreRecordData(isFilterable = true)
    private final String name;

    @VectorStoreRecordData
    private final int code;

    @JsonProperty("summary")
    @VectorStoreRecordData()
    private final String description;

    @JsonProperty("summaryEmbedding1")
    @VectorStoreRecordVector(dimensions = 8, distanceFunction = DistanceFunction.EUCLIDEAN_DISTANCE)
    private final List<Float> euclidean;

    @JsonProperty("summaryEmbedding2")
    @VectorStoreRecordVector(dimensions = 8, distanceFunction = DistanceFunction.COSINE_DISTANCE)
    private final List<Float> cosineDistance;

    @JsonProperty("summaryEmbedding3")
    @VectorStoreRecordVector(dimensions = 8, distanceFunction = DistanceFunction.DOT_PRODUCT)
    private final List<Float> dotProduct;

    @JsonProperty("indexedSummaryEmbedding")
    @VectorStoreRecordVector(dimensions = 8, indexKind = IndexKind.HNSW, distanceFunction = DistanceFunction.EUCLIDEAN_DISTANCE)
    private final List<Float> indexedEuclidean;

    @VectorStoreRecordData
    private final List<String> tags;

    @VectorStoreRecordData
    private double rating;

    public Hotel() {
        this(null, null, 0, null, null, null, null, null, 0.0, null);
    }

    @JsonCreator
    public Hotel(
            @JsonProperty("hotelId") String id,
            @JsonProperty("name") String name,
            @JsonProperty("code") int code,
            @JsonProperty("summary") String description,
            @JsonProperty("summaryEmbedding1") List<Float> euclidean,
            @JsonProperty("summaryEmbedding2") List<Float> cosineDistance,
            @JsonProperty("summaryEmbedding3") List<Float> dotProduct,
            @JsonProperty("indexedSummaryEmbedding") List<Float> indexedEuclidean,
            @JsonProperty("rating") double rating,
            @JsonProperty("tags") List<String> tags) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.description = description;
        this.euclidean = euclidean;
        this.cosineDistance = euclidean;
        this.dotProduct = euclidean;
        this.indexedEuclidean = euclidean;
        this.rating = rating;
        this.tags = tags;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public List<Float> getEuclidean() {
        return euclidean;
    }

    public List<Float> getIndexedEuclidean() {
        return indexedEuclidean;
    }

    public double getRating() {
        return rating;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }
}
