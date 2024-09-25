// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.data.jdbc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.microsoft.semantickernel.data.vectorsearch.VectorOperations;
import com.microsoft.semantickernel.data.vectorsearch.VectorSearchResult;
import com.microsoft.semantickernel.data.vectorstorage.VectorStoreRecordMapper;
import com.microsoft.semantickernel.data.vectorstorage.definition.DistanceFunction;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordDefinition;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordVectorField;
import com.microsoft.semantickernel.data.vectorstorage.options.VectorSearchOptions;
import com.microsoft.semantickernel.exceptions.SKException;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordField;
import com.microsoft.semantickernel.data.vectorstorage.options.DeleteRecordOptions;
import com.microsoft.semantickernel.data.vectorstorage.options.GetRecordOptions;
import com.microsoft.semantickernel.data.vectorstorage.options.UpsertRecordOptions;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JDBCVectorStoreQueryProvider
    implements SQLVectorStoreQueryProvider {
    private static final Logger LOGGER = LoggerFactory
        .getLogger(JDBCVectorStoreQueryProvider.class);

    private final Map<Class<?>, String> supportedKeyTypes;
    private final Map<Class<?>, String> supportedDataTypes;
    private final Map<Class<?>, String> supportedVectorTypes;
    private final DataSource dataSource;
    private final String collectionsTable;
    private final String prefixForCollectionTables;

    @SuppressFBWarnings("EI_EXPOSE_REP2") // DataSource is not exposed
    protected JDBCVectorStoreQueryProvider(
        @Nonnull DataSource dataSource,
        @Nonnull String collectionsTable,
        @Nonnull String prefixForCollectionTables) {
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
        return Stream.generate(() -> "?")
            .limit(wildcards)
            .collect(Collectors.joining(", "));
    }

    /**
     * Gets the key column name from a key field.
     * @param keyField the key field
     * @return the key column name
     */
    protected String getKeyColumnName(VectorStoreRecordField keyField) {
        return validateSQLidentifier(keyField.getEffectiveStorageName());
    }

    /**
     * Formats the query columns from a record definition.
     * @param fields the fields to get the columns from
     * @return the formatted query columns
     */
    protected String getQueryColumnsFromFields(List<VectorStoreRecordField> fields) {
        return fields.stream()
            .map(VectorStoreRecordField::getEffectiveStorageName)
            .map(JDBCVectorStoreQueryProvider::validateSQLidentifier)
            .collect(Collectors.joining(", "));
    }

    /**
     * Formats the column names and types for a table.
     * @param fields the fields
     * @param types the types
     * @return the formatted column names and types
     */
    protected String getColumnNamesAndTypes(List<VectorStoreRecordField> fields,
        Map<Class<?>, String> types) {
        List<String> columns = fields.stream()
            .map(field -> validateSQLidentifier(field.getEffectiveStorageName()) + " "
                + types.get(field.getFieldType()))
            .collect(Collectors.toList());

        return String.join(", ", columns);
    }

    protected String getCollectionTableName(String collectionName) {
        return validateSQLidentifier(prefixForCollectionTables + collectionName);
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
     * Prepares the vector store.
     * Executes any necessary setup steps for the vector store.
     *
     * @throws SKException if an error occurs while preparing the vector store
     */
    @Override
    public void prepareVectorStore() {
        String createCollectionsTable = formatQuery(
            "CREATE TABLE IF NOT EXISTS %s (collectionId VARCHAR(255) PRIMARY KEY);",
            validateSQLidentifier(collectionsTable));

        try (Connection connection = dataSource.getConnection();
            PreparedStatement createTable = connection.prepareStatement(createCollectionsTable)) {
            createTable.execute();
        } catch (SQLException e) {
            throw new SKException("Failed to prepare vector store", e);
        }
    }

    /**
     * Checks if the types of the record class fields are supported.
     *
     * @param recordDefinition the record definition
     * @throws SKException if the types are not supported
     */
    @Override
    public void validateSupportedTypes(VectorStoreRecordDefinition recordDefinition) {

        VectorStoreRecordDefinition.validateSupportedTypes(
            Collections.singletonList(recordDefinition.getKeyField()),
            getSupportedKeyTypes().keySet());
        VectorStoreRecordDefinition.validateSupportedTypes(
            new ArrayList<>(recordDefinition.getDataFields()),
            getSupportedDataTypes().keySet());
        VectorStoreRecordDefinition.validateSupportedTypes(
            new ArrayList<>(recordDefinition.getVectorFields()),
            getSupportedVectorTypes().keySet());
    }

    /**
     * Checks if a collection exists.
     *
     * @param collectionName the collection name
     * @return true if the collection exists, false otherwise
     * @throws SKException if an error occurs while checking if the collection exists
     */
    @Override
    public boolean collectionExists(String collectionName) {
        String query = formatQuery("SELECT 1 FROM %s WHERE collectionId = ?",
            validateSQLidentifier(collectionsTable));

        try (Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setObject(1, collectionName);

            return statement.executeQuery().next();
        } catch (SQLException e) {
            throw new SKException("Failed to check if collection exists", e);
        }
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

        // No approximate search is supported in JDBCVectorStoreQueryProvider
        if (recordDefinition.getVectorFields().stream()
            .anyMatch(field -> field.getIndexKind() != null)) {
            LOGGER
                .warn(String.format("Indexes are not supported in %s. Ignoring indexKind property.",
                    this.getClass().getName()));
        }

        String createStorageTable = formatQuery("CREATE TABLE IF NOT EXISTS %s ("
            + "%s VARCHAR(255) PRIMARY KEY, "
            + "%s, "
            + "%s);",
            getCollectionTableName(collectionName),
            getKeyColumnName(recordDefinition.getKeyField()),
            getColumnNamesAndTypes(new ArrayList<>(recordDefinition.getDataFields()),
                getSupportedDataTypes()),
            getColumnNamesAndTypes(new ArrayList<>(recordDefinition.getVectorFields()),
                getSupportedVectorTypes()));

        String insertCollectionQuery = formatQuery("INSERT INTO %s (collectionId) VALUES (?)",
            validateSQLidentifier(collectionsTable));

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

    /**
     * Deletes a collection.
     *
     * @param collectionName the collection name
     * @throws SKException if an error occurs while deleting the collection
     */
    @Override
    public void deleteCollection(String collectionName) {
        String deleteCollectionOperation = formatQuery("DELETE FROM %s WHERE collectionId = ?",
            validateSQLidentifier(collectionsTable));
        String dropTableOperation = formatQuery("DROP TABLE %s",
            getCollectionTableName(collectionName));

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

    /**
     * Gets the collection names.
     *
     * @return the collection names
     * @throws SKException if an error occurs while getting the collection names
     */
    @Override
    public List<String> getCollectionNames() {
        String query = formatQuery("SELECT collectionId FROM %s",
            validateSQLidentifier(collectionsTable));

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

    /**
     * Gets a list of records from the store.
     *
     * @param collectionName the collection name
     * @param keys the keys
     * @param recordDefinition the record definition
     * @param mapper the mapper
     * @param options the options
     * @return the records
     * @param <Record> the record type
     * @throws SKException if an error occurs while getting the records
     */
    @Override
    public <Record> List<Record> getRecords(String collectionName, List<String> keys,
        VectorStoreRecordDefinition recordDefinition,
        VectorStoreRecordMapper<Record, ResultSet> mapper,
        GetRecordOptions options) {
        List<VectorStoreRecordField> fields;
        if (options != null && options.isIncludeVectors()) {
            fields = recordDefinition.getAllFields();
        } else {
            fields = recordDefinition.getNonVectorFields();
        }

        String query = formatQuery("SELECT %s FROM %s WHERE %s IN (%s)",
            getQueryColumnsFromFields(fields),
            getCollectionTableName(collectionName),
            getKeyColumnName(recordDefinition.getKeyField()),
            getWildcardString(keys.size()));

        try (Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(query)) {
            for (int i = 0; i < keys.size(); ++i) {
                statement.setObject(i + 1, keys.get(i));
            }

            List<Record> records = new ArrayList<>();
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                records.add(mapper.mapStorageModelToRecord(resultSet, options));
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

    /**
     * Deletes records.
     *
     * @param collectionName the collection name
     * @param keys the keys
     * @param recordDefinition the record definition
     * @param options the options
     * @throws SKException if an error occurs while deleting the records
     */
    @Override
    public void deleteRecords(String collectionName, List<String> keys,
        VectorStoreRecordDefinition recordDefinition, DeleteRecordOptions options) {
        String query = formatQuery("DELETE FROM %s WHERE %s IN (%s)",
            getCollectionTableName(collectionName),
            getKeyColumnName(recordDefinition.getKeyField()),
            getWildcardString(keys.size()));

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

    protected <Record> List<Record> getRecordsWithFilter(String collectionName,
        VectorStoreRecordDefinition recordDefinition,
        VectorStoreRecordMapper<Record, ResultSet> mapper, GetRecordOptions options, String filter,
        List<Object> parameters) {
        List<VectorStoreRecordField> fields;
        if (options.isIncludeVectors()) {
            fields = recordDefinition.getAllFields();
        } else {
            fields = recordDefinition.getNonVectorFields();
        }

        String filterClause = filter == null || filter.isEmpty() ? "" : "WHERE " + filter;
        String selectQuery = formatQuery("SELECT %s FROM %s %s",
            getQueryColumnsFromFields(fields),
            getCollectionTableName(collectionName),
            filterClause);

        try (Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(selectQuery)) {
            if (parameters != null) {
                for (int i = 0; i < parameters.size(); ++i) {
                    statement.setObject(i + 1, parameters.get(i));
                }
            }

            List<Record> records = new ArrayList<>();
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                records.add(mapper.mapStorageModelToRecord(resultSet, options));
            }

            return Collections.unmodifiableList(records);
        } catch (SQLException e) {
            throw new SKException("Failed to set statement values", e);
        }
    }

    /**
     * Vector search.
     * Executes a vector search query and returns the results.
     * The results are mapped to the specified record type using the provided mapper.
     * The query is executed against the specified collection.
     *
     * @param <Record> the record type
     * @param collectionName the collection name
     * @param vector the vector to search with
     * @param options the search options
     * @param recordDefinition the record definition
     * @param mapper the mapper, responsible for mapping the result set to the record type.
     * @return the search results
     */
    @Override
    public <Record> List<VectorSearchResult<Record>> search(String collectionName,
        List<Float> vector, VectorSearchOptions options,
        VectorStoreRecordDefinition recordDefinition,
        VectorStoreRecordMapper<Record, ResultSet> mapper) {
        if (recordDefinition.getVectorFields().isEmpty()) {
            throw new SKException("No vector fields defined. Cannot perform vector search");
        }

        VectorStoreRecordVectorField firstVectorField = recordDefinition.getVectorFields()
            .get(0);
        if (options == null) {
            options = VectorSearchOptions.createDefault(firstVectorField.getName());
        }

        VectorStoreRecordVectorField vectorField = options.getVectorFieldName() == null
            ? firstVectorField
            : (VectorStoreRecordVectorField) recordDefinition
                .getField(options.getVectorFieldName());

        String filter = SQLVectorStoreRecordCollectionSearchMapping
            .buildFilter(options.getVectorSearchFilter(), recordDefinition);
        List<Object> parameters = SQLVectorStoreRecordCollectionSearchMapping
            .getFilterParameters(options.getVectorSearchFilter());

        List<Record> records = getRecordsWithFilter(collectionName, recordDefinition, mapper,
            new GetRecordOptions(true), filter, parameters);

        DistanceFunction distanceFunction = vectorField.getDistanceFunction() == null
            ? DistanceFunction.EUCLIDEAN_DISTANCE
            : vectorField.getDistanceFunction();

        return VectorOperations.exactSimilaritySearch(records, vector, vectorField,
            distanceFunction, options);
    }

    /**
     * Validates an SQL identifier.
     *
     * @param identifier the identifier
     * @return the identifier if it is valid
     * @throws SKException if the identifier is invalid
     */
    public static String validateSQLidentifier(String identifier) {
        if (identifier.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
            return identifier;
        }
        throw new SKException("Invalid SQL identifier: " + identifier);
    }

    /**
     * Formats a query.
     *
     * @param query the query
     * @param args the arguments
     * @return the formatted query
     */
    public String formatQuery(String query, String... args) {
        return String.format(query, (Object[]) args);
    }

    /**
     * The builder for {@link JDBCVectorStoreQueryProvider}.
     */
    public static class Builder
        implements SQLVectorStoreQueryProvider.Builder {
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
        public JDBCVectorStoreQueryProvider build() {
            if (dataSource == null) {
                throw new SKException("DataSource is required");
            }

            return new JDBCVectorStoreQueryProvider(dataSource, collectionsTable,
                prefixForCollectionTables);
        }
    }
}
