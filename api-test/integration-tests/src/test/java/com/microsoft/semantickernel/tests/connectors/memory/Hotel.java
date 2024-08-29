package com.microsoft.semantickernel.tests.connectors.memory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.semantickernel.data.record.attributes.VectorStoreRecordDataAttribute;
import com.microsoft.semantickernel.data.record.attributes.VectorStoreRecordKeyAttribute;
import com.microsoft.semantickernel.data.record.attributes.VectorStoreRecordVectorAttribute;

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
    @JsonProperty("summaryEmbedding")
    @VectorStoreRecordVectorAttribute(dimensions = 3)
    private final List<Float> descriptionEmbedding;
    @VectorStoreRecordDataAttribute
    private double rating;

    public Hotel() {
        this(null, null, 0, null, null, 0.0);
    }

    @JsonCreator
    public Hotel(
            @JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("code") int code,
            @JsonProperty("summary") String description,
            @JsonProperty("summaryVector") List<Float> descriptionEmbedding,
            @JsonProperty("rating") double rating) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.description = description;
        this.descriptionEmbedding = descriptionEmbedding;
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

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }
}
