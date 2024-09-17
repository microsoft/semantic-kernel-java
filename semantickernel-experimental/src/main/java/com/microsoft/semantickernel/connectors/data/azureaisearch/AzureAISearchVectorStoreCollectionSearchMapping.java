// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.data.azureaisearch;

import com.microsoft.semantickernel.connectors.data.azureaisearch.filter.AzureAISearchEqualToFilterClause;
import com.microsoft.semantickernel.connectors.data.azureaisearch.filter.AzureAISearchAnyTagEqualToFilterClause;
import com.microsoft.semantickernel.data.vectorsearch.VectorSearchFilter;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordDefinition;
import com.microsoft.semantickernel.exceptions.SKException;

import java.util.stream.Collectors;

class AzureAISearchVectorStoreCollectionSearchMapping {
    public static String buildFilterString(VectorSearchFilter vectorSearchFilter,
        VectorStoreRecordDefinition recordDefinition) {
        if (vectorSearchFilter == null
            || vectorSearchFilter.getFilterClauses().isEmpty()) {
            return "";
        }

        return String.join(" and ",
            vectorSearchFilter.getFilterClauses().stream().map(filterClause -> {
                if (filterClause instanceof AzureAISearchEqualToFilterClause) {
                    AzureAISearchEqualToFilterClause azureFilterClause = (AzureAISearchEqualToFilterClause) filterClause;
                    // Create new instance with the storage name of the field
                    return new AzureAISearchEqualToFilterClause(
                        recordDefinition.getField(azureFilterClause.getFieldName())
                            .getEffectiveStorageName(),
                        azureFilterClause.getValue())
                        .getFilter();
                } else if (filterClause instanceof AzureAISearchAnyTagEqualToFilterClause) {
                    AzureAISearchAnyTagEqualToFilterClause azureFilterClause = (AzureAISearchAnyTagEqualToFilterClause) filterClause;
                    // Create new instance with the storage name of the field
                    return new AzureAISearchAnyTagEqualToFilterClause(
                        recordDefinition.getField(azureFilterClause.getFieldName())
                            .getEffectiveStorageName(),
                        azureFilterClause.getValue())
                        .getFilter();
                } else {
                    throw new SKException("Unsupported filter clause type '"
                        + filterClause.getClass().getSimpleName() + "'.");
                }
            })
                .collect(Collectors.toList()));
    }
}
