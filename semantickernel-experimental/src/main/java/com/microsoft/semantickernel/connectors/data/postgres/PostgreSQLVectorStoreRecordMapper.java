// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.data.postgres;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.semantickernel.builders.SemanticKernelBuilder;
import com.microsoft.semantickernel.data.vectorstorage.VectorStoreRecordMapper;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordDefinition;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordField;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordVectorField;
import com.microsoft.semantickernel.exceptions.SKException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.postgresql.util.PGobject;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Function;

public class PostgreSQLVectorStoreRecordMapper<Record>
    extends VectorStoreRecordMapper<Record, ResultSet> {

    /**
     * Constructs a new instance of the VectorStoreRecordMapper.
     *
     * @param storageModelToRecordMapper the function to convert a storage model to a record
     */
    protected PostgreSQLVectorStoreRecordMapper(
        Function<ResultSet, Record> storageModelToRecordMapper) {
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

    public static class Builder<Record>
        implements SemanticKernelBuilder<PostgreSQLVectorStoreRecordMapper<Record>> {
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
         * Builds the {@link PostgreSQLVectorStoreRecordMapper}.
         *
         * @return the {@link PostgreSQLVectorStoreRecordMapper}
         */
        public PostgreSQLVectorStoreRecordMapper<Record> build() {
            if (recordClass == null) {
                throw new SKException("recordClass is required");
            }
            if (vectorStoreRecordDefinition == null) {
                throw new SKException("vectorStoreRecordDefinition is required");
            }

            return new PostgreSQLVectorStoreRecordMapper<>(
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
                                    // Deserialize the pgvector string to the vector type
                                    value = objectMapper.readValue(((PGobject) value).getValue(),
                                        vectorType);
                                }
                            }

                            JsonNode genericNode = objectMapper.valueToTree(value);
                            objectNode.set(field.getEffectiveStorageName(), genericNode);
                        }

                        return objectMapper.treeToValue(objectNode, recordClass);
                    } catch (SQLException | JsonProcessingException e) {
                        throw new SKException(
                            "Failure to serialize object, by default the JDBC connector uses Jackson, ensure your model object can be serialized by Jackson, i.e the class is visible, has getters, constructor, annotations etc.",
                            e);
                    }
                });
        }
    }
}
