// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.data.azureaisearch;

import com.microsoft.semantickernel.data.filter.AnyTagEqualToFilterClause;
import com.microsoft.semantickernel.data.filter.EqualToFilterClause;
import com.microsoft.semantickernel.data.vectorsearch.VectorSearchFilter;
import com.microsoft.semantickernel.data.filter.FilterMapping;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordDefinition;
import com.microsoft.semantickernel.exceptions.SKException;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

class AzureAISearchVectorStoreCollectionSearchMapping
    implements FilterMapping {

    private AzureAISearchVectorStoreCollectionSearchMapping() {
    }

    private static class AzureAISearchVectorStoreCollectionSearchMappingHolder {
        private static final AzureAISearchVectorStoreCollectionSearchMapping INSTANCE = new AzureAISearchVectorStoreCollectionSearchMapping();
    }

    static AzureAISearchVectorStoreCollectionSearchMapping getInstance() {
        return AzureAISearchVectorStoreCollectionSearchMappingHolder.INSTANCE;
    }

    public String getFilter(VectorSearchFilter vectorSearchFilter,
        VectorStoreRecordDefinition recordDefinition) {
        if (vectorSearchFilter == null
            || vectorSearchFilter.getFilterClauses().isEmpty()) {
            return "";
        }

        return vectorSearchFilter.getFilterClauses().stream().map(filterClause -> {
            if (filterClause instanceof EqualToFilterClause) {
                EqualToFilterClause equalToFilterClause = (EqualToFilterClause) filterClause;
                // Create new instance with the storage name of the field
                return getEqualToFilter(new EqualToFilterClause(
                    recordDefinition.getField(equalToFilterClause.getFieldName())
                        .getEffectiveStorageName(),
                    equalToFilterClause.getValue()));
            } else if (filterClause instanceof AnyTagEqualToFilterClause) {
                AnyTagEqualToFilterClause anyTagEqualToFilterClause = (AnyTagEqualToFilterClause) filterClause;
                // Create new instance with the storage name of the field
                return getAnyTagEqualToFilter(new AnyTagEqualToFilterClause(
                    recordDefinition.getField(anyTagEqualToFilterClause.getFieldName())
                        .getEffectiveStorageName(),
                    anyTagEqualToFilterClause.getValue()));
            } else {
                throw new SKException("Unsupported filter clause type '"
                    + filterClause.getClass().getSimpleName() + "'.");
            }
        }).collect(Collectors.joining(" and "));
    }

    @Override
    public String getEqualToFilter(EqualToFilterClause filterClause) {
        String fieldName = filterClause.getFieldName();
        Object value = filterClause.getValue();

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

    @Override
    public String getAnyTagEqualToFilter(AnyTagEqualToFilterClause filterClause) {
        return String.format("%s/any(t: t eq '%s')", filterClause.getFieldName(),
            filterClause.getValue());
    }
}
