// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.jdbc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.semantickernel.builders.SemanticKernelBuilder;
import com.microsoft.semantickernel.data.vectorstorage.VectorStoreRecordMapper;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordDataField;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordDefinition;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordField;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordVectorField;
import com.microsoft.semantickernel.data.vectorstorage.options.GetRecordOptions;
import com.microsoft.semantickernel.exceptions.SKException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.List;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.BiFunction;

/**
 * Maps a JDBC result set to a record.
 *
 * @param <Record> the record type
 */
public class JDBCVectorStoreRecordMapper<Record>
    extends VectorStoreRecordMapper<Record, ResultSet> {

    /**
     * Constructs a new instance of the VectorStoreRecordMapper.
     *
     * @param storageModelToRecordMapper the function to convert a storage model to a record
     */
    protected JDBCVectorStoreRecordMapper(
        BiFunction<ResultSet, GetRecordOptions, Record> storageModelToRecordMapper) {
        super(null, storageModelToRecordMapper);
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
     * Operation not supported.
     */
    @Override
    public ResultSet mapRecordToStorageModel(Record record) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Builder for {@link JDBCVectorStoreRecordMapper}.
     *
     * @param <Record> the record type
     */
    public static class Builder<Record>
        implements SemanticKernelBuilder<JDBCVectorStoreRecordMapper<Record>> {
        private Class<Record> recordClass;
        private VectorStoreRecordDefinition vectorStoreRecordDefinition;
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
         * @param vectorStoreRecordDefinition the vector store record definition
         * @return the builder
         */
        public Builder<Record> withVectorStoreRecordDefinition(
            VectorStoreRecordDefinition vectorStoreRecordDefinition) {
            this.vectorStoreRecordDefinition = vectorStoreRecordDefinition;
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
         * Builds the {@link JDBCVectorStoreRecordMapper}.
         *
         * @return the {@link JDBCVectorStoreRecordMapper}
         */
        public JDBCVectorStoreRecordMapper<Record> build() {
            if (recordClass == null) {
                throw new SKException("recordClass is required");
            }
            if (vectorStoreRecordDefinition == null) {
                throw new SKException("vectorStoreRecordDefinition is required");
            }

            return new JDBCVectorStoreRecordMapper<>(
                (resultSet, options) -> {
                    try {
                        // Create an ObjectNode to hold the values
                        ObjectNode objectNode = objectMapper.createObjectNode();

                        // Select fields from the record definition.
                        List<VectorStoreRecordField> fields;
                        if (options != null && options.isIncludeVectors()) {
                            fields = vectorStoreRecordDefinition.getAllFields();
                        } else {
                            fields = vectorStoreRecordDefinition.getNonVectorFields();
                        }

                        for (VectorStoreRecordField field : fields) {
                            Object value = resultSet.getObject(field.getEffectiveStorageName());
                            Class<?> fieldType = field.getFieldType();

                            if (field instanceof VectorStoreRecordVectorField) {
                                // If the vector field is other than String, deserialize it from the JSON string
                                if (!fieldType.equals(String.class)) {
                                    value = objectMapper.readValue((String) value, fieldType);
                                }
                            } else if (field instanceof VectorStoreRecordDataField) {
                                // If the field is List, deserialize it from the JSON string
                                if (fieldType.equals(List.class)) {
                                    value = objectMapper.readValue((String) value, fieldType);
                                }
                            }

                            JsonNode genericNode = objectMapper.valueToTree(value);
                            objectNode.set(field.getEffectiveStorageName(), genericNode);
                        }

                        // Deserialize the object node to the record class
                        return objectMapper.convertValue(objectNode, recordClass);
                    } catch (SQLException | JsonProcessingException e) {
                        throw new SKException(
                            "Failure to serialize object, by default the JDBC connector uses Jackson, ensure your model object can be serialized by Jackson, i.e the class is visible, has getters, constructor, annotations etc.",
                            e);
                    }
                });
        }
    }
}
