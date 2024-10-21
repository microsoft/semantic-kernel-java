// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.data.jdbc;

import com.microsoft.semantickernel.connectors.data.jdbc.filter.SQLEqualToFilterClause;
import com.microsoft.semantickernel.data.vectorsearch.VectorSearchFilter;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordDefinition;
import com.microsoft.semantickernel.exceptions.SKException;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A mapping for searching a collection of vector records in SQL.
 */
public class SQLVectorStoreRecordCollectionSearchMapping {

    /**
     * Builds a filter for searching a collection of vector records in SQL.
     *
     * @param vectorSearchFilter the search filter
     * @param recordDefinition   the record definition
     * @return the filter
     */
    public static String buildFilter(VectorSearchFilter vectorSearchFilter,
        VectorStoreRecordDefinition recordDefinition) {
        if (vectorSearchFilter == null
            || vectorSearchFilter.getFilterClauses().isEmpty()) {
            return "";
        }

        return vectorSearchFilter.getFilterClauses().stream().map(filterClause -> {
            if (filterClause instanceof SQLEqualToFilterClause) {
                SQLEqualToFilterClause equalityFilterClause = (SQLEqualToFilterClause) filterClause;
                // Create new instance with the storage name of the field
                return new SQLEqualToFilterClause(
                    recordDefinition.getField(equalityFilterClause.getFieldName())
                        .getEffectiveStorageName(),
                    equalityFilterClause.getValue()).getFilter();
            } else {
                throw new SKException("Unsupported filter clause type '"
                    + filterClause.getClass().getSimpleName() + "'.");
            }
        }).collect(Collectors.joining(" AND "));
    }

    /**
     * Gets the filter parameters.
     *
     * @param vectorSearchFilter the search filter
     * @return the filter parameters
     */
    public static List<Object> getFilterParameters(VectorSearchFilter vectorSearchFilter) {
        if (vectorSearchFilter == null
            || vectorSearchFilter.getFilterClauses().isEmpty()) {
            return Collections.emptyList();
        }

        return vectorSearchFilter.getFilterClauses().stream().map(filterClause -> {
            if (filterClause instanceof SQLEqualToFilterClause) {
                SQLEqualToFilterClause equalityFilterClause = (SQLEqualToFilterClause) filterClause;
                return equalityFilterClause.getValue();
            } else {
                throw new SKException("Unsupported filter clause type '"
                    + filterClause.getClass().getSimpleName() + "'.");
            }
        }).collect(Collectors.toList());
    }
}
