// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.filter;

public abstract class EqualToFilterClause implements FilterClause {

    private final String fieldName;
    private final Object value;

    public EqualToFilterClause(String fieldName, Object value) {
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
