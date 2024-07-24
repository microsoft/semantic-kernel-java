// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data;

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
    @VectorStoreRecordDataAttribute
    private final double rating;

    public Hotel() {
        this(null, null, 0, null, null, 0.0);
    }

    public Hotel(String id, String name, int code, String description,
        List<Float> descriptionEmbedding, double rating) {
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
