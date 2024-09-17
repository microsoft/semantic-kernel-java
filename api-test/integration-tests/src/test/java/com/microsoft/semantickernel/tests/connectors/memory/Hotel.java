package com.microsoft.semantickernel.tests.connectors.memory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.semantickernel.data.vectorstorage.attributes.VectorStoreRecordDataAttribute;
import com.microsoft.semantickernel.data.vectorstorage.attributes.VectorStoreRecordKeyAttribute;
import com.microsoft.semantickernel.data.vectorstorage.attributes.VectorStoreRecordVectorAttribute;

import java.util.List;

public class Hotel {
    @VectorStoreRecordKeyAttribute
    private final String id;

    @VectorStoreRecordDataAttribute
    private final String name;

    @VectorStoreRecordDataAttribute
    private final int code;

    @JsonProperty("summary")
    @VectorStoreRecordDataAttribute()
    private final String description;

    @JsonProperty("summaryEmbedding1")
    @VectorStoreRecordVectorAttribute(dimensions = 8, distanceFunction = "euclidean")
    private final List<Float> euclidean;

    @JsonProperty("summaryEmbedding2")
    @VectorStoreRecordVectorAttribute(dimensions = 8, distanceFunction = "cosineDistance")
    private final List<Float> cosineDistance;

    @JsonProperty("summaryEmbedding3")
    @VectorStoreRecordVectorAttribute(dimensions = 8, distanceFunction = "dotProduct")
    private final List<Float> dotProduct;

    @JsonProperty("indexedSummaryEmbedding")
    @VectorStoreRecordVectorAttribute(dimensions = 8, indexKind = "hnsw", distanceFunction = "euclidean")
    private final List<Float> indexedEuclidean;
    @VectorStoreRecordDataAttribute
    private double rating;

    public Hotel() {
        this(null, null, 0, null, null, null, null, null, 0.0);
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
            @JsonProperty("indexedSummaryEmbedding") List<Float> indexedEuclidean,
            @JsonProperty("rating") double rating) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.description = description;
        this.euclidean = euclidean;
        this.cosineDistance = euclidean;
        this.dotProduct = euclidean;
        this.indexedEuclidean = euclidean;
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

    public List<Float> getIndexedEuclidean() {
        return indexedEuclidean;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }
}
