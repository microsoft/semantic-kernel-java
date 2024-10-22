// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.filter;

/**
 * A filter clause that filters on a field equal to a value.
 */
public class EqualToFilterClause implements FilterClause {

    private final String fieldName;
    private final Object value;

    /**
     * Initializes a new instance of the EqualToFilterClause class.
     *
     * @param fieldName The field name to filter on.
     * @param value The value to filter on.
     */
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

    /**
     * Gets the filter string.
     *
     * @return The filter.
     */
    @Override
    public String getFilter() {
        throw new UnsupportedOperationException(String.format(
            "Not implemented. Use one of %s derived classes.", this.getClass().getSimpleName()));
    }
}
