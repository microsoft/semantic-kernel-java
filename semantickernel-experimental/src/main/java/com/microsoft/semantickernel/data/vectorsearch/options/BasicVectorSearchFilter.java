// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.vectorsearch.options;

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
        this.filterClauses = new ArrayList<>();
    }

    /**
     * Adds an equality filter clause to the filter.
     *
     * @param field The field to filter on.
     * @param value The value to filter on.
     * @return The BasicVectorSearchFilter object.
     */
    public BasicVectorSearchFilter equality(String field, Object value) {
        filterClauses.add(new EqualityFilterClause(field, value));
        return this;
    }

    /**
     * Adds clause to the filter that checks if the tag list contains the specified value.
     *
     * @param field The field to filter on.
     * @param value The value to filter on.
     * @return The BasicVectorSearchFilter object.
     */
    public BasicVectorSearchFilter tagListContains(String field, String value) {
        filterClauses.add(new TagListContainsFilterClause(field, value));
        return this;
    }

    /**
     * Gets the filter clauses.
     *
     * @return The filter clauses.
     */
    public List<FilterClause> getFilterClauses() {
        return Collections.unmodifiableList(filterClauses);
    }
}
