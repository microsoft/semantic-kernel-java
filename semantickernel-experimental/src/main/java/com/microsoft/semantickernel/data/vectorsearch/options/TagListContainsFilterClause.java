// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.vectorsearch.options;

public class TagListContainsFilterClause extends FilterClause {
    private final String fieldName;
    private final String value;

    /**
     * Creates a new instance of the TagListContainsFilterClause class.
     *
     * @param fieldName The name of the field to filter on.
     * @param value The value to filter on.
     */
    public TagListContainsFilterClause(String fieldName, String value) {
        super(FilterClauseType.TAG_LIST_CONTAINS);
        this.fieldName = fieldName;
        this.value = value;
    }

    /**
     * Gets the name of the field to filter on.
     *
     * @return The name of the field to filter on.
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * Gets the value to filter on.
     *
     * @return The value to filter on.
     */
    public String getValue() {
        return value;
    }
}
