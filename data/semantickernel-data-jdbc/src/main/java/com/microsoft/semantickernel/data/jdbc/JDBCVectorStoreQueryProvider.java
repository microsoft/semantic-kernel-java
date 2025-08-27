// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.jdbc;

import com.microsoft.semantickernel.data.filter.AnyTagEqualToFilterClause;
import com.microsoft.semantickernel.data.filter.EqualToFilterClause;
import com.microsoft.semantickernel.data.vectorsearch.VectorOperations;
import com.microsoft.semantickernel.data.vectorsearch.VectorSearchFilter;
import com.microsoft.semantickernel.data.vectorsearch.VectorSearchResults;
import com.microsoft.semantickernel.data.vectorstorage.VectorStoreRecordMapper;
import com.microsoft.semantickernel.data.vectorstorage.definition.DistanceFunction;
import com.microsoft.semantickernel.data.vectorstorage.definition.IndexKind;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordDefinition;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordField;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordVectorField;
import com.microsoft.semantickernel.data.vectorstorage.options.DeleteRecordOptions;
import com.microsoft.semantickernel.data.vectorstorage.options.GetRecordOptions;
import com.microsoft.semantickernel.data.vectorstorage.options.UpsertRecordOptions;
import com.microsoft.semantickernel.data.vectorstorage.options.VectorSearchOptions;
import com.microsoft.semantickernel.exceptions.SKException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A JDBC vector store query provider.
 */
