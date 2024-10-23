// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.semantickernel.data.filter.EqualToFilterClause;
import com.microsoft.semantickernel.data.filter.FilterClause;
import com.microsoft.semantickernel.data.vectorsearch.VectorSearchFilter;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordDefinition;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordField;
import com.microsoft.semantickernel.exceptions.SKException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides methods to filter records based on a {@link VectorSearchFilter}.
 */
public class VolatileVectorStoreCollectionSearchMapping {

    /**
     * Filters the records based on the given {@link VectorSearchFilter}.
     *
     * @param records The records to filter.
     * @param filter The filter to apply.
     * @param recordDefinition The record definition.
     * @param objectMapper The object mapper.
     * @param <Record> The record type.
     * @return The filtered records.
     */
    public static <Record> List<Record> filterRecords(List<Record> records,
        VectorSearchFilter filter,
        VectorStoreRecordDefinition recordDefinition, ObjectMapper objectMapper) {
        if (filter == null || filter.getFilterClauses().isEmpty()) {
            return records;
        }

        return records.stream().filter(
            record -> {
                JsonNode recordNode = objectMapper.valueToTree(record);

                for (FilterClause filterClause : filter.getFilterClauses()) {
                    if (filterClause instanceof EqualToFilterClause) {
                        EqualToFilterClause equalToFilterClause = (EqualToFilterClause) filterClause;
                        VectorStoreRecordField field = recordDefinition
                            .getField(equalToFilterClause.getFieldName());

                        Object value = objectMapper.convertValue(
                            recordNode.get(field.getEffectiveStorageName()), field.getFieldType());
                        if (!equalToFilterClause.getValue().equals(value)) {
                            return false;
                        }
                    } else {
                        throw new SKException(String.format("Unsupported filter clause type '%s'.",
                            filterClause.getClass().getSimpleName()));
                    }
                }
                return true;
            }).collect(Collectors.toList());
    }
}
