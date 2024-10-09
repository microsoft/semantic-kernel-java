// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.filter;

import com.microsoft.semantickernel.data.vectorsearch.VectorSearchFilter;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordDefinition;

public interface FilterMapping {
    /**
     * Gets the filter string for the given vector search filter and record definition.
     *
     * @param filter The filter to get the filter string for.
     * @param recordDefinition The record definition to get the filter string for.
     * @return The filter string.
     */
    String getFilter(VectorSearchFilter filter, VectorStoreRecordDefinition recordDefinition);

    /**
     * Gets the filter string for the given equal to filter clause.
     *
     * @param filterClause The equal to filter clause to get the filter string for.
     * @return The filter string.
     */
    String getEqualToFilter(EqualToFilterClause filterClause);

    /**
     * Gets the filter string for the given any tag equal to filter clause.
     *
     * @param filterClause The any tag equal to filter clause to get the filter string for.
     * @return The filter string.
     */
    String getAnyTagEqualToFilter(AnyTagEqualToFilterClause filterClause);
}
