// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.vectorstorage.options;

import com.microsoft.semantickernel.builders.SemanticKernelBuilder;
import com.microsoft.semantickernel.data.vectorsearch.VectorSearchFilter;

import javax.annotation.Nullable;

public class VectorSearchOptions {

    public static final int DEFAULT_RESULT_LIMIT = 3;

    /**
     * Creates a new instance of the VectorSearchOptions class with default values.
     *
     * @param vectorFieldName The name of the vector field.
     * @return A new instance of the VectorSearchOptions class with default values.
     */
    public static VectorSearchOptions createDefault(String vectorFieldName) {
        return VectorSearchOptions.builder()
            .withVectorFieldName(vectorFieldName).build();
    }

    @Nullable
    private final VectorSearchFilter vectorSearchFilter;
    @Nullable
    private final String vectorFieldName;
    private final int limit;
    private final int offset;
    private final boolean includeVectors;

    public VectorSearchOptions(VectorSearchFilter vectorSearchFilter,
        String vectorFieldName, int limit, int offset, boolean includeVectors) {
        this.vectorSearchFilter = vectorSearchFilter;
        this.vectorFieldName = vectorFieldName;
        this.limit = Math.max(1, limit);
        this.offset = Math.max(0, offset);
        this.includeVectors = includeVectors;
    }

    /**
     * Gets the vector search filter.
     *
     * @return The vector search filter.
     */
    @Nullable
    public VectorSearchFilter getVectorSearchFilter() {
        return vectorSearchFilter;
    }

    /**
     * Gets the name of the vector field.
     *
     * @return The name of the vector field.
     */
    @Nullable
    public String getVectorFieldName() {
        return vectorFieldName;
    }

    /**
     * Gets the limit of the number of results to return.
     *
     * @return The limit of the number of results to return.
     */
    public int getLimit() {
        return limit;
    }

    /**
     * Gets the offset of the results to return.
     *
     * @return The offset of the results to return.
     */
    public int getOffset() {
        return offset;
    }

    /**
     * Gets a value indicating whether to include vectors in the results.
     *
     * @return A value indicating whether to include vectors in the results.
     */
    public boolean isIncludeVectors() {
        return includeVectors;
    }

    /**
     * Creates a new instance of the Builder class.
     *
     * @return A new instance of the Builder class.
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder implements SemanticKernelBuilder<VectorSearchOptions> {
        private VectorSearchFilter vectorSearchFilter;
        private String vectorFieldName;
        private int limit;
        private int offset;
        private boolean includeVectors;

        public Builder() {
            this.limit = DEFAULT_RESULT_LIMIT;
        }

        public Builder withVectorSearchFilter(
            VectorSearchFilter vectorSearchFilter) {
            this.vectorSearchFilter = vectorSearchFilter;
            return this;
        }

        public Builder withVectorFieldName(String vectorFieldName) {
            this.vectorFieldName = vectorFieldName;
            return this;
        }

        public Builder withLimit(int limit) {
            this.limit = limit;
            return this;
        }

        public Builder withOffset(int offset) {
            this.offset = offset;
            return this;
        }

        public Builder withIncludeVectors(boolean includeVectors) {
            this.includeVectors = includeVectors;
            return this;
        }

        @Override
        public VectorSearchOptions build() {
            return new VectorSearchOptions(vectorSearchFilter, vectorFieldName, limit, offset,
                includeVectors);
        }
    }
}
