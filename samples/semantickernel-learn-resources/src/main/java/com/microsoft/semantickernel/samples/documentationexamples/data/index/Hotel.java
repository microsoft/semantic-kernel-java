// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.samples.documentationexamples.data.index;

import com.microsoft.semantickernel.data.vectorstorage.annotations.VectorStoreRecordData;
import com.microsoft.semantickernel.data.vectorstorage.annotations.VectorStoreRecordKey;
import com.microsoft.semantickernel.data.vectorstorage.annotations.VectorStoreRecordVector;
import com.microsoft.semantickernel.data.vectorstorage.definition.DistanceFunction;
import com.microsoft.semantickernel.data.vectorstorage.definition.IndexKind;

import java.util.Collections;
import java.util.List;

public class Hotel {
    @VectorStoreRecordKey
    private String hotelId;

    @VectorStoreRecordData(isFilterable = true)
    private String name;

    @VectorStoreRecordData(isFullTextSearchable = true)
    private String description;

    @VectorStoreRecordVector(dimensions = 4, indexKind = IndexKind.HNSW, distanceFunction = DistanceFunction.COSINE_DISTANCE)
    private List<Float> descriptionEmbedding;

    @VectorStoreRecordData(isFilterable = true)
    private List<String> tags;

    public Hotel() {
    }

    public Hotel(String hotelId, String name, String description, List<Float> descriptionEmbedding,
        List<String> tags) {
        this.hotelId = hotelId;
        this.name = name;
        this.description = description;
        this.descriptionEmbedding = Collections.unmodifiableList(descriptionEmbedding);
        this.tags = Collections.unmodifiableList(tags);
    }

    public String getHotelId() {
        return hotelId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<Float> getDescriptionEmbedding() {
        return descriptionEmbedding;
    }

    public List<String> getTags() {
        return tags;
    }
}
