// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.memory.redis;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.semantickernel.ai.embeddings.Embedding;
import com.microsoft.semantickernel.memory.MemoryRecord;
import com.microsoft.semantickernel.memory.MemoryRecordMetadata;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Represents an entry in the Semantic Kernel Memory Table. */
public class JsonMemoryRecord {

    static final String ID = "Id";
    static final String TEXT = "Text";
    static final String DESCRIPTION = "Description";
    static final String ADDITIONAL_METADATA = "AdditionalMetadata";
    static final String EMBEDDING = "Embedding";
    static final String EXTERNAL_SOURCE_NAME = "ExternalSourceName";
    static final String IS_REFERENCE = "Reference";
    static final String TIME = "Timestamp";

    private final String id;
    private final String text;
    private final String description;
    private final String additionalMetadata;
    private final List<Float> embedding;
    private final String externalSourceName;
    private final boolean isReference;

    @JsonCreator
    public JsonMemoryRecord(
            @JsonProperty(ID) String id,
            @JsonProperty(TEXT) String text,
            @JsonProperty(DESCRIPTION) String description,
            @JsonProperty(ADDITIONAL_METADATA) String additionalMetadata,
            @JsonProperty(EMBEDDING) List<Float> embedding,
            @JsonProperty(EXTERNAL_SOURCE_NAME) String externalSourceName,
            @JsonProperty(IS_REFERENCE) boolean isReference) {
        this.id = id;
        this.text = text;
        this.description = description;
        this.additionalMetadata = additionalMetadata;
        this.embedding = embedding != null ? embedding : Collections.emptyList();
        this.externalSourceName = externalSourceName;
        this.isReference = isReference;
    }

    /**
     * Record ID. The record is not filterable to save quota, also SK uses only semantic search.
     *
     * @return Record ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Content is stored here.
     *
     * @return Content is stored here, {@code null} if not set.
     */
    public String getText() {
        return text;
    }

    /**
     * Optional description of the content, e.g. a title. This can be useful when indexing external
     * data without pulling in the entire content.
     *
     * @return Optional description of the content, {@code null} if not set.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Additional metadata. Currently, this is a String where you could store serialized data as
     * JSON. In future the design might change to allow storing named values and leverage filters.
     *
     * @return Additional metadata, {@code null} if not set.
     */
    public String getAdditionalMetadata() {
        return additionalMetadata;
    }

    /**
     * Embedding vector.
     *
     * @return Embedding vector.
     */
    public List<Float> getEmbedding() {
        return Collections.unmodifiableList(embedding);
    }

    /**
     * Name of the external source, in cases where the content and the Id are referenced to external
     * information.
     *
     * @return Name of the external source, in cases where the content and the Id are referenced to
     *     external information, {@code null} if not set.
     */
    public String getExternalSourceName() {
        return externalSourceName;
    }

    /**
     * Whether the record references external information.
     *
     * @return {@code true} if the record references external information, {@code false} otherwise.
     */
    public boolean isReference() {
        return isReference;
    }

    public MemoryRecordMetadata toMemoryRecordMetadata() {
        return new MemoryRecordMetadata(
                this.isReference,
                decodeId(this.id),
                this.text,
                this.description,
                this.externalSourceName,
                this.additionalMetadata);
    }

    public static JsonMemoryRecord fromMemoryRecord(MemoryRecord record) {
        return new JsonMemoryRecord(
                record.getMetadata().getId(),
                record.getMetadata().getText(),
                record.getMetadata().getDescription(),
                record.getMetadata().getAdditionalMetadata(),
                record.getEmbedding().getVector(),
                record.getMetadata().getExternalSourceName(),
                record.getMetadata().isReference());
    }

    public MemoryRecord toMemoryRecord() {
        return toMemoryRecord(true);
    }

    public MemoryRecord toMemoryRecord(boolean withEmbeddings) {
        return new MemoryRecord(
                this.toMemoryRecordMetadata(),
                new Embedding(withEmbeddings ? this.embedding : Collections.emptyList()),
                this.id,
                null);
    }

    // ACS keys can contain only letters, digits, underscore, dash, equal sign, recommending
    // to encode values with a URL-safe algorithm.
    // <param name="realId">Original Id</param>
    // <returns>Encoded id</returns>
    static String encodeId(String realId) {
        byte[] bytes = Base64.getUrlEncoder().encode(realId.getBytes(StandardCharsets.UTF_8));
        return new String(bytes, StandardCharsets.UTF_8);
    }

    static String decodeId(String encodedId) {
        byte[] bytes = Base64.getUrlDecoder().decode(encodedId.getBytes(StandardCharsets.UTF_8));
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
