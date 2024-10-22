// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.vectorstorage.options;

import com.microsoft.semantickernel.builders.SemanticKernelBuilder;

/**
 * Options for getting a record.
 */
public class GetRecordOptions {

    private final boolean includeVectors;

    private final boolean wildcardKeyMatching;

    /**
     * Creates a new instance of the GetRecordOptions class.
     * @param includeVectors A value indicating whether to include vectors in a response.
     */
    public GetRecordOptions(
        boolean includeVectors) {
        this.includeVectors = includeVectors;
        this.wildcardKeyMatching = false;
    }

    /**
     * Creates a new instance of the GetRecordOptions class.
     * @param includeVectors A value indicating whether to include vectors in a response.
     * @param wildcardKeyMatching A value indicating whether to use wildcard key matching.
     */
    public GetRecordOptions(
        boolean includeVectors,
        boolean wildcardKeyMatching) {
        this.includeVectors = includeVectors;
        this.wildcardKeyMatching = wildcardKeyMatching;
    }

    /**
     * Gets whether to use wildcard key matching.
     * @return {@code true} if wildcard key matching is used; otherwise, {@code false}.
     */
    public boolean isWildcardKeyMatching() {
        return wildcardKeyMatching;
    }

    /**
     * Creates a new builder.
     *
     * @return the builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * A builder for GetRecordOptions.
     */
    public static class Builder implements SemanticKernelBuilder<GetRecordOptions> {

        private boolean includeVectors;
        private boolean wildcardKeyMatching = false;

        /**
         * Sets whether to include vectors.
         *
         * @param includeVectors whether to include vectors
         * @return GetRecordOptions.Builder
         */
        public Builder includeVectors(boolean includeVectors) {
            this.includeVectors = includeVectors;
            return this;
        }

        /**
         * Sets whether to use wildcard key matching. Default is false. Wildcard key matching allows
         * for matching multiple ids, for instance using "LIKE 'a%'" on a SQL query.
         * <p>
         * NOTE: Currently this is only supported by the SQL connectors.
         *
         * @param wildcardKeyMatching whether to use wildcard key matching
         * @return GetRecordOptions.Builder
         */
        public Builder setWildcardKeyMatching(boolean wildcardKeyMatching) {
            this.wildcardKeyMatching = wildcardKeyMatching;
            return this;
        }

        /**
         * Builds the options.
         *
         * @return GetRecordOptions
         */
        @Override
        public GetRecordOptions build() {
            return new GetRecordOptions(includeVectors, wildcardKeyMatching);
        }
    }

    /**
     * Gets whether to include vectors.
     *
     * @return whether to include vectors
     */
    public boolean isIncludeVectors() {
        return includeVectors;
    }
}
