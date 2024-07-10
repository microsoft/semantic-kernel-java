// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.memory.jdbc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.semantickernel.builders.SemanticKernelBuilder;
import com.microsoft.semantickernel.exceptions.SKException;
import com.microsoft.semantickernel.memory.recorddefinition.VectorStoreRecordDefinition;
import com.microsoft.semantickernel.memory.recorddefinition.VectorStoreRecordField;
import com.microsoft.semantickernel.memory.recorddefinition.VectorStoreRecordKeyField;
import com.microsoft.semantickernel.memory.recorddefinition.VectorStoreRecordVectorField;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class JDBCVectorStoreDefaultQueryHandler<Record>
    implements JDBCVectorStoreQueryHandler<Record> {
    private final String storageTableName;
    private final VectorStoreRecordDefinition recordDefinition;
    private final Class<Record> recordClass;
    private final BiFunction<String, String, String> sanitizeKeyFunction;

    public JDBCVectorStoreDefaultQueryHandler(String storageTableName,
        VectorStoreRecordDefinition recordDefinition,
        Class<Record> recordClass, BiFunction<String, String, String> sanitizeKeyFunction) {
        this.storageTableName = storageTableName;
        this.recordDefinition = recordDefinition;
        this.recordClass = recordClass;
        this.sanitizeKeyFunction = sanitizeKeyFunction;
    }

    /**
     * Gets the storage table name.
     */
    public String getStorageTableName() {
        return storageTableName;
    }

    /**
     * Gets the record definition.
     */
    public VectorStoreRecordDefinition getRecordDefinition() {
        return recordDefinition;
    }

    /**
     * Gets the record class.
     */
    public Class<Record> getRecordClass() {
        return recordClass;
    }

    /**
     * Gets the sanitize key function.
     */
    public BiFunction<String, String, String> getSanitizeKeyFunction() {
        return sanitizeKeyFunction;
    }

    /**
     * Creates a new builder.
     * @param <Record> the record type
     * @return the builder
     */
    public static <Record> Builder<Record> builder() {
        return new Builder<>();
    }

    /**
     * Formats a wildcard string for a query.
     * @param wildcards the number of wildcards
     * @return the formatted wildcard string
     */
    protected String getWildcardString(int wildcards) {
        StringBuilder wildcardString = new StringBuilder();
        for (int i = 0; i < wildcards; ++i) {
            wildcardString.append("?");
            if (i < wildcards - 1) {
                wildcardString.append(", ");
            }
        }
        return wildcardString.toString();
    }

    /**
     * Formats the query columns from a record definition.
     * @param fields the fields to get the columns from
     * @return the formatted query columns
     */
    protected String getQueryColumnsFromFields(List<VectorStoreRecordField> fields) {
        return fields.stream().map(VectorStoreRecordField::getName)
            .collect(Collectors.joining(", "));
    }

    /**
     * Formats a get query.
     * @param numberOfKeys the number of keys
     * @return the formatted get query
     */
    @Override
    public String formatGetQuery(int numberOfKeys) {
        return "SELECT " + getQueryColumnsFromFields(recordDefinition.getAllFields())
            + " FROM " + storageTableName
            + " WHERE " + recordDefinition.getKeyField().getName()
            + " IN (" + getWildcardString(numberOfKeys) + ")";
    }

    /**
     * Formats a delete query.
     * @param numberOfKeys the number of keys
     * @return the formatted delete query
     */
    @Override
    public String formatDeleteQuery(int numberOfKeys) {
        return "DELETE FROM " + storageTableName
            + " WHERE " + recordDefinition.getKeyField().getName()
            + " IN (" + getWildcardString(numberOfKeys) + ")";
    }

    /**
     * Builds an upsert query.
     * @return the upsert query
     */
    @Override
    public String formatUpsertQuery() {
        List<VectorStoreRecordField> fields = recordDefinition.getAllFields();

        StringBuilder onDuplicateKeyUpdate = new StringBuilder();
        for (int i = 0; i < fields.size(); ++i) {
            onDuplicateKeyUpdate.append(fields.get(i).getName())
                .append(" = VALUES(")
                .append(fields.get(i).getName())
                .append(")");

            if (i < fields.size() - 1) {
                onDuplicateKeyUpdate.append(", ");
            }
        }

        return "INSERT INTO " + storageTableName
            + " (" + getQueryColumnsFromFields(fields) + ")"
            + " VALUES (" + getWildcardString(fields.size()) + ")"
            + " ON DUPLICATE KEY UPDATE " + onDuplicateKeyUpdate;
    }

    /**
     * Sets the values of a prepared statement before a get operation.
     *
     * @param statement The prepared statement.
     * @param keys      The keys to set the values for.
     * @param collectionName The collection name.
     */
    @Override
    public void configureStatementGetQuery(PreparedStatement statement, List<String> keys,
        String collectionName) {
        for (int i = 0; i < keys.size(); ++i) {
            try {
                statement.setObject(i + 1, sanitizeKeyFunction.apply(keys.get(i), collectionName));
            } catch (SQLException e) {
                throw new SKException("Failed to set statement values", e);
            }
        }
    }

    /**
     * Sets the values of a prepared statement before a delete operation.
     *
     * @param statement The prepared statement.
     * @param keys      The keys to set the values for.
     * @param collectionName The collection name.
     */
    @Override
    public void configureStatementDeleteQuery(PreparedStatement statement, List<String> keys,
        String collectionName) {
        for (int i = 0; i < keys.size(); ++i) {
            try {
                statement.setObject(i + 1, sanitizeKeyFunction.apply(keys.get(i), collectionName));
            } catch (SQLException e) {
                throw new SKException("Failed to set statement values", e);
            }
        }
    }

    /**
     * Sets the values of a prepared statement before an upsert operation.
     *
     * @param statement The prepared statement.
     * @param data      The record to set the values for.
     * @param collectionName The collection name.
     * @return The key of the record.
     */
    @Override
    public String configureStatementUpsertQuery(PreparedStatement statement, Record data,
        String collectionName) {
        List<VectorStoreRecordField> allFields = recordDefinition.getAllFields();
        String key = null;
        for (int i = 0; i < allFields.size(); ++i) {
            VectorStoreRecordField field = allFields.get(i);
            try {
                Field recordField = data.getClass().getDeclaredField(field.getName());
                recordField.setAccessible(true);
                Object value = recordField.get(data);

                if (field instanceof VectorStoreRecordKeyField) {
                    key = (String) value;

                    // Sanitize the key
                    statement.setObject(i + 1, sanitizeKeyFunction.apply(key, collectionName));
                } else if (field instanceof VectorStoreRecordVectorField) {

                    // If the vector field is other than String, serialize it to JSON
                    if (recordClass.getDeclaredField(field.getName()).getType()
                        .equals(String.class)) {
                        statement.setObject(i + 1, value);
                    } else {
                        statement.setObject(i + 1, new ObjectMapper().writeValueAsString(value));
                    }

                } else {
                    statement.setObject(i + 1, value);
                }
            } catch (NoSuchFieldException | IllegalAccessException | SQLException e) {
                throw new SKException("Failed to set statement values", e);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        return key;
    }

    public static class Builder<Record>
        implements SemanticKernelBuilder<JDBCVectorStoreDefaultQueryHandler<Record>> {

        private String storageTableName;
        private VectorStoreRecordDefinition recordDefinition;
        private Class<Record> recordClass;
        private BiFunction<String, String, String> sanitizeKeyFunction;

        /**
         * Sets the storage table name.
         * @param storageTableName the storage table name
         * @return the builder
         */
        public Builder<Record> withStorageTableName(String storageTableName) {
            this.storageTableName = storageTableName;
            return this;
        }

        /**
         * Sets the record definition.
         * @param recordDefinition the record definition
         * @return the builder
         */
        public Builder<Record> withRecordDefinition(VectorStoreRecordDefinition recordDefinition) {
            this.recordDefinition = recordDefinition;
            return this;
        }

        /**
         * Sets the record class.
         * @param recordClass the record class
         * @return the builder
         */
        public Builder<Record> withRecordClass(Class<Record> recordClass) {
            this.recordClass = recordClass;
            return this;
        }

        /**
         * Sets the sanitize key function.
         * @param sanitizeKeyFunction the sanitize key function
         * @return the builder
         */
        public Builder<Record> withSanitizeKeyFunction(
            BiFunction<String, String, String> sanitizeKeyFunction) {
            this.sanitizeKeyFunction = sanitizeKeyFunction;
            return this;
        }

        @Override
        public JDBCVectorStoreDefaultQueryHandler<Record> build() {
            if (storageTableName == null) {
                throw new IllegalArgumentException("storageTableName is required");
            }
            if (recordDefinition == null) {
                throw new IllegalArgumentException("recordDefinition is required");
            }
            if (recordClass == null) {
                throw new IllegalArgumentException("recordClass is required");
            }
            if (sanitizeKeyFunction == null) {
                throw new IllegalArgumentException("sanitizeKeyFunction is required");
            }

            return new JDBCVectorStoreDefaultQueryHandler<>(
                storageTableName,
                recordDefinition,
                recordClass,
                sanitizeKeyFunction);
        }
    }
}
