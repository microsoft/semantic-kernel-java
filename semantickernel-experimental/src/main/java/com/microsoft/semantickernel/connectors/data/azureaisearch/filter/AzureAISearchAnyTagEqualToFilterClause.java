// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.data.azureaisearch.filter;

import com.microsoft.semantickernel.data.filter.AnyTagEqualToFilterClause;

public class AzureAISearchAnyTagEqualToFilterClause extends AnyTagEqualToFilterClause {

    /**
     * Initializes a new instance of the AzureAISearchTagListContainsFilterClause class.
     *
     * @param fieldName The field name to filter on.
     * @param value The value.
     */
    public AzureAISearchAnyTagEqualToFilterClause(String fieldName, Object value) {
        super(fieldName, value);
    }

    /**
     * Gets the filter string.
     *
     * @return The filter string.
     */
    @Override
    public String getFilter() {
        return String.format("%s/any(t: t eq '%s')", getFieldName(), getValue());
    }
}
