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
    private final DataSource dataSource;
    private final String collectionsTable;
    private final String prefixForCollectionTables;

    @SuppressFBWarnings("EI_EXPOSE_REP2") // DataSource is not exposed
    protected JDBCVectorStoreDefaultQueryProvider(
        @Nonnull DataSource dataSource,
        @Nonnull String collectionsTable,
        @Nonnull String prefixForCollectionTables) {
        this.dataSource = dataSource;
        this.collectionsTable = collectionsTable;
        this.prefixForCollectionTables = prefixForCollectionTables;
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
        return validateSQLidentifier(prefixForCollectionTables + collectionName);
    }

    @Override
    public void prepareVectorStore() {
        String createCollectionsTable = "CREATE TABLE IF NOT EXISTS "
            + validateSQLidentifier(collectionsTable)
            + " (collectionId VARCHAR(255) PRIMARY KEY);";

        try (Connection connection = dataSource.getConnection();
            PreparedStatement createTable = connection.prepareStatement(createCollectionsTable)) {
            createTable.execute();
        } catch (SQLException e) {
            throw new SKException("Failed to prepare vector store", e);
        }
    }

    @Override
    public void validateSupportedTypes(Class<?> recordClass,
        VectorStoreRecordDefinition recordDefinition) {
        VectorStoreRecordDefinition.validateSupportedTypes(
            Collections.singletonList(recordDefinition.getKeyDeclaredField(recordClass)),
            supportedKeyTypes.keySet());
        VectorStoreRecordDefinition.validateSupportedTypes(
            recordDefinition.getDataDeclaredFields(recordClass), supportedDataTypes.keySet());
        VectorStoreRecordDefinition.validateSupportedTypes(
            recordDefinition.getVectorDeclaredFields(recordClass), supportedVectorTypes.keySet());
    }

    @Override
    public boolean collectionExists(String collectionName) {
        String query = "SELECT 1 FROM " + validateSQLidentifier(collectionsTable)
            + " WHERE collectionId = ?";

        try (Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setObject(1, collectionName);

            return statement.executeQuery().next();
        } catch (SQLException e) {
            throw new SKException("Failed to check if collection exists", e);
        }
    }

    @Override
    @SuppressFBWarnings("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING") // SQL query is generated dynamically with valid identifiers
    public void createCollection(String collectionName, Class<?> recordClass,
        VectorStoreRecordDefinition recordDefinition) {
        Field keyDeclaredField = recordDefinition.getKeyDeclaredField(recordClass);
        List<Field> dataDeclaredFields = recordDefinition.getDataDeclaredFields(recordClass);
        List<Field> vectorDeclaredFields = recordDefinition.getVectorDeclaredFields(recordClass);

        String createStorageTable = "CREATE TABLE IF NOT EXISTS "
            + getCollectionTableName(collectionName)
            + " (" + keyDeclaredField.getName() + " VARCHAR(255) PRIMARY KEY, "
            + getColumnNamesAndTypes(dataDeclaredFields, supportedDataTypes) + ", "
            + getColumnNamesAndTypes(vectorDeclaredFields, supportedVectorTypes) + ");";

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

    @Override
    public void deleteCollection(String collectionName) {
        String deleteCollectionOperation = "DELETE FROM " + validateSQLidentifier(collectionsTable)
            + " WHERE collectionId = ?";
        String dropTableOperation = "DROP TABLE " + getCollectionTableName(collectionName);

        try (Connection connection = dataSource.getConnection();
            PreparedStatement deleteCollection = connection
                .prepareStatement(deleteCollectionOperation)) {
            deleteCollection.setObject(1, collectionName);
            deleteCollection.execute();
        } catch (SQLException e) {
            throw new SKException("Failed to delete collection", e);
        }

        try (Connection connection = dataSource.getConnection();
            PreparedStatement dropTable = connection.prepareStatement(dropTableOperation)) {
            dropTable.execute();
        } catch (SQLException e) {
            throw new SKException("Failed to drop table", e);
        }
    }

    @Override
    public List<String> getCollectionNames() {
        String query = "SELECT collectionId FROM " + validateSQLidentifier(collectionsTable);

        try (Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(query)) {
            List<String> collectionNames = new ArrayList<>();
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                collectionNames.add(resultSet.getString(1));
            }

            return Collections.unmodifiableList(collectionNames);
        } catch (SQLException e) {
            throw new SKException("Failed to get collection names", e);
        }
    }

    @Override
    public <Record> List<Record> getRecords(String collectionName, List<String> keys,
        VectorStoreRecordDefinition recordDefinition, JDBCVectorStoreRecordMapper<Record> mapper,
        GetRecordOptions options) {
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

        try (Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(query)) {
            for (int i = 0; i < keys.size(); ++i) {
                statement.setObject(i + 1, keys.get(i));
            }

            List<Record> records = new ArrayList<>();
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                records.add(mapper.mapStorageModeltoRecord(resultSet));
            }

            return Collections.unmodifiableList(records);
        } catch (SQLException e) {
            throw new SKException("Failed to set statement values", e);
        }
    }

    @Override
    public void upsertRecords(String collectionName, List<?> records,
        VectorStoreRecordDefinition recordDefinition, UpsertRecordOptions options) {
        throw new UnsupportedOperationException(
            "Upsert is not supported. Try with a specific query provider.");
    }

    @Override
    public void deleteRecords(String collectionName, List<String> keys,
        VectorStoreRecordDefinition recordDefinition, DeleteRecordOptions options) {
        String query = "DELETE FROM " + getCollectionTableName(collectionName)
            + " WHERE " + recordDefinition.getKeyField().getName()
            + " IN (" + getWildcardString(keys.size()) + ")";

        try (Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(query)) {
            for (int i = 0; i < keys.size(); ++i) {
                statement.setObject(i + 1, keys.get(i));
            }

            statement.execute();
        } catch (SQLException e) {
            throw new SKException("Failed to set statement values", e);
        }
    }

    public static String validateSQLidentifier(String identifier) {
        if (identifier.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
            return identifier;
        }
        throw new IllegalArgumentException("Invalid SQL identifier: " + identifier);
    }

    /**
     * The builder for {@link JDBCVectorStoreDefaultQueryProvider}.
     */
    public static class Builder
        implements JDBCVectorStoreQueryProvider.Builder {
        private DataSource dataSource;
        private String collectionsTable = DEFAULT_COLLECTIONS_TABLE;
        private String prefixForCollectionTables = DEFAULT_PREFIX_FOR_COLLECTION_TABLES;

        /**
         * Sets the data source.
         * @param dataSource the data source
         * @return the builder
         */
        @SuppressFBWarnings("EI_EXPOSE_REP2") // DataSource is not exposed
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

        @Override
        public JDBCVectorStoreDefaultQueryProvider build() {
            if (dataSource == null) {
                throw new IllegalArgumentException("DataSource is required");
            }

            return new JDBCVectorStoreDefaultQueryProvider(dataSource, collectionsTable,
                prefixForCollectionTables);
        }
    }
}
