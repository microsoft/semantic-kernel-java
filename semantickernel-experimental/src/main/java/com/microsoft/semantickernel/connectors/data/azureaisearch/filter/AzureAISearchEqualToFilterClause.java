// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.data.azureaisearch.filter;

import com.microsoft.semantickernel.data.filter.EqualToFilterClause;
import com.microsoft.semantickernel.exceptions.SKException;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class AzureAISearchEqualToFilterClause extends EqualToFilterClause {

    /**
     * Initializes a new instance of the AzureAISearchEqualityFilterClause class.
     *
     * @param fieldName The field name to filter on.
     * @param value The value.
     */
    public AzureAISearchEqualToFilterClause(String fieldName, Object value) {
        super(fieldName, value);
    }

    /**
     * Gets the filter string.
     *
     * @return The filter string.
     */
    @Override
    public String getFilter() {
        String fieldName = getFieldName();
        Object value = getValue();

        if (value instanceof String) {
            return String.format("%s eq '%s'", fieldName, value);
        } else if (value instanceof Boolean) {
            return String.format("%s eq %s", fieldName,
                value.toString().toLowerCase());
        } else if (value instanceof Integer) {
            return String.format("%s eq %d", fieldName, (Integer) value);
        } else if (value instanceof Long) {
            return String.format("%s eq %d", fieldName, (Long) value);
        } else if (value instanceof Float) {
            return String.format("%s eq %f", fieldName, (Float) value);
        } else if (value instanceof Double) {
            return String.format("%s eq %f", fieldName, (Double) value);
        } else if (value instanceof OffsetDateTime) {
            return String.format("%s eq %s", fieldName, ((OffsetDateTime) value)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        } else if (value == null) {
            return String.format("%s eq null", fieldName);
        } else {
            throw new SKException("Unsupported filter value type '"
                + value.getClass().getSimpleName() + "'.");
        }
    }
}
