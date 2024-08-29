// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.vectorsearch.options;

public class FilterClause {

    /**
     * The type of the filter clause.
     */
    private final FilterClauseType type;

    /**
     * Creates a new instance of the FilterClause class.
     *
     * @param type The type of the filter clause.
     */
    public FilterClause(FilterClauseType type) {
        this.type = type;
    }

    /**
     * Gets the type of the filter clause.
     *
     * @return The type of the filter clause.
     */
    public FilterClauseType getType() {
        return type;
    }
}
