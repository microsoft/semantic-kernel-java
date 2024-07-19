// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.recorddefinition;

public class VectorStoreRecordDataField extends VectorStoreRecordField {
    private final boolean hasEmbedding;
    private final String embeddingFieldName;
    private final Class<?> fieldType;
    private final boolean isFilterable;

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a new instance of the VectorStoreRecordDataField class.
     *
     * @param name the name of the field
     * @param storageName the storage name of the field
     * @param hasEmbedding a value indicating whether the field has an embedding
     * @param embeddingFieldName the name of the embedding
     * @param fieldType the field type
     * @param isFilterable a value indicating whether the field is filterable
     */
    public VectorStoreRecordDataField(
        String name,
        String storageName,
        boolean hasEmbedding,
        String embeddingFieldName,
        Class<?> fieldType,
        boolean isFilterable) {
        super(name, storageName);
        this.hasEmbedding = hasEmbedding;
        this.embeddingFieldName = embeddingFieldName;
        this.fieldType = fieldType;
        this.isFilterable = isFilterable;
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
    public String getEmbeddingFieldName() {
        return embeddingFieldName;
    }

    /**
     * Gets the field type.
     *
     * @return the field type
     */
    public Class<?> getFieldType() {
        return fieldType;
    }

    /**
     * Gets a value indicating whether the field is filterable.
     *
     * @return a value indicating whether the field is filterable
     */
    public boolean isFilterable() {
        return isFilterable;
    }

    public static class Builder
        extends VectorStoreRecordField.Builder<VectorStoreRecordDataField, Builder> {
        private boolean hasEmbedding;
        private String embeddingFieldName;
        private Class<?> fieldType;
        private boolean isFilterable;

        /**
         * Sets a value indicating whether the field has an embedding.
         *
         * @param hasEmbedding a value indicating whether the field has an embedding
         * @return the builder
         */
        public Builder withHasEmbedding(boolean hasEmbedding) {
            this.hasEmbedding = hasEmbedding;
            return this;
        }

        /**
         * Sets the name of the embedding field.
         *
         * @param embeddingFieldName the name of the embedding
         * @return the builder
         */
        public Builder withEmbeddingFieldName(String embeddingFieldName) {
            this.embeddingFieldName = embeddingFieldName;
            return this;
        }

        /**
         * Sets the field type.
         *
         * @param fieldType the field type
         * @return the builder
         */
        public Builder withFieldType(Class<?> fieldType) {
            this.fieldType = fieldType;
            return this;
        }

        /**
         * Sets a value indicating whether the field is filterable.
         *
         * @param isFilterable a value indicating whether the field is filterable
         * @return the builder
         */
        public Builder withIsFilterable(boolean isFilterable) {
            this.isFilterable = isFilterable;
            return this;
        }

        /**
         * Builds a new instance of the VectorStoreRecordDataField class.
         *
         * @return a new instance of the VectorStoreRecordDataField class
         */
        @Override
        public VectorStoreRecordDataField build() {
            if (name == null) {
                throw new IllegalArgumentException("name is required");
            }
            if (hasEmbedding && embeddingFieldName == null) {
                throw new IllegalArgumentException(
                    "embeddingFieldName is required when hasEmbedding is true");
            }

            return new VectorStoreRecordDataField(
                name,
                storageName,
                hasEmbedding,
                embeddingFieldName,
                fieldType,
                isFilterable);
        }
    }

}
