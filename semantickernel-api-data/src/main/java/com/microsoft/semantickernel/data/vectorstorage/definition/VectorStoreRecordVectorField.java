// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.vectorstorage.definition;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents a vector field in a record.
 */
public class VectorStoreRecordVectorField extends VectorStoreRecordField {
    private final int dimensions;
    private final IndexKind indexKind;
    private final DistanceFunction distanceFunction;

    /**
     * Create a builder for the VectorStoreRecordVectorField class.
     * @return a new instance of the builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a new instance of the VectorStoreRecordVectorField class.
     *
     * @param name the name of the field
     * @param storageName the storage name of the field
     * @param fieldType the field type
     * @param dimensions the number of dimensions in the vector
     * @param indexKind the index kind
     * @param distanceFunction the distance function
     */
    public VectorStoreRecordVectorField(
        @Nonnull String name,
        @Nullable String storageName,
        @Nonnull Class<?> fieldType,
        Class<?> fieldSubType,
        int dimensions,
        @Nullable IndexKind indexKind,
        @Nullable DistanceFunction distanceFunction) {
        super(name, storageName, fieldType, fieldSubType);
        this.dimensions = dimensions;
        this.indexKind = indexKind == null ? IndexKind.UNDEFINED : indexKind;
        this.distanceFunction = distanceFunction == null ? DistanceFunction.UNDEFINED
            : distanceFunction;
    }

    /**
     * Gets the number of dimensions in the vector.
     *
     * @return the number of dimensions in the vector
     */
    public int getDimensions() {
        return dimensions;
    }

    /**
     * Gets the index kind.
     *
     * @return the index kind
     */
    public IndexKind getIndexKind() {
        return indexKind;
    }

    /**
     * Gets the distance function.
     *
     * @return the distance function
     */
    public DistanceFunction getDistanceFunction() {
        return distanceFunction;
    }

    /**
     * A builder for the VectorStoreRecordVectorField class.
     */
    public static class Builder
        extends VectorStoreRecordField.Builder<VectorStoreRecordVectorField, Builder> {
        private int dimensions;
        private IndexKind indexKind = IndexKind.UNDEFINED;
        private DistanceFunction distanceFunction = DistanceFunction.UNDEFINED;

        /**
         * Sets the number of dimensions in the vector.
         *
         * @param dimensions the number of dimensions in the vector
         * @return the builder
         */
        public Builder withDimensions(int dimensions) {
            this.dimensions = dimensions;
            return this;
        }

        /**
         * Sets the index kind.
         *
         * @param indexKind the index kind
         * @return the builder
         */
        public Builder withIndexKind(IndexKind indexKind) {
            this.indexKind = indexKind;
            return this;
        }

        /**
         * Sets the distance function.
         *
         * @param distanceFunction the distance function
         * @return the builder
         */
        public Builder withDistanceFunction(DistanceFunction distanceFunction) {
            this.distanceFunction = distanceFunction;
            return this;
        }

        /**
         * Builds a new instance of the VectorStoreRecordVectorField class.
         *
         * @return a new instance of the VectorStoreRecordVectorField class
         */
        @Override
        public VectorStoreRecordVectorField build() {
            if (name == null) {
                throw new IllegalArgumentException("name is required");
            }
            if (fieldType == null) {
                throw new IllegalArgumentException("fieldType is required");
            }
            if (dimensions <= 0) {
                throw new IllegalArgumentException("dimensions must be greater than 0");
            }

            return new VectorStoreRecordVectorField(name, storageName, fieldType, fieldSubType,
                dimensions,
                indexKind,
                distanceFunction);
        }
    }
}
