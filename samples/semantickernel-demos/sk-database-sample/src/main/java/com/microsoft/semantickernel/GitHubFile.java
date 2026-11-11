// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel;

import com.microsoft.semantickernel.data.vectorstorage.annotations.VectorStoreRecordData;
import com.microsoft.semantickernel.data.vectorstorage.annotations.VectorStoreRecordKey;
import com.microsoft.semantickernel.data.vectorstorage.annotations.VectorStoreRecordVector;
import com.microsoft.semantickernel.data.vectorstorage.definition.DistanceFunction;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

public class GitHubFile {

    @VectorStoreRecordKey
    private final String id;
    @VectorStoreRecordData
    private final String description;
    @VectorStoreRecordData
    private final String link;
    @VectorStoreRecordVector(dimensions = 1536, distanceFunction = DistanceFunction.COSINE_DISTANCE)
    private final List<Float> embedding;

    public GitHubFile() {
        this(null, null, null, Collections.emptyList());
    }

    public GitHubFile(
        String id,
        String description,
        String link,
        List<Float> embedding) {
        this.id = id;
        this.description = description;
        this.link = link;
        this.embedding = embedding;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getLink() {
        return link;
    }

    public List<Float> getEmbedding() {
        return embedding;
    }

    public static String encodeId(String realId) {
        byte[] bytes = Base64.getUrlEncoder().encode(realId.getBytes(StandardCharsets.UTF_8));
        return new String(bytes, StandardCharsets.UTF_8);
    }
}