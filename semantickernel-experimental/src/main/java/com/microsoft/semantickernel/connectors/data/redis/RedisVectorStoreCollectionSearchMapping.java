// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.data.redis;

import com.microsoft.semantickernel.connectors.data.redis.filter.RedisEqualToFilterClause;
import com.microsoft.semantickernel.data.vectorsearch.VectorSearchFilter;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordDataField;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordDefinition;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordVectorField;
import com.microsoft.semantickernel.data.vectorstorage.options.VectorSearchOptions;
import com.microsoft.semantickernel.exceptions.SKException;
import org.apache.commons.lang3.tuple.Pair;
import redis.clients.jedis.args.SortingOrder;
import redis.clients.jedis.search.FTSearchParams;
import redis.clients.jedis.search.Query;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.stream.Collectors;

public class RedisVectorStoreCollectionSearchMapping {
    static final String VECTOR_SCORE_FIELD = "vector_score";

    public static Pair<String, FTSearchParams> buildQuery(List<Float> vector,
        VectorSearchOptions options,
        VectorStoreRecordDefinition recordDefinition,
        RedisStorageType storageType) {
        VectorStoreRecordVectorField firstVectorField = recordDefinition.getVectorFields().get(0);
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

        FTSearchParams searchParams = new FTSearchParams()
            .addParam("K", options.getLimit() + options.getOffset())
            .addParam("BLOB", convertListToByteArray(vector))
            .limit(options.getOffset(), options.getLimit())
            .sortBy(VECTOR_SCORE_FIELD, SortingOrder.ASC)
            .dialect(2);

        // For hash set storage is possible to select what fields to return without them being filterable
        if (storageType == RedisStorageType.HASH_SET) {
            // We also need to tell Redis to return the fields without decoding them
            // Vector fields specially need to be returned as raw bytes
            for (VectorStoreRecordDataField dataField : recordDefinition.getDataFields()) {
                searchParams.returnField(dataField.getEffectiveStorageName(), false);
            }
            if (options.isIncludeVectors()) {
                for (VectorStoreRecordVectorField v : recordDefinition.getVectorFields()) {
                    searchParams.returnField(v.getEffectiveStorageName(), false);
                }
            }

            // Also, return the score field, this can be decoded.
            searchParams.returnField(VECTOR_SCORE_FIELD, true);
        }

        return Pair.of(knn, searchParams);
    }

    public static byte[] convertListToByteArray(List<Float> embeddings) {
        ByteBuffer bytes = ByteBuffer.allocate(Float.BYTES * embeddings.size());
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        embeddings.iterator().forEachRemaining(bytes::putFloat);
        return bytes.array();
    }

    public static List<Float> convertByteArrayToList(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        List<Float> embeddings = new java.util.ArrayList<>();
        while (buffer.hasRemaining()) {
            embeddings.add(buffer.getFloat());
        }
        return embeddings;
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
