package com.microsoft.semantickernel.samples.documentationexamples.vectorstores.index;

import com.microsoft.semantickernel.data.vectorstorage.attributes.VectorStoreRecordDataAttribute;
import com.microsoft.semantickernel.data.vectorstorage.attributes.VectorStoreRecordKeyAttribute;
import com.microsoft.semantickernel.data.vectorstorage.attributes.VectorStoreRecordVectorAttribute;
import com.microsoft.semantickernel.data.vectorstorage.definition.DistanceFunction;
import com.microsoft.semantickernel.data.vectorstorage.definition.IndexKind;

import java.util.List;

public class Hotel {
    @VectorStoreRecordKeyAttribute
    private String hotelId;

    @VectorStoreRecordDataAttribute(isFilterable = true)
    private String name;

    @VectorStoreRecordDataAttribute(isFullTextSearchable = true)
    private String description;

    @VectorStoreRecordVectorAttribute(dimensions = 4, indexKind = IndexKind.HNSW, distanceFunction = DistanceFunction.COSINE_DISTANCE)
    private List<Float> descriptionEmbedding;

    public Hotel() { }

    public Hotel(String hotelId, String name, String description, List<Float> descriptionEmbedding) {
        this.hotelId = hotelId;
        this.name = name;
        this.description = description;
        this.descriptionEmbedding = descriptionEmbedding;
    }

    public String getHotelId() { return hotelId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public List<Float> getDescriptionEmbedding() { return descriptionEmbedding; }
}
