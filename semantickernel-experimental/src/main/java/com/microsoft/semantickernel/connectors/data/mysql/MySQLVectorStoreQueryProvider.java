// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.data.mysql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.semantickernel.connectors.data.jdbc.JDBCVectorStoreDefaultQueryProvider;
import com.microsoft.semantickernel.connectors.data.jdbc.JDBCVectorStoreQueryProvider;
import com.microsoft.semantickernel.data.recorddefinition.VectorStoreRecordDefinition;
import com.microsoft.semantickernel.data.recorddefinition.VectorStoreRecordField;
import com.microsoft.semantickernel.data.recorddefinition.VectorStoreRecordKeyField;
import com.microsoft.semantickernel.data.recorddefinition.VectorStoreRecordVectorField;
import com.microsoft.semantickernel.data.recordoptions.UpsertRecordOptions;
import com.microsoft.semantickernel.exceptions.SKException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class MySQLVectorStoreQueryProvider extends
    JDBCVectorStoreDefaultQueryProvider implements JDBCVectorStoreQueryProvider {

    private final DataSource dataSource;

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    private MySQLVectorStoreQueryProvider(DataSource dataSource, String collectionsTable,
        String prefixForCollectionTables) {
        super(dataSource, collectionsTable, prefixForCollectionTables);
        this.dataSource = dataSource;
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

    /**
     * Upserts records into the collection.
     * @param collectionName the collection name
     * @param records the records to upsert
     * @param recordDefinition the record definition
     * @param options the upsert options
     * @throws SKException if the upsert fails
     */
    @Override
    @SuppressFBWarnings("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING") // SQL query is generated dynamically with valid identifiers
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

        try (Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(query)) {
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
        private DataSource dataSource;
        private String collectionsTable = DEFAULT_COLLECTIONS_TABLE;
        private String prefixForCollectionTables = DEFAULT_PREFIX_FOR_COLLECTION_TABLES;

        @SuppressFBWarnings("EI_EXPOSE_REP2")
        public Builder withDataSource(DataSource dataSource) {
            this.dataSource = dataSource;
            return this;
        }

        /**
         * Sets the collections table name.
         * @param collectionsTable the collections table name
         * @return the builder
         */
        public Builder withCollectionsTable(String collectionsTable) {
            this.collectionsTable = validateSQLidentifier(collectionsTable);
            return this;
        }

        /**
         * Sets the prefix for collection tables.
         * @param prefixForCollectionTables the prefix for collection tables
         * @return the builder
         */
        public Builder withPrefixForCollectionTables(String prefixForCollectionTables) {
            this.prefixForCollectionTables = validateSQLidentifier(prefixForCollectionTables);
            return this;
        }

        public MySQLVectorStoreQueryProvider build() {
            if (dataSource == null) {
                throw new SKException("DataSource is required");
            }

            return new MySQLVectorStoreQueryProvider(dataSource, collectionsTable,
                prefixForCollectionTables);
        }
    }
}
