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
import com.microsoft.semantickernel.memory.recordoptions.DeleteRecordOptions;
import com.microsoft.semantickernel.memory.recordoptions.GetRecordOptions;
import com.microsoft.semantickernel.memory.recordoptions.UpsertRecordOptions;
import com.microsoft.semantickernel.services.textembedding.Embedding;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class JDBCVectorStoreDefaultQueryProvider<Record>
    implements JDBCVectorStoreQueryProvider<Record> {
    protected final String storageTableName;
    protected final VectorStoreRecordDefinition recordDefinition;
    protected final Class<Record> recordClass;
    protected final BiFunction<String, String, String> sanitizeKeyFunction;

    public JDBCVectorStoreDefaultQueryProvider(String storageTableName,
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
    public String formatGetQuery(int numberOfKeys, @Nullable GetRecordOptions options) {
        List<VectorStoreRecordField> fields;
        if (options == null || options.includeVectors()) {
            fields = recordDefinition.getAllFields();
        } else {
            fields = recordDefinition.getNonVectorFields();
        }

        return "SELECT " + getQueryColumnsFromFields(fields)
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
    public String formatDeleteQuery(int numberOfKeys, @Nullable DeleteRecordOptions options) {
        return "DELETE FROM " + storageTableName
            + " WHERE " + recordDefinition.getKeyField().getName()
            + " IN (" + getWildcardString(numberOfKeys) + ")";
    }

    /**
     * Builds an upsert query.
     *
     * @return the upsert query
     */
    @Override
    public String formatUpsertQuery(@Nullable UpsertRecordOptions options) {
        throw new UnsupportedOperationException(
            "Upsert is not supported. Try with a specific query provider.");
    }

    /**
     * Configures the values for a get query generated by {@link #formatGetQuery(int, GetRecordOptions)}.
     *
     * @param statement the prepared statement
     * @param keys the keys to get records for
     * @param collectionName the name of the collection
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
     * Configures the values for a delete query generated by {@link #formatDeleteQuery(int, DeleteRecordOptions)}.
     *
     * @param statement the prepared statement
     * @param keys the keys to delete records for
     * @param collectionName the name of the collection
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
     * Configures the values for an upsert query generated by {@link #formatUpsertQuery(UpsertRecordOptions)}.
     *
     * @param statement the prepared statement
     * @param data the record to upsert
     * @param collectionName the name of the collection
     * @return the key of the record
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
                    Class<?> vectorType = recordClass.getDeclaredField(field.getName()).getType();

                    // If the vector field is other than String, serialize it to JSON
                    if (vectorType.equals(String.class)) {
                        statement.setObject(i + 1, value);
                    } else {
                        // Serialize the vector to JSON
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
        implements JDBCVectorStoreQueryProvider.Builder<Record> {

        protected String storageTableName;
        protected VectorStoreRecordDefinition recordDefinition;
        protected Class<Record> recordClass;
        protected BiFunction<String, String, String> sanitizeKeyFunction;

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
        public JDBCVectorStoreDefaultQueryProvider<Record> build() {
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

            return new JDBCVectorStoreDefaultQueryProvider<>(
                storageTableName,
                recordDefinition,
                recordClass,
                sanitizeKeyFunction);
        }
    }
}
