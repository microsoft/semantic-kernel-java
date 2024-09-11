// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.filtering;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BasicVectorSearchFilter {

    /**
     * Creates a new instance of the BasicVectorSearchFilter class.
     *
     * @return A new instance of the BasicVectorSearchFilter class.
     */
    public static BasicVectorSearchFilter createDefault() {
        return new BasicVectorSearchFilter();
    }

    private final List<FilterClause> filterClauses;

    public BasicVectorSearchFilter() {
        this(new ArrayList<>());
    }

    /**
     * Creates a new instance of the BasicVectorSearchFilter class.
     *
     * @param filterClauses The filter clauses.
     */
    public BasicVectorSearchFilter(List<FilterClause> filterClauses) {
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
         * Adds an equality filter clause to the filter.
         *
         * @param equalityFilterClause The equality filter clause to add.
         * @return The builder.
         */
        public Builder equality(EqualityFilterClause equalityFilterClause) {
            filterClauses.add(equalityFilterClause);
            return this;
        }

        /**
         * Adds a tag list contains filter clause to the filter.
         *
         * @param tagListContainsFilterClause The tag list contains filter clause to add.
         * @return The builder.
         */
        public Builder tagListContains(TagListContainsFilterClause tagListContainsFilterClause) {
            filterClauses.add(tagListContainsFilterClause);
            return this;
        }

        public BasicVectorSearchFilter build() {
            return new BasicVectorSearchFilter(filterClauses);
        }
    }
}
