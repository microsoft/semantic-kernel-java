// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.data.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.semantickernel.builders.SemanticKernelBuilder;
import com.microsoft.semantickernel.data.vectorstorage.VectorStoreRecordMapper;
import com.microsoft.semantickernel.data.vectorstorage.options.GetRecordOptions;
import com.microsoft.semantickernel.exceptions.SKException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.AbstractMap;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Function;
import javax.annotation.Nullable;

public class RedisJsonVectorStoreRecordMapper<Record>
    extends VectorStoreRecordMapper<Record, Entry<String, Object>> {

    private RedisJsonVectorStoreRecordMapper(
        Function<Record, Entry<String, Object>> toStorageModelMapper,
        BiFunction<Entry<String, Object>, GetRecordOptions, Record> toRecordMapper) {
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
        implements SemanticKernelBuilder<RedisJsonVectorStoreRecordMapper<Record>> {
        @Nullable
        private String keyFieldName;
        @Nullable
        private Class<Record> recordClass;
        private ObjectMapper objectMapper = new ObjectMapper();

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
         * Builds the {@link RedisJsonVectorStoreRecordMapper}.
         *
         * @return the {@link RedisJsonVectorStoreRecordMapper}
         */
        @Override
        public RedisJsonVectorStoreRecordMapper<Record> build() {
            if (keyFieldName == null) {
                throw new SKException("keyFieldName is required");
            }
            if (recordClass == null) {
                throw new SKException("recordClass is required");
            }

            return new RedisJsonVectorStoreRecordMapper<>(record -> {
                try {
                    ObjectNode jsonNode = objectMapper.valueToTree(record);
                    String key = jsonNode.get(keyFieldName).asText();
                    jsonNode.remove(keyFieldName);

                    return new AbstractMap.SimpleEntry<>(key, jsonNode);
                } catch (Exception e) {
                    throw new SKException(
                        "Failure to serialize object, by default the Redis connector uses Jackson, ensure your model object can be serialized by Jackson, i.e the class is visible, has getters, constructor, annotations etc.",
                        e);
                }
            }, (storageModel, options) -> {
                try {
                    ObjectNode jsonNode = objectMapper.valueToTree(storageModel.getValue());
                    // Add the key back to the record
                    jsonNode.put(keyFieldName, storageModel.getKey());
                    return objectMapper.convertValue(jsonNode, recordClass);
                } catch (Exception e) {
                    throw new SKException(
                        "Failure to deserialize object, by default the Redis connector uses Jackson, ensure your model object can be serialized by Jackson, i.e the class is visible, has getters, constructor, annotations etc.",
                        e);
                }
            });
        }
    }
}
