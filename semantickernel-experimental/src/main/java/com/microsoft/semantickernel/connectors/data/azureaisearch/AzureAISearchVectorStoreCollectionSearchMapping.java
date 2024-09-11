// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.data.azureaisearch;

import com.microsoft.semantickernel.data.filtering.BasicVectorSearchFilter;
import com.microsoft.semantickernel.exceptions.SKException;

import java.util.Map;
import java.util.stream.Collectors;

class AzureAISearchVectorStoreCollectionSearchMapping {
    public static String buildFilterString(BasicVectorSearchFilter basicVectorSearchFilter,
        Map<String, String> storageNames) {
        if (basicVectorSearchFilter == null
            || basicVectorSearchFilter.getFilterClauses().isEmpty()) {
            return "";
        }

        return String.join(" and ",
            basicVectorSearchFilter.getFilterClauses().stream().map(filterClause -> {
                if (filterClause instanceof AzureAISearchEqualityFilterClause) {
                    AzureAISearchEqualityFilterClause azureFilterClause = (AzureAISearchEqualityFilterClause) filterClause;
                    // Create new instance with the storage name of the field
                    return new AzureAISearchEqualityFilterClause(
                        storageNames.get(azureFilterClause.getFieldName()),
                        azureFilterClause.getValue()).getFilter();
                } else if (filterClause instanceof AzureAISearchTagListContainsFilterClause) {
                    AzureAISearchTagListContainsFilterClause azureFilterClause = (AzureAISearchTagListContainsFilterClause) filterClause;
                    // Create new instance with the storage name of the field
                    return new AzureAISearchTagListContainsFilterClause(
                        storageNames.get(azureFilterClause.getFieldName()),
                        azureFilterClause.getValue()).getFilter();
                } else {
                    throw new SKException("Unsupported filter clause type '"
                        + filterClause.getClass().getSimpleName() + "'.");
                }
            })
                .collect(Collectors.toList()));
    }
}
