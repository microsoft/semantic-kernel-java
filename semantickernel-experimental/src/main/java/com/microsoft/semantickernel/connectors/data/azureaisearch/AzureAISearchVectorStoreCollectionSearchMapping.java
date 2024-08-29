// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.data.azureaisearch;

import com.microsoft.semantickernel.data.vectorsearch.options.BasicVectorSearchFilter;
import com.microsoft.semantickernel.data.vectorsearch.options.EqualityFilterClause;
import com.microsoft.semantickernel.data.vectorsearch.options.TagListContainsFilterClause;
import com.microsoft.semantickernel.exceptions.SKException;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AzureAISearchVectorStoreCollectionSearchMapping {
    public static String buildFilterString(BasicVectorSearchFilter basicVectorSearchFilter,
        Map<String, String> storageNames) {
        if (basicVectorSearchFilter == null) {
            return "";
        }

        String filter = "";

        if (basicVectorSearchFilter.getFilterClauses() != null) {

            List<String> filterStrings = basicVectorSearchFilter.getFilterClauses().stream()
                .filter(filterClause -> filterClause instanceof EqualityFilterClause)
                .map(filterClause -> {
                    String fieldName = ((EqualityFilterClause) filterClause).getFieldName();
                    String storageFieldName = storageNames.get(fieldName);
                    Object value = ((EqualityFilterClause) filterClause).getValue();

                    if (value instanceof String) {
                        return String.format("%s eq '%s'", storageFieldName, value);
                    } else if (value instanceof Boolean) {
                        return String.format("%s eq %s", storageFieldName,
                            value.toString().toLowerCase());
                    } else if (value instanceof Integer) {
                        return String.format("%s eq %d", storageFieldName, (Integer) value);
                    } else if (value instanceof Long) {
                        return String.format("%s eq %d", storageFieldName, (Long) value);
                    } else if (value instanceof Float) {
                        return String.format("%s eq %f", storageFieldName, (Float) value);
                    } else if (value instanceof Double) {
                        return String.format("%s eq %f", storageFieldName, (Double) value);
                    } else if (value instanceof OffsetDateTime) {
                        return String.format("%s eq %s", storageFieldName, ((OffsetDateTime) value)
                            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
                    } else if (value == null) {
                        return String.format("%s eq null", storageFieldName);
                    } else {
                        throw new SKException("Unsupported filter value type '"
                            + value.getClass().getSimpleName() + "'.");
                    }
                }).collect(Collectors.toList());

            List<String> tagListContainsStrings = basicVectorSearchFilter.getFilterClauses()
                .stream()
                .filter(filterClause -> filterClause instanceof TagListContainsFilterClause)
                .map(filterClause -> {
                    String fieldName = ((TagListContainsFilterClause) filterClause).getFieldName();
                    String storageFieldName = storageNames.get(fieldName);

                    TagListContainsFilterClause tagListClause = (TagListContainsFilterClause) filterClause;
                    return String.format("%s/any(t: t eq '%s')", storageFieldName,
                        tagListClause.getValue());
                }).collect(Collectors.toList());

            filterStrings.addAll(tagListContainsStrings);
            filter = String.join(" and ", filterStrings);
        }

        return filter;
    }
}
