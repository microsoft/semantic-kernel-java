/*
 ** Oracle Database Vector Store Connector for Semantic Kernel (Java)
 **
 ** Copyright (c) 2025 Oracle and/or its affiliates. All rights reserved.
 **
 ** The MIT License (MIT)
 **
 ** Permission is hereby granted, free of charge, to any person obtaining a copy
 ** of this software and associated documentation files (the "Software"), to
 ** deal in the Software without restriction, including without limitation the
 ** rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 ** sell copies of the Software, and to permit persons to whom the Software is
 ** furnished to do so, subject to the following conditions:
 **
 ** The above copyright notice and this permission notice shall be included in
 ** all copies or substantial portions of the Software.
 **
 ** THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 ** IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 ** FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 ** AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 ** LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 ** FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 ** IN THE SOFTWARE.
 */
package com.microsoft.semantickernel.data.jdbc.oracle;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.semantickernel.data.vectorstorage.annotations.VectorStoreRecordData;
import com.microsoft.semantickernel.data.vectorstorage.annotations.VectorStoreRecordKey;
import com.microsoft.semantickernel.data.vectorstorage.annotations.VectorStoreRecordVector;
import com.microsoft.semantickernel.data.vectorstorage.definition.DistanceFunction;
import com.microsoft.semantickernel.data.vectorstorage.definition.IndexKind;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonCreator.Mode.DELEGATING;
import static com.fasterxml.jackson.annotation.JsonCreator.Mode.PROPERTIES;

public class Hotel {
    @VectorStoreRecordKey
    private final String id;

    @VectorStoreRecordData(isFilterable = true)
    private final String name;

    @VectorStoreRecordData
    private final int code;

    @VectorStoreRecordData
    private final double price;

    @VectorStoreRecordData(isFilterable = true)
    private final List<String> tags;

    @JsonProperty("summary")
    @VectorStoreRecordData( isFilterable = true, isFullTextSearchable = true )
    private final String description;

    @JsonProperty("summaryEmbedding1")
    @VectorStoreRecordVector(dimensions = 8, distanceFunction = DistanceFunction.EUCLIDEAN_DISTANCE, indexKind = IndexKind.IVFFLAT)
    private final List<Float> euclidean;

    @JsonProperty("summaryEmbedding2")
    @VectorStoreRecordVector(dimensions = 8, distanceFunction = DistanceFunction.COSINE_DISTANCE, indexKind = IndexKind.HNSW)
    private final float[] cosineDistance;

    @JsonProperty("summaryEmbedding3")
    @VectorStoreRecordVector(dimensions = 8, distanceFunction = DistanceFunction.COSINE_SIMILARITY, indexKind = IndexKind.IVFFLAT)
    private final float[] cosineSimilarity;

    @JsonProperty("summaryEmbedding4")
    @VectorStoreRecordVector(dimensions = 8, distanceFunction = DistanceFunction.DOT_PRODUCT, indexKind = IndexKind.IVFFLAT)
    private final Float[] dotProduct;
    @VectorStoreRecordData
    private double rating;

    @JsonCreator(mode = DELEGATING)
    public Hotel() {
        this(null, null, 0, 0d, null, null, null, null, null, null, 0.0);
    }

    @JsonCreator(mode = PROPERTIES)
    protected Hotel(
        @JsonProperty("id") String id,
        @JsonProperty("name") String name,
        @JsonProperty("code") int code,
        @JsonProperty("price") double price,
        @JsonProperty("tags") List<String> tags,
        @JsonProperty("summary") String description,
        @JsonProperty("summaryEmbedding1") List<Float> euclidean,
        @JsonProperty("summaryEmbedding2") float[] cosineDistance,
        @JsonProperty("summaryEmbedding3") float[] cosineSimilarity,
        @JsonProperty("summaryEmbedding4") Float[] dotProduct,
        @JsonProperty("rating") double rating) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.price = price;
        this.tags = tags;
        this.description = description;
        this.euclidean = euclidean;
        this.cosineDistance = cosineDistance;
        this.cosineSimilarity = cosineSimilarity;
        this.dotProduct = dotProduct;
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

    public double getPrice() { return price; }

    public List<String> getTags() { return tags; }

    public String getDescription() {
        return description;
    }

    public List<Float> getEuclidean() {
        return euclidean;
    }

    public float[]  getCosineDistance() {
        return cosineDistance;
    }

    public Float[] getDotProduct() {
        return dotProduct;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }
}
