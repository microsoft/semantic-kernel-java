// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.data.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.semantickernel.builders.SemanticKernelBuilder;
import com.microsoft.semantickernel.data.VectorStoreRecordMapper;
import com.microsoft.semantickernel.data.recorddefinition.VectorStoreRecordDataField;
import com.microsoft.semantickernel.data.recorddefinition.VectorStoreRecordDefinition;
import com.microsoft.semantickernel.data.recorddefinition.VectorStoreRecordVectorField;
import com.microsoft.semantickernel.exceptions.SKException;

import javax.annotation.Nullable;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

public class RedisHashSetVectorStoreRecordMapper<Record>
    extends VectorStoreRecordMapper<Record, Entry<String, Map<String, String>>> {

    private RedisHashSetVectorStoreRecordMapper(
        Function<Record, Entry<String, Map<String, String>>> toStorageModelMapper,
        Function<Entry<String, Map<String, String>>, Record> toRecordMapper) {
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

            ObjectMapper mapper = new ObjectMapper();

            return new RedisHashSetVectorStoreRecordMapper<>(record -> {
                try {
                    ObjectNode jsonNode = mapper.valueToTree(record);
                    String key = jsonNode
                        .get(recordDefinition.getKeyField().getEffectiveStorageName()).asText();
                    jsonNode.remove(recordDefinition.getKeyField().getEffectiveStorageName());

                    Map<String, String> resultMap = new HashMap<>();
                    Iterator<Entry<String, JsonNode>> fields = jsonNode.fields();
                    while (fields.hasNext()) {
                        Map.Entry<String, JsonNode> field = fields.next();
                        if (field.getValue().isTextual()) {
                            resultMap.put(field.getKey(), field.getValue().asText());
                        } else {
                            resultMap.put(field.getKey(),
                                mapper.valueToTree(field.getValue()).toString());
                        }
                    }

                    return new AbstractMap.SimpleEntry<>(key, resultMap);
                } catch (Exception e) {
                    throw new SKException(
                        "Failure to serialize object, by default the Redis connector uses Jackson, ensure your model object can be serialized by Jackson, i.e the class is visible, has getters, constructor, annotations etc.",
                        e);
                }
            }, storageModel -> {
                try {
                    // Empty map means no record found
                    if (storageModel.getValue() == null || storageModel.getValue().isEmpty()) {
                        return null;
                    }

                    ObjectNode jsonNode = mapper.createObjectNode();
                    jsonNode.set(recordDefinition.getKeyField().getEffectiveStorageName(),
                        mapper.valueToTree(storageModel.getKey()));

                    for (VectorStoreRecordDataField field : recordDefinition.getDataFields()) {
                        jsonNode.put(field.getEffectiveStorageName(),
                            storageModel.getValue().get(field.getEffectiveStorageName()));
                    }

                    for (VectorStoreRecordVectorField field : recordDefinition.getVectorFields()) {
                        String value = storageModel.getValue().get(field.getEffectiveStorageName());

                        // If vector fields were not requested, skip
                        if (value == null) {
                            continue;
                        }

                        Class<?> valueType = field.getFieldType();

                        if (valueType.equals(String.class)) {
                            jsonNode.put(field.getEffectiveStorageName(), value);
                        } else {
                            // Convert the String stored in Redis back to the correct type and then put the JSON node
                            jsonNode.set(field.getEffectiveStorageName(),
                                mapper.valueToTree(mapper.readValue(value, valueType)));
                        }
                    }

                    return mapper.convertValue(jsonNode, recordClass);
                } catch (JsonProcessingException e) {
                    throw new SKException(
                        "Failure to deserialize object, by default the Redis connector uses Jackson, ensure your model object can be serialized by Jackson, i.e the class is visible, has getters, constructor, annotations etc.",
                        e);
                }
            });
        }
    }
}
