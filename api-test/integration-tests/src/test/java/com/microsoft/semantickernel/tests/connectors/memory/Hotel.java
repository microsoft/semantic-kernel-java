package com.microsoft.semantickernel.tests.connectors.memory;

import com.microsoft.semantickernel.memory.recordattributes.VectorStoreRecordDataAttribute;
import com.microsoft.semantickernel.memory.recordattributes.VectorStoreRecordKeyAttribute;
import com.microsoft.semantickernel.memory.recordattributes.VectorStoreRecordVectorAttribute;

import java.util.List;

public class Hotel {
    @VectorStoreRecordKeyAttribute
    private String id;
    @VectorStoreRecordDataAttribute
    private String name;
    @VectorStoreRecordDataAttribute
    private int code;
    @VectorStoreRecordDataAttribute(hasEmbedding = true, embeddingFieldName = "descriptionEmbedding")
    private String description;
    @VectorStoreRecordVectorAttribute
    private List<Float> descriptionEmbedding;
    @VectorStoreRecordDataAttribute
    private double rating;

    public Hotel() {
    }
    public Hotel(String id, String name, int code, String description, List<Float> descriptionEmbedding, double rating) {
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
}
