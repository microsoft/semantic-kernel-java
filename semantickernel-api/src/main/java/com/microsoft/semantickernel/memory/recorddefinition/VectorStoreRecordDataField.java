// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.memory.recorddefinition;

public class VectorStoreRecordDataField extends VectorStoreRecordField {
    private final boolean hasEmbedding;
    private final String embeddingName;

    /**
     * Creates a new instance of the VectorStoreRecordDataField class.
     *
     * @param name the name of the field
     */
    public VectorStoreRecordDataField(String name) {
        this(name, false, null);
    }

    /**
     * Creates a new instance of the VectorStoreRecordDataField class.
     *
     * @param name the name of the field
     * @param hasEmbedding a value indicating whether the field has an embedding
     * @param embeddingName the name of the embedding
     */
    public VectorStoreRecordDataField(String name, boolean hasEmbedding, String embeddingName) {
        super(name);
        this.hasEmbedding = hasEmbedding;
        this.embeddingName = embeddingName;
    }

    /**
     * Gets a value indicating whether the field has an embedding.
     *
     * @return a value indicating whether the field has an embedding
     */
    public boolean hasEmbedding() {
        return hasEmbedding;
    }

    /**
     * Gets the name of the embedding.
     *
     * @return the name of the embedding
     */
    public String getEmbeddingName() {
        return embeddingName;
    }
}
