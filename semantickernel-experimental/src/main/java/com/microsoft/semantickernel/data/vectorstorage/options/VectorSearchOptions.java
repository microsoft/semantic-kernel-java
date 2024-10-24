// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.vectorstorage.options;

import com.microsoft.semantickernel.builders.SemanticKernelBuilder;
import com.microsoft.semantickernel.data.vectorsearch.VectorSearchFilter;

import javax.annotation.Nullable;

/**
 * Options for a vector search.
 */
public class VectorSearchOptions {

    /**
     * The default limit of the number of results to return.
     */
    public static final int DEFAULT_TOP = 3;

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
    private final int top;
    private final int skip;
    private final boolean includeVectors;
    private final boolean includeTotalCount;

    /**
     * Creates a new instance of the VectorSearchOptions class.
     * @param vectorSearchFilter The vector search filter.
     * @param vectorFieldName The name of the vector field.
     * @param top The limit of the number of results to return.
     * @param skip The offset of the results to return.
     * @param includeVectors A value indicating whether to include vectors in the results.
     * @param includeTotalCount A value indicating whether to include the total count of the results.
     */
    public VectorSearchOptions(VectorSearchFilter vectorSearchFilter,
        String vectorFieldName, int top, int skip, boolean includeVectors,
        boolean includeTotalCount) {
        this.vectorSearchFilter = vectorSearchFilter;
        this.vectorFieldName = vectorFieldName;
        this.top = Math.max(1, top);
        this.skip = Math.max(0, skip);
        this.includeVectors = includeVectors;
        this.includeTotalCount = includeTotalCount;
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
    public int getTop() {
        return top;
    }

    /**
     * Gets the offset of the results to return.
     *
     * @return The offset of the results to return.
     */
    public int getSkip() {
        return skip;
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
     * Gets a value indicating whether to include the total count of the results.
     *
     * @return A value indicating whether to include the total count of the results.
     */
    public boolean isIncludeTotalCount() {
        return includeTotalCount;
    }

    /**
     * Creates a new instance of the Builder class.
     *
     * @return A new instance of the Builder class.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * A builder for the VectorSearchOptions class.
     */
    public static class Builder implements SemanticKernelBuilder<VectorSearchOptions> {
        private VectorSearchFilter vectorSearchFilter;
        private String vectorFieldName;
        private int top = DEFAULT_TOP;
        private int skip = 0;
        private boolean includeVectors = false;
        private boolean includeTotalCount = false;

        /**
         * Sets the vector search filter.
         * @param vectorSearchFilter the vector search filter
         * @return {@code this} builder
         */
        public Builder withVectorSearchFilter(
            VectorSearchFilter vectorSearchFilter) {
            this.vectorSearchFilter = vectorSearchFilter;
            return this;
        }

        /**
         * Sets the name of the vector field.
         * @param vectorFieldName the name of the vector field
         * @return {@code this} builder
         */
        public Builder withVectorFieldName(String vectorFieldName) {
            this.vectorFieldName = vectorFieldName;
            return this;
        }

        /**
         * Sets the limit of the number of results to return.
         * @param top the limit of the number of results to return
         * @return {@code this} builder
         */
        public Builder withTop(int top) {
            this.top = top;
            return this;
        }

        /**
         * Sets the offset of the results to return.
         * @param skip the offset of the results to return
         * @return {@code this} builder
         */
        public Builder withSkip(int skip) {
            this.skip = skip;
            return this;
        }

        /**
         * Sets a value indicating whether to include vectors in the results.
         * @param includeVectors a value indicating whether to include vectors in the results
         * @return {@code this} builder
         */
        public Builder withIncludeVectors(boolean includeVectors) {
            this.includeVectors = includeVectors;
            return this;
        }

        /**
         * Sets a value indicating whether to include the total count of the results.
         * @param includeTotalCount a value indicating whether to include the total count of the results
         * @return {@code this} builder
         */
        public Builder withIncludeTotalCount(boolean includeTotalCount) {
            this.includeTotalCount = includeTotalCount;
            return this;
        }

        /**
         * Builds a new instance of the VectorSearchOptions class.
         * @return a new instance of the VectorSearchOptions class
         */
        @Override
        public VectorSearchOptions build() {
            return new VectorSearchOptions(vectorSearchFilter, vectorFieldName, top, skip,
                includeVectors, includeTotalCount);
        }
    }
}
