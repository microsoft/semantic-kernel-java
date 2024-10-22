// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.filter;

/**
 * A filter clause that filters on any tag equal to a value.
 */
public abstract class AnyTagEqualToFilterClause implements FilterClause {

    private final String fieldName;
    private final Object value;

    /**
     * Creates a new instance of the AnyTagEqualToFilterClause class.
     * @param fieldName The field name to filter on.
     * @param value The value to filter on.
     */
    public AnyTagEqualToFilterClause(String fieldName, Object value) {
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
