// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.data.postgres;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
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

import javax.annotation.Nonnull;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostgreSQLVectorStoreQueryProvider extends
    JDBCVectorStoreDefaultQueryProvider implements JDBCVectorStoreQueryProvider {

    private final Map<Class<?>, String> supportedKeyTypes;
    private final Map<Class<?>, String> supportedDataTypes;
    private final Map<Class<?>, String> supportedVectorTypes;

    private final DataSource dataSource;
    private final String collectionsTable;
    private final String prefixForCollectionTables;
    private final ObjectMapper objectMapper;

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    private PostgreSQLVectorStoreQueryProvider(
            @Nonnull DataSource dataSource,
            @Nonnull String collectionsTable,
            @Nonnull String prefixForCollectionTables,
            @Nonnull ObjectMapper objectMapper) {
        super(dataSource, collectionsTable, prefixForCollectionTables);
        this.dataSource = dataSource;
        this.collectionsTable = collectionsTable;
        this.prefixForCollectionTables = prefixForCollectionTables;
        this.objectMapper = objectMapper;

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
     * Gets the supported key types and their corresponding SQL types.
     *
     * @return the supported key types
     */
    @Override
    public Map<Class<?>, String> getSupportedKeyTypes() {
        return new HashMap<>(this.supportedKeyTypes);
    }

    /**
     * Gets the supported data types and their corresponding SQL types.
     *
     * @return the supported data types
     */
    @Override
    public Map<Class<?>, String> getSupportedDataTypes() {
        return new HashMap<>(this.supportedDataTypes);
    }

    /**
     * Gets the supported vector types and their corresponding SQL types.
     *
     * @return the supported vector types
     */
    @Override
    public Map<Class<?>, String> getSupportedVectorTypes() {
        return new HashMap<>(this.supportedVectorTypes);
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

    private String getColumnNamesAndTypesForVectorFields(
        List<VectorStoreRecordVectorField> fields) {
        StringBuilder columnNames = new StringBuilder();
        for (VectorStoreRecordVectorField field : fields) {
            if (columnNames.length() > 0) {
                columnNames.append(", ");
            }

            if (field.getFieldType().equals(String.class)) {
                columnNames.append(field.getEffectiveStorageName()).append(" ")
                    .append(supportedVectorTypes.get(String.class));
            } else {
                // Get the vector type and dimensions
                String type = String.format(supportedVectorTypes.get(field.getFieldType()),
                    field.getDimensions());
                columnNames.append(field.getEffectiveStorageName()).append(" ").append(type);
            }
        }

        return columnNames.toString();
    }

    /**
     * Creates a collection.
     *
     * @param collectionName the collection name
     * @param recordDefinition the record definition
     * @throws SKException if an error occurs while creating the collection
     */
    @Override
    @SuppressFBWarnings("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING") // SQL query is generated dynamically with valid identifiers
    public void createCollection(String collectionName,
        VectorStoreRecordDefinition recordDefinition) {

        String createStorageTable = "CREATE TABLE IF NOT EXISTS "
            + getCollectionTableName(collectionName)
            + " (" + recordDefinition.getKeyField().getStorageName() + " VARCHAR(255) PRIMARY KEY, "
            + getColumnNamesAndTypes(new ArrayList<>(recordDefinition.getDataFields()),
                supportedDataTypes)
            + ", "
            + getColumnNamesAndTypesForVectorFields(recordDefinition.getVectorFields())
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
        JsonNode jsonNode = objectMapper.valueToTree(record);

        for (int i = 0; i < fields.size(); ++i) {
            VectorStoreRecordField field = fields.get(i);
            try {
                JsonNode valueNode = jsonNode.get(field.getEffectiveStorageName());

                if (field instanceof VectorStoreRecordVectorField) {
                    // Convert the vector field to a string
                    if (!field.getFieldType().equals(String.class)) {
                        statement.setObject(i + 1, objectMapper.writeValueAsString(valueNode));
                        continue;
                    }
                }

                statement.setObject(i + 1,
                    objectMapper.convertValue(valueNode, field.getFieldType()));
            } catch (SQLException | JsonProcessingException e) {
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
            onDuplicateKeyUpdate.append(field.getEffectiveStorageName())
                .append(" = EXCLUDED.")
                .append(field.getEffectiveStorageName());
        }

        String query = "INSERT INTO " + getCollectionTableName(collectionName)
            + " (" + getQueryColumnsFromFields(fields) + ")"
            + " VALUES (" + getWildcardStringWithCast(fields) + ")"
            + " ON CONFLICT (" + recordDefinition.getKeyField().getEffectiveStorageName()
            + ") DO UPDATE SET "
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
        private ObjectMapper objectMapper = new ObjectMapper();

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

        /**
         * Sets the object mapper.
         *
         * @param objectMapper the object mapper
         * @return the builder
         */
        @SuppressFBWarnings("EI_EXPOSE_REP2")
        public PostgreSQLVectorStoreQueryProvider.Builder withObjectMapper(
            ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
            return this;
        }

        public PostgreSQLVectorStoreQueryProvider build() {
            if (dataSource == null) {
                throw new SKException("DataSource is required");
            }

            return new PostgreSQLVectorStoreQueryProvider(dataSource, collectionsTable,
                prefixForCollectionTables, objectMapper);
        }
    }
}
