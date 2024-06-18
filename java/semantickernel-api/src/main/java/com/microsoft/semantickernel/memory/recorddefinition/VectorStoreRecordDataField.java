package com.microsoft.semantickernel.memory.recorddefinition;

public class VectorStoreRecordDataField extends VectorStoreRecordField {
    private final boolean hasEmbedding;
    private final String embeddingName;
    public VectorStoreRecordDataField(String name) {
        this(name, false, null);
    }
    public VectorStoreRecordDataField(String name, boolean hasEmbedding, String embeddingName) {
        super(name);
        this.hasEmbedding = hasEmbedding;
        this.embeddingName = embeddingName;
    }

    public boolean hasEmbedding() {
        return hasEmbedding;
    }

    public String getEmbeddingName() {
        return embeddingName;
    }
}
