// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.vectorsearch.filtering;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public BasicVectorSearchFilter(List<FilterClause> filterClauses) {
        this.filterClauses = new ArrayList<>(filterClauses);
    }

    /**
     * Gets the filter clauses.
     *
     * @return The filter clauses.
     */
    public List<FilterClause> getFilterClauses() {
        return Collections.unmodifiableList(filterClauses);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final List<FilterClause> filterClauses = new ArrayList<>();

        public Builder equality(EqualityFilterClause equalityFilterClause) {
            filterClauses.add(equalityFilterClause);
            return this;
        }

        public Builder tagListContains(TagListContainsFilterClause tagListContainsFilterClause) {
            filterClauses.add(tagListContainsFilterClause);
            return this;
        }

        public BasicVectorSearchFilter build() {
            BasicVectorSearchFilter basicVectorSearchFilter = new BasicVectorSearchFilter();
            basicVectorSearchFilter.filterClauses.addAll(filterClauses);
            return basicVectorSearchFilter;
        }
    }
}
