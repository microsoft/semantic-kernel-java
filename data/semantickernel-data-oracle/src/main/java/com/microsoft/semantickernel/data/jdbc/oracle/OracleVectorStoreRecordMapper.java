// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.jdbc.oracle;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.semantickernel.builders.SemanticKernelBuilder;
import com.microsoft.semantickernel.data.vectorstorage.VectorStoreRecordMapper;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordDefinition;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordField;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordVectorField;
import com.microsoft.semantickernel.data.vectorstorage.options.GetRecordOptions;
import com.microsoft.semantickernel.exceptions.SKException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import oracle.jdbc.OracleResultSet;
import oracle.jdbc.provider.oson.OsonModule;
import oracle.sql.TIMESTAMPTZ;
import java.nio.ByteBuffer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;

/**
 * Maps a Oracle result set to a record.
 *
 * @param <Record> the record type
 */
public class OracleVectorStoreRecordMapper<Record>
    extends VectorStoreRecordMapper<Record, ResultSet> {

    /**
     * Constructs a new instance of the VectorStoreRecordMapper.
     *
     * @param storageModelToRecordMapper the function to convert a storage model to a record
     */
    protected OracleVectorStoreRecordMapper(
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
     * Builder for {@link OracleVectorStoreRecordMapper}.
     *
     * @param <Record> the record type
     */
    public static class Builder<Record>
        implements SemanticKernelBuilder<OracleVectorStoreRecordMapper<Record>> {
        private Class<Record> recordClass;
        private VectorStoreRecordDefinition vectorStoreRecordDefinition;
        private Map<Class<?>, String> supportedDataTypesMapping;
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
         * Sets the Map of supported data types and their database representation
         *
         * @param supportedDataTypesMapping the Map of supported data types and their
         *                                  database representation
         * @return the builder
         */
        public Builder<Record> withSupportedDataTypesMapping(
            Map<Class<?>, String> supportedDataTypesMapping) {
            this.supportedDataTypesMapping = supportedDataTypesMapping;
            return this;
        }

        /**
         * Builds the {@link OracleVectorStoreRecordMapper}.
         *
         * @return the {@link OracleVectorStoreRecordMapper}
         */
        public OracleVectorStoreRecordMapper<Record> build() {
            if (recordClass == null) {
                throw new SKException("recordClass is required");
            }
            if (vectorStoreRecordDefinition == null) {
                throw new SKException("vectorStoreRecordDefinition is required");
            }

            return new OracleVectorStoreRecordMapper<>(
                (resultSet, options) -> {
                    try {
                        objectMapper.registerModule(new OsonModule());
                        // Create an ObjectNode to hold the values
                        ObjectNode objectNode = objectMapper.createObjectNode();

                        // Read non vector fields
                        for (VectorStoreRecordField field : vectorStoreRecordDefinition.getNonVectorFields()) {
                            Class<?> fieldType = field.getFieldType();

                            Object value;
                            switch (supportedDataTypesMapping.get(fieldType)) {
                                case OracleDataTypesMapping.STRING_CLOB:
                                    value = resultSet.getString(field.getEffectiveStorageName());
                                    break;
                                case OracleDataTypesMapping.BYTE:
                                    value = resultSet.getByte(field.getEffectiveStorageName());
                                    break;
                                case OracleDataTypesMapping.SHORT:
                                    value = resultSet.getShort(field.getEffectiveStorageName());
                                    break;
                                case OracleDataTypesMapping.INTEGER:
                                    value = resultSet.getInt(field.getEffectiveStorageName());
                                    break;
                                case OracleDataTypesMapping.LONG:
                                    value = resultSet.getLong(field.getEffectiveStorageName());
                                    break;
                                case OracleDataTypesMapping.FLOAT:
                                    value = resultSet.getFloat(field.getEffectiveStorageName());
                                    break;
                                case OracleDataTypesMapping.DOUBLE:
                                    value = resultSet.getDouble(field.getEffectiveStorageName());
                                    break;
                                case OracleDataTypesMapping.DECIMAL:
                                    value = resultSet.getBigDecimal(field.getEffectiveStorageName());
                                    break;
                                case OracleDataTypesMapping.BOOLEAN:
                                    value = resultSet.getBoolean(field.getEffectiveStorageName());
                                    break;
                                case OracleDataTypesMapping.OFFSET_DATE_TIME:
                                    TIMESTAMPTZ timestamptz = ((OracleResultSet)resultSet).getTIMESTAMPTZ(field.getEffectiveStorageName());
                                    value = timestamptz != null ? timestamptz.offsetDateTimeValue() : null;
                                    break;
                                case OracleDataTypesMapping.BYTE_ARRAY:
                                    value = resultSet.getBytes(field.getEffectiveStorageName());
                                    break;
                                case OracleDataTypesMapping.UUID:
                                    byte[] bytes = resultSet.getBytes(field.getEffectiveStorageName());
                                    if (bytes != null) {
                                        ByteBuffer bb = ByteBuffer.wrap(bytes);
                                        long firstLong = bb.getLong();
                                        long secondLong = bb.getLong();
                                        value = new UUID(firstLong, secondLong);
                                    } else {
                                        value = null;
                                    }
                                    break;
                                case OracleDataTypesMapping.JSON:
                                    value = resultSet.getObject(field.getEffectiveStorageName(), fieldType);
                                    break;
                                default:
                                    value = resultSet.getString(field.getEffectiveStorageName());
                            }
                            // Result set getter method sometimes returns a default value when NULL,
                            // set value to null in that case.
                            if (resultSet.wasNull()) {
                                value = null;
                            }

                            JsonNode genericNode = objectMapper.valueToTree(value);

                            objectNode.set(field.getEffectiveStorageName(), genericNode);
                        }
                        if (options != null && options.isIncludeVectors()) {
                            for (VectorStoreRecordVectorField field : vectorStoreRecordDefinition.getVectorFields()) {
                                Object value = resultSet.getObject(field.getEffectiveStorageName(), float[].class);
                                JsonNode genericNode = objectMapper.valueToTree(value);
                                objectNode.set(field.getEffectiveStorageName(), genericNode);
                            }
                        } else {
                            for (VectorStoreRecordVectorField field : vectorStoreRecordDefinition.getVectorFields()) {
                                JsonNode genericNode = objectMapper.valueToTree(null);
                                objectNode.set(field.getEffectiveStorageName(), genericNode);
                            }
                        }

                        // Deserialize the object node to the record class
                        return objectMapper.convertValue(objectNode, recordClass);
                    } catch (SQLException e) {
                        throw new SKException(
                            "Failure to serialize object, by default the JDBC connector uses Jackson, ensure your model object can be serialized by Jackson, i.e the class is visible, has getters, constructor, annotations etc.",
                            e);
                    }
                });
        }
    }
}
