package com.microsoft.semantickernel.tests.connectors.memory;

import com.microsoft.semantickernel.data.recordattributes.VectorStoreRecordDataAttribute;
import com.microsoft.semantickernel.data.recordattributes.VectorStoreRecordKeyAttribute;
import com.microsoft.semantickernel.data.recordattributes.VectorStoreRecordVectorAttribute;

import java.util.List;

public class Hotel {
    @VectorStoreRecordKeyAttribute
    private final String id;
    @VectorStoreRecordDataAttribute
    private final String name;
    @VectorStoreRecordDataAttribute
    private final int code;
    @VectorStoreRecordDataAttribute(hasEmbedding = true, embeddingFieldName = "descriptionEmbedding")
    private final String description;
    @VectorStoreRecordVectorAttribute(dimensions = 3)
    private final List<Float> descriptionEmbedding;
    @VectorStoreRecordVectorAttribute(dimensions = 3, indexKind = "hnsw", distanceFunction = "cosine")
    private final List<Float> additionalEmbedding;
    @VectorStoreRecordDataAttribute
    private double rating;

    public Hotel() {
        this(null, null, 0, null, null, 0.0);
    }

    public Hotel(String id, String name, int code, String description, List<Float> descriptionEmbedding, double rating) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.description = description;
        this.descriptionEmbedding = descriptionEmbedding;
        this.additionalEmbedding = descriptionEmbedding;
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

    public List<Float> getDescriptionEmbedding() {
        return descriptionEmbedding;
    }
    public List<Float> getAdditionalEmbedding() {
        return additionalEmbedding;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }
}
