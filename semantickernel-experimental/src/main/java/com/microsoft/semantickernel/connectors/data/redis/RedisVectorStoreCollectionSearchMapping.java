// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.data.redis;

import com.microsoft.semantickernel.connectors.data.redis.filter.RedisEqualToFilterClause;
import com.microsoft.semantickernel.data.vectorsearch.VectorSearchFilter;
import com.microsoft.semantickernel.data.vectorsearch.queries.VectorizedSearchQuery;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordDataField;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordDefinition;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordVectorField;
import com.microsoft.semantickernel.data.vectorstorage.options.VectorSearchOptions;
import com.microsoft.semantickernel.exceptions.SKException;
import redis.clients.jedis.search.Query;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.stream.Collectors;

public class RedisVectorStoreCollectionSearchMapping {
    static final String VECTOR_SCORE_FIELD = "vector_score";

    public static Query buildQuery(VectorizedSearchQuery query,
        VectorStoreRecordDefinition recordDefinition) {
        VectorSearchOptions options = query.getSearchOptions();

        VectorStoreRecordVectorField firstVectorField = recordDefinition.getVectorFields()
            .get(0);
        if (options == null) {
            options = VectorSearchOptions.createDefault(firstVectorField.getName());
        }

        VectorStoreRecordVectorField vectorField = options.getVectorFieldName() == null
            ? firstVectorField
            : (VectorStoreRecordVectorField) recordDefinition
                .getField(options.getVectorFieldName());

        String filter = buildFilter(options.getVectorSearchFilter(), recordDefinition);

        String knn = String.format("%s=>[KNN $K @%s $BLOB AS %s]", filter,
            vectorField.getEffectiveStorageName(), VECTOR_SCORE_FIELD);
        Query redisQuery = new Query(knn)
            .addParam("K", options.getLimit() + options.getOffset())
            .addParam("BLOB", convertListToByteArray(query.getVector()))
            .limit(options.getOffset(), options.getLimit())
            .setSortBy(VECTOR_SCORE_FIELD, true)
            .dialect(2);

        if (options.isIncludeVectors()) {
            redisQuery.returnFields(recordDefinition.getDataFields().stream()
                .map(VectorStoreRecordDataField::getEffectiveStorageName)
                .toArray(String[]::new));
        }

        return redisQuery;
    }

    public static byte[] convertListToByteArray(List<Float> embeddings) {
        ByteBuffer bytes = ByteBuffer.allocate(Float.BYTES * embeddings.size());
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        embeddings.iterator().forEachRemaining(bytes::putFloat);
        return bytes.array();
    }

    public static String buildFilter(VectorSearchFilter vectorSearchFilter,
        VectorStoreRecordDefinition recordDefinition) {
        if (vectorSearchFilter == null
            || vectorSearchFilter.getFilterClauses().isEmpty()) {
            return "*";
        }

        return String.format("(%s)",
            vectorSearchFilter.getFilterClauses().stream().map(filterClause -> {
                if (filterClause instanceof RedisEqualToFilterClause) {
                    RedisEqualToFilterClause equalToFilterClause = (RedisEqualToFilterClause) filterClause;
                    return new RedisEqualToFilterClause(
                        recordDefinition.getField(equalToFilterClause.getFieldName())
                            .getEffectiveStorageName(),
                        equalToFilterClause.getValue()).getFilter();
                } else {
                    throw new SKException("Unsupported filter clause type '"
                        + filterClause.getClass().getSimpleName() + "'.");
                }
            }).collect(Collectors.joining(" ")));
    }
}
