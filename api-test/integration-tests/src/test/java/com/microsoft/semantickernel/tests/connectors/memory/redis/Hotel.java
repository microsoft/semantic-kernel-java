package com.microsoft.semantickernel.tests.connectors.memory.redis;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.semantickernel.data.vectorstorage.attributes.VectorStoreRecordData;
import com.microsoft.semantickernel.data.vectorstorage.attributes.VectorStoreRecordKey;
import com.microsoft.semantickernel.data.vectorstorage.attributes.VectorStoreRecordVector;
import com.microsoft.semantickernel.data.vectorstorage.definition.DistanceFunction;
import com.microsoft.semantickernel.data.vectorstorage.definition.IndexKind;

import java.util.List;

public class Hotel {

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
    @VectorStoreRecordVector(dimensions = 8, indexKind = IndexKind.HNSW, distanceFunction = DistanceFunction.EUCLIDEAN_DISTANCE)
    private final List<Float> euclidean;

    @JsonProperty("summaryEmbedding2")
    @VectorStoreRecordVector(dimensions = 8)
    private final List<Float> cosineDistance;

    @JsonProperty("summaryEmbedding3")
    @VectorStoreRecordVector(dimensions = 8, indexKind = IndexKind.HNSW, distanceFunction = DistanceFunction.DOT_PRODUCT)
    private final List<Float> dotProduct;

    @VectorStoreRecordData(isFilterable = true)
    private double rating;

    public Hotel() {
        this(null, null, 0, null, null, null, null, 0.0);
    }

    @JsonCreator
    public Hotel(
        @JsonProperty("id") String id,
        @JsonProperty("name") String name,
        @JsonProperty("code") int code,
        @JsonProperty("summary") String description,
        @JsonProperty("summaryEmbedding1") List<Float> euclidean,
        @JsonProperty("summaryEmbedding2") List<Float> cosineDistance,
        @JsonProperty("summaryEmbedding3") List<Float> dotProduct,
        @JsonProperty("rating") double rating) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.description = description;
        this.euclidean = euclidean;
        this.cosineDistance = euclidean;
        this.dotProduct = euclidean;
        this.rating = rating;
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

    public List<Float> getCosineDistance() {
        return cosineDistance;
    }

    public List<Float> getDotProduct() {
        return dotProduct;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }
}
