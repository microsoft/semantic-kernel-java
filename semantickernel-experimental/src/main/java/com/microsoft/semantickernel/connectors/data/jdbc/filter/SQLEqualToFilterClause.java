// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.data.jdbc.filter;

import com.microsoft.semantickernel.connectors.data.jdbc.JDBCVectorStoreQueryProvider;
import com.microsoft.semantickernel.data.filter.EqualToFilterClause;
import com.microsoft.semantickernel.exceptions.SKException;

import java.time.OffsetDateTime;

/**
 * Represents an equality filter clause for SQL.
 */
public class SQLEqualToFilterClause extends EqualToFilterClause {

    /**
     * Initializes a new instance of the SQLEqualityFilterClause class.
     *
     * @param fieldName The field name to filter on.
     * @param value The value.
     */
    public SQLEqualToFilterClause(String fieldName, Object value) {
        super(fieldName, value);
    }

    /**
     * Gets the filter string.
     *
     * @return The filter string.
     */
    @Override
    public String getFilter() {
        String fieldName = JDBCVectorStoreQueryProvider.validateSQLidentifier(getFieldName());
        Object value = getValue();

        if (value instanceof String) {
            return String.format("%s = ?", fieldName);
        } else if (value instanceof Boolean) {
            return String.format("%s = ?", fieldName);
        } else if (value instanceof Integer) {
            return String.format("%s = ?", fieldName);
        } else if (value instanceof Long) {
            return String.format("%s = ?", fieldName);
        } else if (value instanceof Float) {
            return String.format("%s = ?", fieldName);
        } else if (value instanceof Double) {
            return String.format("%s = ?", fieldName);
        } else if (value instanceof OffsetDateTime) {
            return String.format("%s = ?", fieldName);
        } else {
            throw new SKException("Unsupported filter value type '"
                + value.getClass().getSimpleName() + "'.");
        }
    }
}
