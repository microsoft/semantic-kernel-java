// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.vectorsearch.filtering;

public abstract class EqualityFilterClause implements FilterClause {

    private final String fieldName;
    private final Object value;

    public EqualityFilterClause(String fieldName, Object value) {
        this.fieldName = fieldName;
        this.value = value;
    }

    /**
     * Gets the field name to filter on.
     *
     * @return The field name to filter on.
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * Gets the value.
     *
     * @return The value.
     */
    public Object getValue() {
        return value;
    }
}
