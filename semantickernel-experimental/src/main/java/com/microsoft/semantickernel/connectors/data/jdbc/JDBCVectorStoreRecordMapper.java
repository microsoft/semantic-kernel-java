// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.data.jdbc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.semantickernel.builders.SemanticKernelBuilder;
import com.microsoft.semantickernel.data.VectorStoreRecordMapper;
import com.microsoft.semantickernel.data.recorddefinition.VectorStoreRecordDefinition;
import com.microsoft.semantickernel.data.recorddefinition.VectorStoreRecordField;
import com.microsoft.semantickernel.data.recorddefinition.VectorStoreRecordVectorField;
import com.microsoft.semantickernel.exceptions.SKException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.sql.ResultSetMetaData;
import java.util.List;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Function;

public class JDBCVectorStoreRecordMapper<Record>
    extends VectorStoreRecordMapper<Record, ResultSet> {

    /**
     * Constructs a new instance of the VectorStoreRecordMapper.
     *
     * @param storageModelToRecordMapper the function to convert a storage model to a record
     */
    protected JDBCVectorStoreRecordMapper(Function<ResultSet, Record> storageModelToRecordMapper) {
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
                throw new IllegalArgumentException("recordClass is required");
            }
            if (vectorStoreRecordDefinition == null) {
                throw new IllegalArgumentException("vectorStoreRecordDefinition is required");
            }

            return new JDBCVectorStoreRecordMapper<>(
                resultSet -> {
                    try {
                        // Create an ObjectNode to hold the values
                        ObjectNode objectNode = objectMapper.createObjectNode();

                        // Select fields from the record definition.
                        List<VectorStoreRecordField> fields;
                        ResultSetMetaData metaData = resultSet.getMetaData();
                        if (metaData.getColumnCount() == vectorStoreRecordDefinition.getAllFields()
                            .size()) {
                            fields = vectorStoreRecordDefinition.getAllFields();
                        } else {
                            fields = vectorStoreRecordDefinition.getNonVectorFields();
                        }

                        for (VectorStoreRecordField field : fields) {
                            Object value = resultSet.getObject(field.getEffectiveStorageName());

                            if (field instanceof VectorStoreRecordVectorField) {
                                Class<?> vectorType = field.getFieldType();

                                // If the vector field is other than String, deserialize it from the JSON string
                                if (!vectorType.equals(String.class)) {
                                    value = objectMapper.readValue((String) value, vectorType);
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
