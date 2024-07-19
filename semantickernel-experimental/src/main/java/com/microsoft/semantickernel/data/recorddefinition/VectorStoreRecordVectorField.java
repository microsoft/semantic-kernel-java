// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.recorddefinition;

/**
 * Represents a vector field in a record.
 */
public class VectorStoreRecordVectorField extends VectorStoreRecordField {
    private final int dimensions;
    private final IndexKind indexKind;
    private final DistanceFunction distanceFunction;

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a new instance of the VectorStoreRecordVectorField class.
     *
     * @param name the name of the field
     * @param storageName the storage name of the field
     * @param dimensions the number of dimensions in the vector
     * @param indexKind the index kind
     * @param distanceFunction the distance function
     */
    public VectorStoreRecordVectorField(
        String name,
        String storageName,
        int dimensions,
        IndexKind indexKind,
        DistanceFunction distanceFunction) {
        super(name, storageName);
        this.dimensions = dimensions;
        this.indexKind = indexKind;
        this.distanceFunction = distanceFunction;
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

    public static class Builder
        extends VectorStoreRecordField.Builder<VectorStoreRecordVectorField, Builder> {
        private int dimensions;
        private IndexKind indexKind;
        private DistanceFunction distanceFunction;

        public Builder withDimensions(int dimensions) {
            this.dimensions = dimensions;
            return this;
        }

        public Builder withIndexKind(IndexKind indexKind) {
            this.indexKind = indexKind;
            return this;
        }

        public Builder withDistanceFunction(DistanceFunction distanceFunction) {
            this.distanceFunction = distanceFunction;
            return this;
        }

        public VectorStoreRecordVectorField build() {
            if (name == null) {
                throw new IllegalArgumentException("name is required");
            }
            if (dimensions <= 0) {
                throw new IllegalArgumentException("dimensions must be greater than 0");
            }

            return new VectorStoreRecordVectorField(name, storageName, dimensions, indexKind,
                distanceFunction);
        }
    }
}
