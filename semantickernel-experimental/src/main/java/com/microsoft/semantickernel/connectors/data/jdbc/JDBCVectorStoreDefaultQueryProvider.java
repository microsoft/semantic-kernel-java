// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.data.jdbc;

import com.microsoft.semantickernel.data.recorddefinition.VectorStoreRecordDefinition;
import com.microsoft.semantickernel.exceptions.SKException;
import com.microsoft.semantickernel.data.recorddefinition.VectorStoreRecordField;
import com.microsoft.semantickernel.data.recordoptions.DeleteRecordOptions;
import com.microsoft.semantickernel.data.recordoptions.GetRecordOptions;
import com.microsoft.semantickernel.data.recordoptions.UpsertRecordOptions;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JDBCVectorStoreDefaultQueryProvider
    implements JDBCVectorStoreQueryProvider {
    private static final Map<Class<?>, String> supportedKeyTypes;
    private static final Map<Class<?>, String> supportedDataTypes;
    private static final Map<Class<?>, String> supportedVectorTypes;

    static {
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
        supportedDataTypes.put(Double.class, "DOUBLE");
        supportedDataTypes.put(double.class, "DOUBLE");
        supportedDataTypes.put(Boolean.class, "BOOLEAN");
        supportedDataTypes.put(boolean.class, "BOOLEAN");
        supportedDataTypes.put(OffsetDateTime.class, "TIMESTAMPTZ");

        supportedVectorTypes = new HashMap<>();
        supportedVectorTypes.put(String.class, "TEXT");
        supportedVectorTypes.put(List.class, "TEXT");
        supportedVectorTypes.put(Collection.class, "TEXT");
    }

    protected final Connection connection;
    protected final String collectionsTable;
    protected final String prefixForCollectionTables;

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public JDBCVectorStoreDefaultQueryProvider(
            @Nonnull Connection connection,
            @Nonnull String collectionsTable,
            @Nonnull String prefixForCollectionTables) {
        this.connection = connection;
        // Validate table name
        if (!isValidSQLIdentifier(collectionsTable)) {
            throw new IllegalArgumentException("Invalid collections table name: " + collectionsTable);
        }
        if (!isValidSQLIdentifier(prefixForCollectionTables)) {
            throw new IllegalArgumentException("Invalid prefix for collection tables: " + prefixForCollectionTables);
        }

        this.collectionsTable = collectionsTable;
        this.prefixForCollectionTables = prefixForCollectionTables;
    }

    public JDBCVectorStoreDefaultQueryProvider(
            @Nonnull Connection connection) {
        this(connection, DEFAULT_COLLECTIONS_TABLE, DEFAULT_PREFIX_FOR_COLLECTION_TABLES);
    }

    /**
     * Creates a new builder.
     * @return the builder
     */
    public static Builder builder() {
        return new Builder();
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

    protected String getColumnNamesAndTypes(List<Field> fields, Map<Class<?>, String> types) {
        List<String> columns = fields.stream()
                .map(field -> field.getName() + " " + types.get(field.getType()))
                .collect(Collectors.toList());

        return String.join(", ", columns);
    }

    protected String getCollectionTableName(String collectionName) {
        return prefixForCollectionTables + collectionName;
    }

    @Override
    public void prepareVectorStore() throws SQLException {
        String createCollectionsTable =
                "CREATE TABLE IF NOT EXISTS " + collectionsTable
                        + " (collectionId VARCHAR(255) PRIMARY KEY);";

        PreparedStatement createTable = connection.prepareStatement(createCollectionsTable);
        createTable.execute();
    }

    @Override
    public void validateSupportedTypes(Class<?> recordClass, VectorStoreRecordDefinition recordDefinition) {
        VectorStoreRecordDefinition.validateSupportedTypes(
                Collections.singletonList(recordDefinition.getKeyDeclaredField(recordClass)), supportedKeyTypes.keySet());
        VectorStoreRecordDefinition.validateSupportedTypes(
                recordDefinition.getDataDeclaredFields(recordClass), supportedDataTypes.keySet());
        VectorStoreRecordDefinition.validateSupportedTypes(
                recordDefinition.getVectorDeclaredFields(recordClass), supportedVectorTypes.keySet());
    }

    @Override
    public boolean collectionExists(String collectionName) throws SQLException {
        String query = "SELECT 1 FROM " + collectionsTable + " WHERE collectionId = ?";

        PreparedStatement statement = connection.prepareStatement(query);
        statement.setObject(1, collectionName);

        return statement.executeQuery().next();
    }

    @Override
    public void createCollection(String collectionName, Class<?> recordClass, VectorStoreRecordDefinition recordDefinition) throws SQLException {
        Field keyDeclaredField = recordDefinition.getKeyDeclaredField(recordClass);
        List<Field> dataDeclaredFields = recordDefinition.getDataDeclaredFields(recordClass);
        List<Field> vectorDeclaredFields = recordDefinition.getVectorDeclaredFields(recordClass);

        String createStorageTable =
                "CREATE TABLE IF NOT EXISTS " + getCollectionTableName(collectionName)
                        + " (" + keyDeclaredField.getName() + " VARCHAR(255) PRIMARY KEY, "
                        + getColumnNamesAndTypes(dataDeclaredFields, supportedDataTypes) + ", "
                        + getColumnNamesAndTypes(vectorDeclaredFields, supportedVectorTypes) + ");";

        PreparedStatement createTable = connection.prepareStatement(createStorageTable);

        String insertCollectionQuery = "INSERT INTO " + collectionsTable + " (collectionId) VALUES (?)";
        PreparedStatement insert = connection.prepareStatement(insertCollectionQuery);
        insert.setObject(1, collectionName);

        createTable.execute();
        insert.execute();
    }

    @Override
    public void deleteCollection(String collectionName) throws SQLException {
        String deleteCollectionOperation = "DELETE FROM " + collectionsTable + " WHERE collectionId = ?";
        String dropTableOperation = "DROP TABLE " + getCollectionTableName(collectionName);

        PreparedStatement deleteCollection = connection.prepareStatement(deleteCollectionOperation);
        deleteCollection.setObject(1, collectionName);

        PreparedStatement dropTable = connection.prepareStatement(dropTableOperation);

        dropTable.execute();
        deleteCollection.execute();
    }

    @Override
    public ResultSet getCollectionNames() throws SQLException {
        String query = "SELECT collectionId FROM " + collectionsTable;

        return connection.prepareStatement(query).executeQuery();
    }

    @Override
    public ResultSet getRecords(String collectionName, List<String> keys, VectorStoreRecordDefinition recordDefinition, GetRecordOptions options) throws SQLException {
        List<VectorStoreRecordField> fields;
        if (options == null || options.includeVectors()) {
            fields = recordDefinition.getAllFields();
        } else {
            fields = recordDefinition.getNonVectorFields();
        }

        String query = "SELECT " + getQueryColumnsFromFields(fields)
                + " FROM " + getCollectionTableName(collectionName)
                + " WHERE " + recordDefinition.getKeyField().getName()
                + " IN (" + getWildcardString(keys.size()) + ")";

        PreparedStatement statement = connection.prepareStatement(query);
        for (int i = 0; i < keys.size(); ++i) {
            try {
                statement.setObject(i + 1, keys.get(i));
            } catch (SQLException e) {
                throw new SKException("Failed to set statement values", e);
            }
        }

        return statement.executeQuery();
    }

    @Override
    public void upsertRecords(String collectionName, List<?> records, VectorStoreRecordDefinition recordDefinition, UpsertRecordOptions options) throws SQLException {
        throw new UnsupportedOperationException(
                "Upsert is not supported. Try with a specific query provider.");
    }

    @Override
    public void deleteRecords(String collectionName, List<String> keys, VectorStoreRecordDefinition recordDefinition, DeleteRecordOptions options) throws SQLException {
        String query = "DELETE FROM " + getCollectionTableName(collectionName)
                + " WHERE " + recordDefinition.getKeyField().getName()
                + " IN (" + getWildcardString(keys.size()) + ")";

        PreparedStatement statement = connection.prepareStatement(query);
        for (int i = 0; i < keys.size(); ++i) {
            try {
                statement.setObject(i + 1, keys.get(i));
            } catch (SQLException e) {
                throw new SKException("Failed to set statement values", e);
            }
        }

        statement.execute();
    }

    public static boolean isValidSQLIdentifier(String identifier) {
        return identifier.matches("[a-zA-Z_][a-zA-Z0-9_]*");
    }

    /**
     * The builder for {@link JDBCVectorStoreDefaultQueryProvider}.
     */
    public static class Builder
        implements JDBCVectorStoreQueryProvider.Builder {
        protected Connection connection;
        protected String collectionsTable = DEFAULT_COLLECTIONS_TABLE;
        protected String prefixForCollectionTables = DEFAULT_PREFIX_FOR_COLLECTION_TABLES;

        /**
         * Sets the connection.
         * @param connection the connection
         * @return the builder
         */
        public Builder withConnection(Connection connection) {
            this.connection = connection;
            return this;
        }

        /**
         * Sets the collections table name.
         * @param collectionsTable the collections table name
         * @return the builder
         */
        public Builder withCollectionsTable(String collectionsTable) {
            this.collectionsTable = collectionsTable;
            return this;
        }

        /**
         * Sets the prefix for collection tables.
         * @param prefixForCollectionTables the prefix for collection tables
         * @return the builder
         */
        public Builder withPrefixForCollectionTables(String prefixForCollectionTables) {
            this.prefixForCollectionTables = prefixForCollectionTables;
            return this;
        }

        @Override
        public JDBCVectorStoreDefaultQueryProvider build() {
            if (connection == null) {
                throw new IllegalArgumentException("connection is required");
            }

            return new JDBCVectorStoreDefaultQueryProvider(connection, collectionsTable, prefixForCollectionTables);
        }
    }
}
