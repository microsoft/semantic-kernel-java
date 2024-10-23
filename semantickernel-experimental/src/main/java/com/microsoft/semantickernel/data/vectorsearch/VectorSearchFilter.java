// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.vectorsearch;

import com.microsoft.semantickernel.data.filter.EqualToFilterClause;
import com.microsoft.semantickernel.data.filter.FilterClause;
import com.microsoft.semantickernel.data.filter.AnyTagEqualToFilterClause;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A vector search filter.
 */
public class VectorSearchFilter {

    /**
     * Creates a new instance of the VectorSearchFilter class.
     *
     * @return A new instance of the VectorSearchFilter class.
     */
    public static VectorSearchFilter createDefault() {
        return new VectorSearchFilter();
    }

    private final List<FilterClause> filterClauses;

    /**
     * Creates a new instance of the VectorSearchFilter class.
     */
    public VectorSearchFilter() {
        this(Collections.emptyList());
    }

    /**
     * Creates a new instance of the VectorSearchFilter class.
     *
     * @param filterClauses The filter clauses.
     */
    public VectorSearchFilter(List<FilterClause> filterClauses) {
        this.filterClauses = Collections.unmodifiableList(filterClauses);
    }

    /**
     * Gets the filter clauses.
     *
     * @return The filter clauses.
     */
    public List<FilterClause> getFilterClauses() {
        return filterClauses;
    }

    /**
     * Creates a {@link Builder} for the VectorSearchFilter class.
     *
     * @return A new instance of the VectorSearchFilter Builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * A builder for the VectorSearchFilter class.
     */
    public static class Builder {
        private final List<FilterClause> filterClauses = new ArrayList<>();

        /**
         * Adds an EqualToFilterClause to the filter.
         *
         * @param fieldName The field name to filter on.
         * @param value The value.
         * @return The builder.
         */
        public Builder equalTo(String fieldName, Object value) {
            filterClauses.add(new EqualToFilterClause(fieldName, value));
            return this;
        }

        /**
         * Adds an AnyTagEqualToFilterClause to the filter.
         *
         * @param fieldName The field name to filter on.
         * @param value The value.
         * @return The builder.
         */
        public Builder anyTagEqualTo(String fieldName, Object value) {
            filterClauses.add(new AnyTagEqualToFilterClause(fieldName, value));
            return this;
        }

        /**
         * Builds the VectorSearchFilter.
         *
         * @return The VectorSearchFilter.
         */
        public VectorSearchFilter build() {
            return new VectorSearchFilter(filterClauses);
        }
    }
}
