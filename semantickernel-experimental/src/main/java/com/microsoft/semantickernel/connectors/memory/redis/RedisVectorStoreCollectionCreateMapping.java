// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.memory.redis;

import com.azure.search.documents.indexes.models.ExhaustiveKnnAlgorithmConfiguration;
import com.azure.search.documents.indexes.models.ExhaustiveKnnParameters;
import com.azure.search.documents.indexes.models.HnswAlgorithmConfiguration;
import com.azure.search.documents.indexes.models.HnswParameters;
import com.azure.search.documents.indexes.models.VectorSearchAlgorithmConfiguration;
import com.azure.search.documents.indexes.models.VectorSearchAlgorithmMetric;
import com.microsoft.semantickernel.data.recorddefinition.IndexKind;
import com.microsoft.semantickernel.data.recorddefinition.VectorStoreRecordDataField;
import com.microsoft.semantickernel.data.recorddefinition.VectorStoreRecordField;
import com.microsoft.semantickernel.data.recorddefinition.VectorStoreRecordKeyField;
import com.microsoft.semantickernel.data.recorddefinition.VectorStoreRecordVectorField;
import redis.clients.jedis.json.Path2;
import redis.clients.jedis.search.Schema;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

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
        if (vectorField.getDistanceFunction() == null) {
            return RedisVectorDistanceMetric.COSINE;
        }

        switch (vectorField.getDistanceFunction()) {
            case COSINE_SIMILARITY:
                return RedisVectorDistanceMetric.COSINE;
            case DOT_PRODUCT:
                return RedisVectorDistanceMetric.DOT_PRODUCT;
            case EUCLIDEAN:
                return RedisVectorDistanceMetric.EUCLIDEAN;
            default:
                throw new IllegalArgumentException(
                    "Unsupported distance function: " + vectorField.getDistanceFunction());
        }
    }

    private static Schema.VectorField.VectorAlgo getAlgorithmConfig(
        VectorStoreRecordVectorField vectorField) {
        if (vectorField.getIndexKind() == null) {
            return Schema.VectorField.VectorAlgo.HNSW;
        }

        switch (vectorField.getIndexKind()) {
            case HNSW:
                return Schema.VectorField.VectorAlgo.HNSW;
            case FLAT:
                return Schema.VectorField.VectorAlgo.FLAT;
            default:
                throw new IllegalArgumentException(
                    "Unsupported index kind: " + vectorField.getIndexKind());
        }
    }

    private static String getRedisPath(String name) {
        return "$." + name;
    }

    public static Schema mapToSchema(List<VectorStoreRecordField> fields) {
        Schema schema = new Schema();

        for (VectorStoreRecordField field : fields) {
            if (field instanceof VectorStoreRecordKeyField) {
                continue;
            }

            if (field instanceof VectorStoreRecordDataField
                && ((VectorStoreRecordDataField) field).isFilterable()) {
                VectorStoreRecordDataField dataField = (VectorStoreRecordDataField) field;

                if (dataField.getFieldType() == null) {
                    throw new IllegalArgumentException(
                        "Field type is required for filterable fields: " + dataField.getName());
                }

                if (dataField.getFieldType().equals(String.class)) {
                    schema.addTextField(getRedisPath(dataField.getName()), 1.0);
                } else if (supportedFilterableNumericTypes.contains(dataField.getFieldType())) {
                    schema.addNumericField(getRedisPath(dataField.getName()));
                } else {
                    throw new IllegalArgumentException(
                        "Unsupported field type for numeric filterable fields: "
                            + dataField.getName());
                }

            }

            if (field instanceof VectorStoreRecordVectorField) {
                VectorStoreRecordVectorField vectorField = (VectorStoreRecordVectorField) field;

                if (vectorField.getDimensions() < 1) {
                    throw new IllegalArgumentException(
                        "Dimensions must be greater than 0 for vector fields: "
                            + vectorField.getName());
                }

                Schema.VectorField.VectorAlgo algorithm = getAlgorithmConfig(vectorField);
                String metric = getAlgorithmMetric(vectorField);

                Map<String, Object> attributes = new HashMap<>();
                attributes.put(RedisIndexSchemaParams.TYPE, "FLOAT32");
                attributes.put(RedisIndexSchemaParams.DIMENSIONS, vectorField.getDimensions());
                attributes.put(RedisIndexSchemaParams.DISTANCE_METRIC, metric);

                schema.addVectorField(getRedisPath(vectorField.getName()), algorithm, attributes);
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
        public static final String COSINE = "COSINE";
    }

}
