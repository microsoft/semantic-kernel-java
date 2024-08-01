// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.data.jdbc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.semantickernel.data.recorddefinition.VectorStoreRecordDefinition;
import com.microsoft.semantickernel.data.recorddefinition.VectorStoreRecordField;
import com.microsoft.semantickernel.data.recorddefinition.VectorStoreRecordKeyField;
import com.microsoft.semantickernel.data.recorddefinition.VectorStoreRecordVectorField;
import com.microsoft.semantickernel.data.recordoptions.UpsertRecordOptions;
import com.microsoft.semantickernel.exceptions.SKException;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class MySQLVectorStoreQueryProvider extends
    JDBCVectorStoreDefaultQueryProvider implements JDBCVectorStoreQueryProvider {

    public MySQLVectorStoreQueryProvider(Connection connection, String collectionsTable,
        String prefixForCollectionTables) {
        super(connection, collectionsTable, prefixForCollectionTables);
    }

    /**
     * Creates a new builder.
     * @return the builder
     */
    public static Builder builder() {
        return new Builder();
    }

    private void setStatementValues(PreparedStatement statement, Object record,
        List<VectorStoreRecordField> fields) {
        for (int i = 0; i < fields.size(); ++i) {
            VectorStoreRecordField field = fields.get(i);
            try {
                Field recordField = record.getClass().getDeclaredField(field.getName());
                recordField.setAccessible(true);
                Object value = recordField.get(record);

                if (field instanceof VectorStoreRecordKeyField) {
                    statement.setObject(i + 1, (String) value);
                } else if (field instanceof VectorStoreRecordVectorField) {
                    Class<?> vectorType = record.getClass().getDeclaredField(field.getName())
                        .getType();

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
    }

    @Override
    public void upsertRecords(String collectionName, List<?> records,
        VectorStoreRecordDefinition recordDefinition, UpsertRecordOptions options) {
        validateSQLidentifier(getCollectionTableName(collectionName));

        List<VectorStoreRecordField> fields = recordDefinition.getAllFields();

        StringBuilder onDuplicateKeyUpdate = new StringBuilder();
        for (int i = 0; i < fields.size(); ++i) {
            VectorStoreRecordField field = fields.get(i);
            if (i > 0) {
                onDuplicateKeyUpdate.append(", ");
            }

            onDuplicateKeyUpdate.append(field.getName()).append(" = VALUES(")
                .append(field.getName()).append(")");
        }

        String query = "INSERT INTO " + getCollectionTableName(collectionName)
            + " (" + getQueryColumnsFromFields(fields) + ")"
            + " VALUES (" + getWildcardString(fields.size()) + ")"
            + " ON DUPLICATE KEY UPDATE " + onDuplicateKeyUpdate;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            for (Object record : records) {
                setStatementValues(statement, record, recordDefinition.getAllFields());
                statement.addBatch();
            }

            statement.executeBatch();
        } catch (SQLException e) {
            throw new SKException("Failed to upsert records", e);
        }
    }

    public static class Builder
        extends JDBCVectorStoreDefaultQueryProvider.Builder {
        public MySQLVectorStoreQueryProvider build() {
            if (connection == null) {
                throw new IllegalArgumentException("connection is required");
            }

            return new MySQLVectorStoreQueryProvider(connection, collectionsTable,
                prefixForCollectionTables);
        }
    }
}
