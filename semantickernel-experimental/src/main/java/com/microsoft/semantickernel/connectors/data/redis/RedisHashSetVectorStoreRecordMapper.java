// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.data.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.semantickernel.builders.SemanticKernelBuilder;
import com.microsoft.semantickernel.data.vectorstorage.VectorStoreRecordMapper;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordDataField;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordDefinition;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordField;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordKeyField;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordVectorField;
import com.microsoft.semantickernel.data.vectorstorage.options.GetRecordOptions;
import com.microsoft.semantickernel.exceptions.SKException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Function;

public class RedisHashSetVectorStoreRecordMapper<Record>
    extends VectorStoreRecordMapper<Record, Entry<String, Map<byte[], byte[]>>> {

    private RedisHashSetVectorStoreRecordMapper(
        Function<Record, Entry<String, Map<byte[], byte[]>>> toStorageModelMapper,
        BiFunction<Entry<String, Map<byte[], byte[]>>, GetRecordOptions, Record> toRecordMapper) {
        super(toStorageModelMapper, toRecordMapper);
    }

    /**
     * Creates a new builder.
     *
     * @param <Record> the record type
     * @return the builder
     */
    public static <Record> Builder<Record> builder() {
        return new Builder<>();
    }

    /**
     * Creates a new builder.
     *
     * @param <Record> the record type
     */
    public static class Builder<Record>
        implements SemanticKernelBuilder<RedisHashSetVectorStoreRecordMapper<Record>> {
        @Nullable
        private Class<Record> recordClass;
        @Nullable
        private VectorStoreRecordDefinition recordDefinition;
        @Nullable
        private ObjectMapper objectMapper = new ObjectMapper();

        /**
         * Sets the record class.
         *
         * @param recordClass the record class
         * @return the builder
         */
        public Builder<Record> withRecordClass(Class<Record> recordClass) {
            this.recordClass = recordClass;
            return this;
        }

        /**
         * Sets the vector store record definition.
         *
         * @param recordDefinition the vector store record definition
         * @return the builder
         */
        public Builder<Record> withVectorStoreRecordDefinition(
            VectorStoreRecordDefinition recordDefinition) {
            this.recordDefinition = recordDefinition;
            return this;
        }

        /**
         * Sets the object mapper.
         *
         * @param objectMapper the object mapper
         * @return the builder
         */
        @SuppressFBWarnings("EI_EXPOSE_REP2")
        public Builder<Record> withObjectMapper(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
            return this;
        }

        /**
         * Builds the {@link RedisHashSetVectorStoreRecordMapper}.
         *
         * @return the {@link RedisHashSetVectorStoreRecordMapper}
         */
        @Override
        public RedisHashSetVectorStoreRecordMapper<Record> build() {
            if (recordClass == null) {
                throw new SKException("recordClass is required");
            }
            if (recordDefinition == null) {
                throw new SKException("vectorStoreRecordDefinition is required");
            }

            return new RedisHashSetVectorStoreRecordMapper<>(record -> {
                try {
                    ObjectNode jsonNode = objectMapper.valueToTree(record);

                    String key = jsonNode
                        .get(recordDefinition.getKeyField().getEffectiveStorageName()).asText();
                    jsonNode.remove(recordDefinition.getKeyField().getEffectiveStorageName());

                    Map<byte[], byte[]> storage = new HashMap<>();
                    for (VectorStoreRecordDataField field : recordDefinition.getDataFields()) {
                        JsonNode value = jsonNode.get(field.getEffectiveStorageName());
                        if (value != null) {
                            storage.put(
                                field.getEffectiveStorageName().getBytes(StandardCharsets.UTF_8),
                                objectMapper.writeValueAsBytes(value));
                        }
                    }
                    for (VectorStoreRecordVectorField field : recordDefinition.getVectorFields()) {
                        ArrayNode value = (ArrayNode) jsonNode.get(field.getEffectiveStorageName());
                        List<Float> vector = objectMapper.convertValue(value, List.class);
                        if (value != null) {
                            storage.put(
                                field.getEffectiveStorageName().getBytes(StandardCharsets.UTF_8),
                                RedisVectorStoreCollectionSearchMapping
                                    .convertListToByteArray(vector));
                        }
                    }

                    return new AbstractMap.SimpleEntry<>(key, storage);
                } catch (Exception e) {
                    throw new SKException(
                        "Failure to serialize object, by default the Redis connector uses Jackson, ensure your model object can be serialized by Jackson, i.e the class is visible, has getters, constructor, annotations etc.",
                        e);
                }
            }, (storageModel, options) -> {
                try {
                    // Empty map means no record found
                    if (storageModel.getValue() == null || storageModel.getValue().isEmpty()) {
                        return null;
                    }

                    ObjectNode jsonNode = objectMapper.createObjectNode();
                    jsonNode.set(recordDefinition.getKeyField().getEffectiveStorageName(),
                        objectMapper.valueToTree(storageModel.getKey()));

                    // byte[] as key is not useful, convert to String
                    Map<String, byte[]> storage = new HashMap<>();
                    storageModel.getValue()
                        .forEach((k, v) -> storage.put(new String(k, StandardCharsets.UTF_8), v));

                    for (VectorStoreRecordDataField field : recordDefinition.getDataFields()) {
                        byte[] value = storage.get(field.getEffectiveStorageName());
                        if (value != null) {
                            jsonNode.set(field.getEffectiveStorageName(),
                                objectMapper.valueToTree(
                                    objectMapper.readValue(value, field.getFieldType())));
                        }
                    }
                    if (options != null && options.isIncludeVectors()) {
                        for (VectorStoreRecordVectorField field : recordDefinition
                            .getVectorFields()) {
                            byte[] value = storage.get(field.getEffectiveStorageName());
                            if (value != null) {
                                jsonNode.set(field.getEffectiveStorageName(),
                                    objectMapper.valueToTree(RedisVectorStoreCollectionSearchMapping
                                        .convertByteArrayToList(value)));
                            }
                        }
                    }

                    return objectMapper.convertValue(jsonNode, recordClass);
                } catch (JsonProcessingException e) {
                    throw new SKException(
                        "Failure to deserialize object, by default the Redis connector uses Jackson, ensure your model object can be serialized by Jackson, i.e the class is visible, has getters, constructor, annotations etc.",
                        e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
}
