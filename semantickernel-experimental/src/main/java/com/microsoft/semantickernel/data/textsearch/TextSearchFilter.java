// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.textsearch;

import com.microsoft.semantickernel.data.filter.EqualToFilterClause;
import com.microsoft.semantickernel.data.filter.FilterClause;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a text search filter.
 */
public class TextSearchFilter {

    private final List<FilterClause> filterClauses;

    /**
     * Creates a new instance of the TextSearchFilter class.
     */
    public TextSearchFilter() {
        this(Collections.emptyList());
    }

    /**
     * Creates a new instance of the TextSearchFilter class.
     *
     * @param filterClauses The filter clauses.
     */
    public TextSearchFilter(List<FilterClause> filterClauses) {
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
     * Creates a new instance of the {@link Builder} class.
     *
     * @return The builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * The builder for the {@link TextSearchFilter} class.
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
         * Builds the filter.
         *
         * @return The filter.
         */
        public TextSearchFilter build() {
            return new TextSearchFilter(filterClauses);
        }
    }
}
