// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.data.redis;

import com.microsoft.semantickernel.data.filter.AnyTagEqualToFilterClause;
import com.microsoft.semantickernel.data.filter.EqualToFilterClause;
import com.microsoft.semantickernel.data.vectorsearch.VectorSearchFilter;
import com.microsoft.semantickernel.data.filter.FilterMapping;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordDataField;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordDefinition;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordVectorField;
import com.microsoft.semantickernel.data.vectorstorage.options.VectorSearchOptions;
import com.microsoft.semantickernel.exceptions.SKException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import redis.clients.jedis.args.SortingOrder;
import redis.clients.jedis.search.FTSearchParams;

/**
 * A mapping for searching a collection of vector records in Redis.
 */
public class RedisVectorStoreCollectionSearchMapping implements FilterMapping {

    static final String VECTOR_SCORE_FIELD = "vector_score";

    private RedisVectorStoreCollectionSearchMapping() {
    }

    static class RedisVectorStoreCollectionSearchMappingHolder {
        static final RedisVectorStoreCollectionSearchMapping INSTANCE = new RedisVectorStoreCollectionSearchMapping();
    }

    static RedisVectorStoreCollectionSearchMapping getInstance() {
        return RedisVectorStoreCollectionSearchMappingHolder.INSTANCE;
    }

    /**
     * Builds a query for searching a collection of vector records in Redis.
     * @param vector the vector to search for
     * @param options the search options
     * @param recordDefinition the record definition
     * @param storageType the storage type
     * @return the query and search parameters
     */
    public Pair<String, FTSearchParams> buildQuery(List<Float> vector,
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

        String filter = getFilter(options.getVectorSearchFilter(), recordDefinition);

        String knn = String.format("%s=>[KNN $K @%s $BLOB AS %s]", filter,
            vectorField.getEffectiveStorageName(), VECTOR_SCORE_FIELD);

        FTSearchParams searchParams = new FTSearchParams()
            .addParam("K", options.getTop() + options.getSkip())
            .addParam("BLOB", convertListToByteArray(vector))
            .limit(options.getSkip(), options.getTop())
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

    /**
     * Converts a list of floats to a byte array.
     * @param embeddings the embeddings
     * @return the byte array
     */
    public static byte[] convertListToByteArray(List<Float> embeddings) {
        ByteBuffer bytes = ByteBuffer.allocate(Float.BYTES * embeddings.size());
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        embeddings.iterator().forEachRemaining(bytes::putFloat);
        return bytes.array();
    }

    /**
     * Converts a byte array to a list of floats.
     * @param bytes the byte array
     * @return the list of floats
     */
    public static List<Float> convertByteArrayToList(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        List<Float> embeddings = new java.util.ArrayList<>();
        while (buffer.hasRemaining()) {
            embeddings.add(buffer.getFloat());
        }
        return embeddings;
    }

    /**
     * Gets the filter string for the given vector search filter and record definition.
     *
     * @param filter           The filter to get the filter string for.
     * @param recordDefinition The record definition to get the filter string for.
     * @return The filter string.
     */
    @Override
    public String getFilter(VectorSearchFilter filter,
        VectorStoreRecordDefinition recordDefinition) {
        if (filter == null
            || filter.getFilterClauses().isEmpty()) {
            return "*";
        }

        return String.format("(%s)",
            filter.getFilterClauses().stream().map(filterClause -> {
                if (filterClause instanceof EqualToFilterClause) {
                    EqualToFilterClause equalToFilterClause = (EqualToFilterClause) filterClause;
                    return getEqualToFilter(new EqualToFilterClause(
                        recordDefinition.getField(equalToFilterClause.getFieldName())
                            .getEffectiveStorageName(),
                        equalToFilterClause.getValue()));
                } else {
                    throw new SKException("Unsupported filter clause type '"
                        + filterClause.getClass().getSimpleName() + "'.");
                }
            }).collect(Collectors.joining(" ")));
    }

    /**
     * Gets the filter string for the given equal to filter clause.
     *
     * @param filterClause The equal to filter clause to get the filter string for.
     * @return The filter string.
     */
    @Override
    public String getEqualToFilter(EqualToFilterClause filterClause) {
        String fieldName = filterClause.getFieldName();
        Object value = filterClause.getValue();
        String formattedValue;

        if (value instanceof String) {
            formattedValue = String.format("\"%s\"", value);
        } else if (value instanceof Number) {
            formattedValue = String.format("[%s %s]", value, value);
        } else {
            throw new SKException("Unsupported filter value type '"
                + value.getClass().getSimpleName() + "'.");
        }

        return String.format("@%s:%s", fieldName, formattedValue);
    }

    /**
     * Gets the filter string for the given any tag equal to filter clause.
     *
     * @param filterClause The any tag equal to filter clause to get the filter string for.
     * @return The filter string.
     */
    @Override
    public String getAnyTagEqualToFilter(AnyTagEqualToFilterClause filterClause) {
        return String.format("@%s:\"%s\"", filterClause.getFieldName(), filterClause.getValue());
    }
}
