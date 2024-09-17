// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.vectorsearch;

import com.microsoft.semantickernel.data.filter.EqualToFilterClause;
import com.microsoft.semantickernel.data.filter.FilterClause;
import com.microsoft.semantickernel.data.filter.AnyTagEqualToFilterClause;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final List<FilterClause> filterClauses = new ArrayList<>();

        /**
         * Adds an EqualToFilterClause to the filter.
         *
         * @param equalToFilterClause The EqualToFilterClause to add.
         * @return The builder.
         */
        public Builder withEqualToFilterClause(EqualToFilterClause equalToFilterClause) {
            filterClauses.add(equalToFilterClause);
            return this;
        }

        /**
         * Adds an AnyTagEqualToFilterClause to the filter.
         *
         * @param anyTagEqualToFilterClause The AnyTagEqualToFilterClause clause to add.
         * @return The builder.
         */
        public Builder withAnyTagEqualToFilterClause(
            AnyTagEqualToFilterClause anyTagEqualToFilterClause) {
            filterClauses.add(anyTagEqualToFilterClause);
            return this;
        }

        public VectorSearchFilter build() {
            return new VectorSearchFilter(filterClauses);
        }
    }
}
