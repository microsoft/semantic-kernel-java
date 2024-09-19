// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.data.redis.filter;

import com.microsoft.semantickernel.data.filter.EqualToFilterClause;
import com.microsoft.semantickernel.exceptions.SKException;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class RedisEqualToFilterClause extends EqualToFilterClause {
    public RedisEqualToFilterClause(String fieldName, Object value) {
        super(fieldName, value);
    }

    /**
     * Gets the filter string.
     *
     * @return The filter.
     */
    @Override
    public String getFilter() {
        String fieldName = getFieldName();
        Object value = getValue();
        String formattedValue;

        if (value instanceof String) {
            formattedValue = String.format("\"%s\"", value);
        } else if (value instanceof Boolean) {
            formattedValue = value.toString().toLowerCase();
        } else if (value instanceof Number) {
            formattedValue = value.toString();
        } else if (value instanceof OffsetDateTime) {
            formattedValue = ((OffsetDateTime) value)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        } else {
            throw new SKException("Unsupported filter value type '"
                + value.getClass().getSimpleName() + "'.");
        }

        return String.format("@%s:%s", fieldName, formattedValue);
    }
}
