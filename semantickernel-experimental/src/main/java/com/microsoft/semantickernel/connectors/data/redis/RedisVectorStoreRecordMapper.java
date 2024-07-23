// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.data.redis;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.semantickernel.builders.SemanticKernelBuilder;
import com.microsoft.semantickernel.data.VectorStoreRecordMapper;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;

import java.util.AbstractMap;
import java.util.Map.Entry;
import java.util.function.Function;

public class RedisVectorStoreRecordMapper<Record>
    extends VectorStoreRecordMapper<Record, Entry<String, Object>> {
    private RedisVectorStoreRecordMapper(
        Function<Record, Entry<String, Object>> toStorageModelMapper,
        Function<Entry<String, Object>, Record> toRecordMapper) {
        super(toStorageModelMapper, toRecordMapper);
    }

    public static <Record> Builder<Record> builder() {
        return null;
    }

    /**
     * Creates a new builder.
     *
     * @param <Record> the record type
     */
    public static class Builder<Record>
        implements SemanticKernelBuilder<RedisVectorStoreRecordMapper<Record>> {
        private String keyFieldName;
        private Class<Record> recordClass;

        /**
         * Sets the key field name in the record.
         *
         * @param keyFieldName the key field
         * @return the builder
         */
        public Builder<Record> withKeyFieldName(String keyFieldName) {
            this.keyFieldName = keyFieldName;
            return this;
        }

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
         * Builds the {@link RedisVectorStoreRecordMapper}.
         *
         * @return the {@link RedisVectorStoreRecordMapper}
         */
        @Override
        public RedisVectorStoreRecordMapper<Record> build() {
            if (keyFieldName == null) {
                throw new IllegalArgumentException("keyFieldName is required");
            }
            if (recordClass == null) {
                throw new IllegalArgumentException("recordClass is required");
            }

            return new RedisVectorStoreRecordMapper<>(record -> {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    String json = mapper.writeValueAsString(record);
                    ObjectNode jsonNode = (ObjectNode) mapper.readTree(json);
                    String key = jsonNode.get(keyFieldName).asText();
                    jsonNode.remove(keyFieldName);

                    return new AbstractMap.SimpleEntry<>(key, jsonNode);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }, storageModel -> {
                ObjectMapper mapper = new ObjectMapper();
                mapper.setVisibility(VisibilityChecker.Std.defaultInstance()
                    .withFieldVisibility(JsonAutoDetect.Visibility.ANY));

                ObjectNode jsonNode = mapper.valueToTree(storageModel.getValue());

                // Add the key back to the record
                jsonNode.put(keyFieldName, storageModel.getKey());
                return mapper.convertValue(jsonNode, recordClass);
            });
        }
    }
}