public class JDBCVectorStoreQueryProvider
    implements SQLVectorStoreQueryProvider,
    SQLVectorStoreFilterQueryProvider {

    private static final Logger LOGGER = LoggerFactory
        .getLogger(JDBCVectorStoreQueryProvider.class);

    protected final Map<Class<?>, String> supportedKeyTypes;
    protected final Map<Class<?>, String> supportedDataTypes;
    protected final Map<Class<?>, String> supportedVectorTypes;

    protected final DataSource dataSource;
    private final String collectionsTable;
    private final String prefixForCollectionTables;

    private final Object dbCreationLock = new Object();

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
        supportedDataTypes.put(List.class, "TEXT");

        supportedVectorTypes = new HashMap<>();
        supportedVectorTypes.put(String.class, "TEXT");
        supportedVectorTypes.put(List.class, "TEXT");
        supportedVectorTypes.put(Collection.class, "TEXT");
    }

    /**
     * Creates a new instance of the JDBCVectorStoreQueryProvider class.
     *
     * @param dataSource                the data source
     * @param collectionsTable          the collections table
     * @param prefixForCollectionTables the prefix for collection tables
     * @param supportedKeyTypes         the supported key types
     * @param supportedDataTypes        the supported data types
     * @param supportedVectorTypes      the supported vector types
     */
    public JDBCVectorStoreQueryProvider(
        @SuppressFBWarnings("EI_EXPOSE_REP2") @Nonnull DataSource dataSource,
        @Nonnull String collectionsTable,
        @Nonnull String prefixForCollectionTables,
        @Nonnull Map<Class<?>, String> supportedKeyTypes,
        @Nonnull Map<Class<?>, String> supportedDataTypes,
        @Nonnull Map<Class<?>, String> supportedVectorTypes) {
        this.dataSource = dataSource;
        this.collectionsTable = collectionsTable;
        this.prefixForCollectionTables = prefixForCollectionTables;
        this.supportedKeyTypes = new HashMap<>(supportedKeyTypes);
        this.supportedDataTypes = new HashMap<>(supportedDataTypes);
        this.supportedVectorTypes = new HashMap<>(supportedVectorTypes);
    }

    /**
     * Creates a new builder.
     *
     * @return the builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Formats a wildcard string for a query.
     *
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
     *
     * @param keyField the key field
     * @return the key column name
     */
    protected String getKeyColumnName(VectorStoreRecordField keyField) {
        return validateSQLidentifier(keyField.getEffectiveStorageName());
    }

    /**
     * Formats the query columns from a record definition.
     *
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
     *
     * @param fields the fields
     * @param types  the types
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
     * Prepares the vector store. Executes any necessary setup steps for the vector store.
     *
     * @throws SKException if an error occurs while preparing the vector store
     */
    @Override
    public void prepareVectorStore() {
        String createCollectionsTable = formatQuery(
            "CREATE TABLE IF NOT EXISTS %s (collectionId VARCHAR(255) PRIMARY KEY)",
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
     * @param collectionName   the collection name
     * @param recordDefinition the record definition
     * @throws SKException if an error occurs while creating the collection
     */
    @Override
    @SuppressFBWarnings("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
    @GuardedBy("dbCreationLock")
    // SQL query is generated dynamically with valid identifiers
    public void createCollection(String collectionName,
        VectorStoreRecordDefinition recordDefinition) {

        synchronized (dbCreationLock) {
            // No approximate search is supported in JDBCVectorStoreQueryProvider
            if (recordDefinition.getVectorFields().stream()
                .anyMatch(
                    field -> field.getIndexKind() != null && field.getIndexKind() != IndexKind.FLAT
                        && field.getIndexKind() != IndexKind.UNDEFINED)) {
                LOGGER
                    .warn(String.format(
                        "Indexes are not supported in %s. Ignoring indexKind property.",
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

            String insertCollectionQuery = this.getInsertCollectionQuery(collectionsTable);

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
    }

    protected String getInsertCollectionQuery(String collectionsTable) {
        return formatQuery(
            "INSERT IGNORE INTO %s (collectionId) VALUES (?)",
            validateSQLidentifier(collectionsTable));
    }

    /**
     * Deletes a collection.
     *
     * @param collectionName the collection name
     * @throws SKException if an error occurs while deleting the collection
     */
    @Override
    @GuardedBy("dbCreationLock")
    public void deleteCollection(String collectionName) {
        synchronized (dbCreationLock) {
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
     * @param collectionName   the collection name
     * @param keys             the keys
     * @param recordDefinition the record definition
     * @param mapper           the mapper
     * @param options          the options
     * @param <Record>         the record type
     * @return the records
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

        String query;

        if (options != null && options.isWildcardKeyMatching()) {
            if (keys.size() > 1) {
                throw new SKException("If using wildcard key matching, only one key is allowed");
            }
            query = "SELECT %s FROM %s WHERE %s LIKE (%s)";
        } else {
            query = "SELECT %s FROM %s WHERE %s IN (%s)";
        }

        query = formatQuery(query,
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
     * @param collectionName   the collection name
     * @param keys             the keys
     * @param recordDefinition the record definition
     * @param options          the options
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
        VectorStoreRecordMapper<Record, ResultSet> mapper,
        GetRecordOptions options,
        String filter,
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
     * Vector search. Executes a vector search query and returns the results. The results are mapped
     * to the specified record type using the provided mapper. The query is executed against the
     * specified collection.
     *
     * @param <Record>         the record type
     * @param collectionName   the collection name
     * @param vector           the vector to search with
     * @param options          the search options
     * @param recordDefinition the record definition
     * @param mapper           the mapper, responsible for mapping the result set to the record
     *                         type.
     * @return the search results
     */
    @Override
    public <Record> VectorSearchResults<Record> search(String collectionName,
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

        String filter = getFilter(options.getVectorSearchFilter(), recordDefinition);
        List<Object> parameters = getFilterParameters(options.getVectorSearchFilter());

        List<Record> records = getRecordsWithFilter(collectionName, recordDefinition, mapper,
            new GetRecordOptions(true), filter, parameters);

        DistanceFunction distanceFunction = vectorField
            .getDistanceFunction() == DistanceFunction.UNDEFINED
                ? DistanceFunction.EUCLIDEAN_DISTANCE
                : vectorField.getDistanceFunction();

        return new VectorSearchResults<>(
            VectorOperations.exactSimilaritySearch(records, vector, vectorField,
                distanceFunction, options));
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
     * @param args  the arguments
     * @return the formatted query
     */
    public String formatQuery(String query, String... args) {
        return String.format(query, (Object[]) args);
    }

    /**
     * Gets the filter query string for the given vector search filter and record definition.
     *
     * @param filter           The filter to get the filter string for.
     * @param recordDefinition The record definition to get the filter string for.
     * @return The filter string.
     */
    @Override
    public String getFilter(VectorSearchFilter filter,
        VectorStoreRecordDefinition recordDefinition) {
        if (filter == null
            || filter.getFilterClauses().isEmpty()) {
            return "";
        }

        return filter.getFilterClauses().stream().map(filterClause -> {
            if (filterClause instanceof EqualToFilterClause) {
                EqualToFilterClause equalToFilterClause = (EqualToFilterClause) filterClause;
                return getEqualToFilter(new EqualToFilterClause(
                    recordDefinition.getField(equalToFilterClause.getFieldName())
                        .getEffectiveStorageName(),
                    equalToFilterClause.getValue()));
            } else if (filterClause instanceof AnyTagEqualToFilterClause) {
                AnyTagEqualToFilterClause anyTagEqualToFilterClause = (AnyTagEqualToFilterClause) filterClause;
                return getAnyTagEqualToFilter(new AnyTagEqualToFilterClause(
                    recordDefinition.getField(anyTagEqualToFilterClause.getFieldName())
                        .getEffectiveStorageName(),
                    anyTagEqualToFilterClause.getValue()));
            } else {
                throw new SKException("Unsupported filter clause type '"
                    + filterClause.getClass().getSimpleName() + "'.");
            }
        }).collect(Collectors.joining(" AND "));
    }

    /**
     * Gets the filter parameters for the given vector search filter to associate with the filter
     * string generated by the getFilter method.
     *
     * @param filter The filter to get the filter parameters for.
     * @return The filter parameters.
     */
    @Override
    public List<Object> getFilterParameters(VectorSearchFilter filter) {
        if (filter == null
            || filter.getFilterClauses().isEmpty()) {
            return Collections.emptyList();
        }

        return filter.getFilterClauses().stream().map(filterClause -> {
            if (filterClause instanceof EqualToFilterClause) {
                EqualToFilterClause equalToFilterClause = (EqualToFilterClause) filterClause;
                return equalToFilterClause.getValue();
            } else if (filterClause instanceof AnyTagEqualToFilterClause) {
                AnyTagEqualToFilterClause anyTagEqualToFilterClause = (AnyTagEqualToFilterClause) filterClause;
                return String.format("%%\"%s\"%%", anyTagEqualToFilterClause.getValue());
            } else {
                throw new SKException("Unsupported filter clause type '"
                    + filterClause.getClass().getSimpleName() + "'.");
            }
        }).collect(Collectors.toList());
    }

    @Override
    public String getEqualToFilter(EqualToFilterClause filterClause) {
        String fieldName = JDBCVectorStoreQueryProvider
            .validateSQLidentifier(filterClause.getFieldName());
        Object value = filterClause.getValue();

        if (value instanceof String) {
            return String.format("%s = ?", fieldName);
        } else if (value instanceof Boolean) {
            return String.format("%s = ?", fieldName);
        } else if (value instanceof Integer) {
            return String.format("%s = ?", fieldName);
        } else if (value instanceof Long) {
            return String.format("%s = ?", fieldName);
        } else if (value instanceof Float) {
            return String.format("%s = ?", fieldName);
        } else if (value instanceof Double) {
            return String.format("%s = ?", fieldName);
        } else if (value instanceof OffsetDateTime) {
            return String.format("%s = ?", fieldName);
        } else {
            throw new SKException("Unsupported filter value type '"
                + value.getClass().getSimpleName() + "'.");
        }
    }

    @Override
    public String getAnyTagEqualToFilter(AnyTagEqualToFilterClause filterClause) {
        String fieldName = JDBCVectorStoreQueryProvider
            .validateSQLidentifier(filterClause.getFieldName());

        return String.format("%s LIKE ?", fieldName);
    }

    @Override
    public <Record> VectorStoreRecordMapper<Record, ResultSet> getVectorStoreRecordMapper(
        Class<Record> recordClass,
        VectorStoreRecordDefinition recordDefinition) {
        return JDBCVectorStoreRecordMapper.<Record>builder()
            .withRecordClass(recordClass)
            .withVectorStoreRecordDefinition(recordDefinition)
            .build();
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
         *
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
         *
         * @param collectionsTable the collections table name
         * @return the builder
         */
        public Builder withCollectionsTable(String collectionsTable) {
            this.collectionsTable = validateSQLidentifier(collectionsTable);
            return this;
        }

        /**
         * Sets the prefix for collection tables.
         *
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
