package com.microsoft.semantickernel.connectors.memory.redis;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.semantickernel.builders.SemanticKernelBuilder;
import com.microsoft.semantickernel.memory.VectorStoreRecordMapper;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;

import java.util.AbstractMap;
import java.util.Map.Entry;
import java.util.function.Function;

public class RedisVectorStoreRecordMapper<Record> extends VectorStoreRecordMapper<Record, Entry<String, Object>> {
    private RedisVectorStoreRecordMapper(
            Function<Record, Entry<String, Object>> toStorageModelMapper,
            Function<Entry<String, Object>, Record> toRecordMapper) {
        super(toStorageModelMapper, toRecordMapper);
    }

    /**
     * Creates a new builder.
     *
     * @param <Record> the record type
     * @return the builder
     */
    public static class Builder<Record> implements SemanticKernelBuilder<RedisVectorStoreRecordMapper<Record>> {
        private String keyField;
        private Class<Record> recordClass;

        /**
         * Sets the key field name in the record.
         *
         * @param keyField the key field
         * @return the builder
         */
        public Builder<Record> keyField(String keyField) {
            this.keyField = keyField;
            return this;
        }

        /**
         * Sets the record class.
         *
         * @param recordClass the record class
         * @return the builder
         */
        public Builder<Record> recordClass(Class<Record> recordClass) {
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
            if (keyField == null) {
                throw new IllegalArgumentException("keyField is required");
            }
            if (recordClass == null) {
                throw new IllegalArgumentException("recordClass is required");
            }

            return new RedisVectorStoreRecordMapper<>(record -> {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    String json = mapper.writeValueAsString(record);
                    ObjectNode jsonNode = (ObjectNode) mapper.readTree(json);
                    String key = jsonNode.get(keyField).asText();
                    jsonNode.remove(keyField);

                    return new AbstractMap.SimpleEntry<>(key, jsonNode);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }, storageModel -> {
                ObjectMapper mapper = new ObjectMapper();
                mapper.setVisibility(VisibilityChecker.Std.defaultInstance().withFieldVisibility(JsonAutoDetect.Visibility.ANY));

                ObjectNode jsonNode = mapper.valueToTree(storageModel.getValue());

                // Add the key back to the record
                jsonNode.put(keyField, storageModel.getKey());
                return mapper.convertValue(jsonNode, recordClass);
            });
        }
    }
}
