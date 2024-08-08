// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.data.postgres;

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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class PostgreSQLVectorStoreQueryProvider extends
    JDBCVectorStoreDefaultQueryProvider implements JDBCVectorStoreQueryProvider {
    private final DataSource dataSource;
    private final String collectionsTable;
    private final String prefixForCollectionTables;

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    private PostgreSQLVectorStoreQueryProvider(DataSource dataSource, String collectionsTable,
        String prefixForCollectionTables) {
        super(dataSource, collectionsTable, prefixForCollectionTables);
        this.dataSource = dataSource;
        this.collectionsTable = collectionsTable;
        this.prefixForCollectionTables = prefixForCollectionTables;

        supportedKeyTypes = new HashMap<>();
        supportedKeyTypes.put(String.class, "VARCHAR(255)");

        supportedDataTypes = new HashMap<>();
        supportedDataTypes.put(String.class, "TEXT");
        supportedDataTypes.put(Integer.class, "INTEGER");
        supportedDataTypes.put(int.class, "INTEGER");
        supportedDataTypes.put(Long.class, "BIGINT");
        supportedDataTypes.put(long.class, "BIGINT");
        supportedDataTypes.put(Float.class, "REAL");
        supportedDataTypes.put(float.class, "REAL");
        supportedDataTypes.put(Double.class, "DOUBLE PRECISION");
        supportedDataTypes.put(double.class, "DOUBLE PRECISION");
        supportedDataTypes.put(Boolean.class, "BOOLEAN");
        supportedDataTypes.put(boolean.class, "BOOLEAN");
        supportedDataTypes.put(OffsetDateTime.class, "TIMESTAMPTZ");

        supportedVectorTypes = new HashMap<>();
        supportedDataTypes.put(String.class, "TEXT");
        supportedVectorTypes.put(List.class, "VECTOR(%d)");
        supportedVectorTypes.put(Collection.class, "VECTOR(%d)");
    }

    /**
     * Creates a new builder.
     * @return the builder
     */
    public static PostgreSQLVectorStoreQueryProvider.Builder builder() {
        return new PostgreSQLVectorStoreQueryProvider.Builder();
    }

    /**
     * Prepares the vector store.
     * Executes any necessary setup steps for the vector store.
     *
     * @throws SKException if an error occurs while preparing the vector store
     */
    @Override
    public void prepareVectorStore() {
        super.prepareVectorStore();

        // Create the vector extension
        String pgVector = "CREATE EXTENSION IF NOT EXISTS vector";

        try (Connection connection = dataSource.getConnection();
            PreparedStatement createPgVector = connection.prepareStatement(pgVector)) {
            createPgVector.execute();
        } catch (SQLException e) {
            throw new SKException("Failed to prepare vector store", e);
        }
    }

    private String getColumnNamesAndTypesForVectorFields(List<VectorStoreRecordVectorField> fields,
        Class<?> recordClass) {
        StringBuilder columnNames = new StringBuilder();
        for (VectorStoreRecordVectorField field : fields) {
            try {
                Field declaredField = recordClass.getDeclaredField(field.getName());
                if (columnNames.length() > 0) {
                    columnNames.append(", ");
                }

                if (declaredField.getType().equals(String.class)) {
                    columnNames.append(field.getName())
                        .append(supportedVectorTypes.get(String.class));
                } else {
                    // Get the vector type and dimensions
                    String type = String.format(supportedVectorTypes.get(declaredField.getType()),
                        field.getDimensions());
                    columnNames.append(field.getName()).append(" ").append(type);
                }
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }

        return columnNames.toString();
    }

    /**
     * Creates a collection.
     *
     * @param collectionName the collection name
     * @param recordClass the record class
     * @param recordDefinition the record definition
     * @throws SKException if an error occurs while creating the collection
     */
    @Override
    @SuppressFBWarnings("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING") // SQL query is generated dynamically with valid identifiers
    public void createCollection(String collectionName, Class<?> recordClass,
        VectorStoreRecordDefinition recordDefinition) {
        Field keyDeclaredField = recordDefinition.getKeyDeclaredField(recordClass);
        List<Field> dataDeclaredFields = recordDefinition.getDataDeclaredFields(recordClass);

        String createStorageTable = "CREATE TABLE IF NOT EXISTS "
            + getCollectionTableName(collectionName)
            + " (" + keyDeclaredField.getName() + " VARCHAR(255) PRIMARY KEY, "
            + getColumnNamesAndTypes(dataDeclaredFields, supportedDataTypes) + ", "
            + getColumnNamesAndTypesForVectorFields(recordDefinition.getVectorFields(), recordClass)
            + ");";

        String insertCollectionQuery = "INSERT INTO " + validateSQLidentifier(collectionsTable)
            + " (collectionId) VALUES (?)";

        try (Connection connection = dataSource.getConnection();
            PreparedStatement createTable = connection.prepareStatement(createStorageTable)) {
            createTable.execute();
        } catch (SQLException e) {
            throw new SKException("Failed to create collection", e);
        }

        try (Connection connection = dataSource.getConnection();
            PreparedStatement insert = connection.prepareStatement(insertCollectionQuery)) {
            insert.setObject(1, collectionName);
            insert.execute();
        } catch (SQLException e) {
            throw new SKException("Failed to insert collection", e);
        }
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
                        statement.setString(i + 1, new ObjectMapper().writeValueAsString(value));
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

    private String getWildcardStringWithCast(List<VectorStoreRecordField> fields) {
        StringBuilder wildcardString = new StringBuilder();
        int wildcards = fields.size();
        for (int i = 0; i < wildcards; ++i) {
            if (i > 0) {
                wildcardString.append(", ");
            }
            wildcardString.append("?");
            // Add casting for vector fields
            if (fields.get(i) instanceof VectorStoreRecordVectorField) {
                wildcardString.append("::vector");
            }
        }
        return wildcardString.toString();
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
        for (VectorStoreRecordField field : fields) {
            if (field instanceof VectorStoreRecordKeyField) {
                continue;
            }
            if (onDuplicateKeyUpdate.length() > 0) {
                onDuplicateKeyUpdate.append(", ");
            }
            onDuplicateKeyUpdate.append(field.getName())
                .append(" = EXCLUDED.")
                .append(field.getName());
        }

        String query = "INSERT INTO " + getCollectionTableName(collectionName)
            + " (" + getQueryColumnsFromFields(fields) + ")"
            + " VALUES (" + getWildcardStringWithCast(fields) + ")"
            + " ON CONFLICT (" + recordDefinition.getKeyField().getName() + ") DO UPDATE SET "
            + onDuplicateKeyUpdate;

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
        public PostgreSQLVectorStoreQueryProvider.Builder withDataSource(DataSource dataSource) {
            this.dataSource = dataSource;
            return this;
        }

        /**
         * Sets the collections table name.
         * @param collectionsTable the collections table name
         * @return the builder
         */
        public PostgreSQLVectorStoreQueryProvider.Builder withCollectionsTable(
            String collectionsTable) {
            this.collectionsTable = validateSQLidentifier(collectionsTable);
            return this;
        }

        /**
         * Sets the prefix for collection tables.
         * @param prefixForCollectionTables the prefix for collection tables
         * @return the builder
         */
        public PostgreSQLVectorStoreQueryProvider.Builder withPrefixForCollectionTables(
            String prefixForCollectionTables) {
            this.prefixForCollectionTables = validateSQLidentifier(prefixForCollectionTables);
            return this;
        }

        public PostgreSQLVectorStoreQueryProvider build() {
            if (dataSource == null) {
                throw new SKException("DataSource is required");
            }

            return new PostgreSQLVectorStoreQueryProvider(dataSource, collectionsTable,
                prefixForCollectionTables);
        }
    }
}
