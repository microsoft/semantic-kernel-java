// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.filter;

/**
 * A filter clause for a query.
 */
public interface FilterClause {

    /**
     * Gets the filter string.
     *
     * @return The filter.
     */
    String getFilter();
}
