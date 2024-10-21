// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.data.redis;

import com.microsoft.semantickernel.data.vectorstorage.definition.DistanceFunction;
import com.microsoft.semantickernel.data.vectorstorage.definition.IndexKind;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordDataField;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordField;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordKeyField;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordVectorField;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.microsoft.semantickernel.exceptions.SKException;
import redis.clients.jedis.search.Schema;

/**
 * Maps a vector store record collection to a Redis schema.
 */
public class RedisVectorStoreCollectionCreateMapping {
    private static final HashSet<Class<?>> supportedFilterableNumericTypes = new HashSet<>(
        Arrays.asList(
            Integer.class,
            int.class,
            Double.class,
            double.class,
            Long.class,
            long.class,
            Float.class,
            float.class,
            Short.class,
            short.class,
            Byte.class,
            byte.class));

    private static String getAlgorithmMetric(
        VectorStoreRecordVectorField vectorField) {
        if (vectorField.getDistanceFunction() == DistanceFunction.UNDEFINED) {
            return RedisVectorDistanceMetric.COSINE;
        }

        switch (vectorField.getDistanceFunction()) {
            case COSINE_DISTANCE:
                return RedisVectorDistanceMetric.COSINE;
            case DOT_PRODUCT:
                return RedisVectorDistanceMetric.DOT_PRODUCT;
            case EUCLIDEAN_DISTANCE:
                return RedisVectorDistanceMetric.EUCLIDEAN;
            default:
                throw new SKException(
                    "Unsupported distance function: " + vectorField.getDistanceFunction());
        }
    }

    private static Schema.VectorField.VectorAlgo getAlgorithmConfig(
        VectorStoreRecordVectorField vectorField) {
        if (vectorField.getIndexKind() == IndexKind.UNDEFINED) {
            return Schema.VectorField.VectorAlgo.HNSW;
        }

        switch (vectorField.getIndexKind()) {
            case HNSW:
                return Schema.VectorField.VectorAlgo.HNSW;
            case FLAT:
                return Schema.VectorField.VectorAlgo.FLAT;
            default:
                throw new SKException(
                    "Unsupported index kind: " + vectorField.getIndexKind());
        }
    }

    private static String getRedisPath(String name, boolean withRedisJsonRoot) {
        return withRedisJsonRoot ? "$." + name : name;
    }

    /**
     * Maps a vector store record collection to a Redis schema.
     *
     * @param fields the fields
     * @param storageType the Redis storage type
     * @return the schema
     */
    public static Schema mapToSchema(List<VectorStoreRecordField> fields,
        RedisStorageType storageType) {
        Schema schema = new Schema();
        boolean withRedisJsonRoot = storageType == RedisStorageType.JSON;

        for (VectorStoreRecordField field : fields) {
            if (field instanceof VectorStoreRecordKeyField) {
                continue;
            }

            if (field instanceof VectorStoreRecordDataField
                && ((VectorStoreRecordDataField) field).isFilterable()) {
                VectorStoreRecordDataField dataField = (VectorStoreRecordDataField) field;

                if (dataField.getFieldType() == null) {
                    throw new SKException(
                        "Field type is required for filterable fields: "
                            + dataField.getEffectiveStorageName());
                }

                if (dataField.getFieldType().equals(String.class)) {
                    schema.addTextField(
                        getRedisPath(dataField.getEffectiveStorageName(), withRedisJsonRoot), 1.0)
                        .as(dataField.getEffectiveStorageName());
                } else if (supportedFilterableNumericTypes.contains(dataField.getFieldType())) {
                    schema
                        .addNumericField(
                            getRedisPath(dataField.getEffectiveStorageName(), withRedisJsonRoot))
                        .as(dataField.getEffectiveStorageName());
                } else {
                    throw new SKException(
                        "Unsupported field type for numeric filterable fields: "
                            + dataField.getEffectiveStorageName());
                }

            }

            if (field instanceof VectorStoreRecordVectorField) {
                VectorStoreRecordVectorField vectorField = (VectorStoreRecordVectorField) field;

                if (vectorField.getDimensions() < 1) {
                    throw new SKException(
                        "Dimensions must be greater than 0 for vector fields: "
                            + vectorField.getEffectiveStorageName());
                }

                Schema.VectorField.VectorAlgo algorithm = getAlgorithmConfig(vectorField);
                String metric = getAlgorithmMetric(vectorField);

                Map<String, Object> attributes = new HashMap<>();
                attributes.put(RedisIndexSchemaParams.TYPE, "FLOAT32");
                attributes.put(RedisIndexSchemaParams.DIMENSIONS, vectorField.getDimensions());
                attributes.put(RedisIndexSchemaParams.DISTANCE_METRIC, metric);

                schema.addVectorField(
                    getRedisPath(vectorField.getEffectiveStorageName(), withRedisJsonRoot),
                    algorithm, attributes).as(vectorField.getEffectiveStorageName());
            }
        }

        return schema;
    }

    static class RedisIndexSchemaParams {
        public static final String TYPE = "TYPE";
        public static final String DIMENSIONS = "DIM";
        public static final String DISTANCE_METRIC = "DISTANCE_METRIC";
    }

    static class RedisVectorDistanceMetric {
        public static final String EUCLIDEAN = "L2";
        public static final String DOT_PRODUCT = "IP";
        public static final String COSINE = "COSINE"; // Cosine distance
    }

}
