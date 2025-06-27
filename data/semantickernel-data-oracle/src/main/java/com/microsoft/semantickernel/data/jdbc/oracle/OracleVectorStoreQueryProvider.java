/*
 ** Semantic Kernel Oracle connector version 1.0.
 **
 ** Copyright (c) 2025 Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */
package com.microsoft.semantickernel.data.jdbc.oracle;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.microsoft.semantickernel.data.filter.AnyTagEqualToFilterClause;
import com.microsoft.semantickernel.data.filter.EqualToFilterClause;
import com.microsoft.semantickernel.data.jdbc.*;
import com.microsoft.semantickernel.data.vectorsearch.VectorSearchFilter;
import com.microsoft.semantickernel.data.vectorsearch.VectorSearchResult;
import com.microsoft.semantickernel.data.vectorsearch.VectorSearchResults;
import com.microsoft.semantickernel.data.vectorstorage.VectorStoreRecordMapper;
import com.microsoft.semantickernel.data.vectorstorage.definition.DistanceFunction;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordDataField;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordDefinition;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordField;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordVectorField;
import com.microsoft.semantickernel.data.vectorstorage.options.GetRecordOptions;
import com.microsoft.semantickernel.data.vectorstorage.options.UpsertRecordOptions;
import com.microsoft.semantickernel.data.vectorstorage.options.VectorSearchOptions;
import com.microsoft.semantickernel.exceptions.SKException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import oracle.jdbc.OraclePreparedStatement;
import oracle.jdbc.OracleStatement;
import oracle.jdbc.OracleTypes;
import oracle.sql.TIMESTAMPTZ;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;
import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * JDBC Vector Store for the Oracle Database
 */
public class OracleVectorStoreQueryProvider extends JDBCVectorStoreQueryProvider {

    // This could be removed if super.collectionTable made protected
    private final String collectionsTable;

    // This could be common to all query providers
    private final ObjectMapper objectMapper;

    /**
     * Lock used to ensure that only one thread can create a collection at a time.
     */
    private static final ReentrantLock dbCreationLock = new ReentrantLock();

    /**
     * The logger
     */
    private static final Logger LOGGER = Logger.getLogger(OracleVectorStoreQueryProvider.class.getName());

    public enum StringTypeMapping {
        /**
         * Maps String to CLOB
         */
        USE_CLOB,
        /**
         * Maps String to VARCHAR2(4000)
         */
        USE_VARCHAR
    }

    /**
     * Create an instance of OracleVectorStoreQueryProvider.
     *
     * @param dataSource the datasource
     * @param collectionsTable the collections table name
     * @param prefixForCollectionTables the prefix for the collection table name
     * @param defaultVarcharSize the size of VARCHAR columns
     * @param stringTypeMapping the storage type of string columns (VARCHAR or CLOB)
     * @param objectMapper the object mapper.
     */
    private OracleVectorStoreQueryProvider(
        @Nonnull DataSource dataSource,
        @Nonnull String collectionsTable,
        @Nonnull String prefixForCollectionTables,
        int defaultVarcharSize,
        @Nonnull StringTypeMapping stringTypeMapping,
        ObjectMapper objectMapper) {
        super(
            dataSource,
            collectionsTable,
            prefixForCollectionTables,
            OracleVectorStoreFieldHelper.getSupportedKeyTypes(),
            OracleVectorStoreFieldHelper.getSupportedDataTypes(stringTypeMapping, defaultVarcharSize),
            OracleVectorStoreFieldHelper.getSupportedVectorTypes());
        this.collectionsTable = collectionsTable;
        this.objectMapper = objectMapper;
        // The JavaTimeModule must be registered to handle OffsetDateTime. To make sure that it is
        // registered enable the feature IGNORE_DUPLICATE_MODULE_REGISTRATIONS and register the
        // module.
        this.objectMapper.enable(MapperFeature.IGNORE_DUPLICATE_MODULE_REGISTRATIONS);
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * <p>
     *  Creates a collection with the given name and record definition.
     * </p><p>
     *  A collection is represented as a table in an Oracle DB containing columns
     *  that match the record definition. The table name is the name of the collection
     *  prefixed by the provided collection prefix. If no prefix was provided the default
     *  prefix will be used.
     * </p>
     * @param collectionName   the name of the collection
     * @param recordDefinition the record definition
     */
    @Override
    @SuppressFBWarnings("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
    @GuardedBy("dbCreationLock")
    public void createCollection(String collectionName,
        VectorStoreRecordDefinition recordDefinition) {

        dbCreationLock.lock();
        try {

            List<VectorStoreRecordVectorField> vectorFields = recordDefinition.getVectorFields();
            String createStorageTable = formatQuery("CREATE TABLE IF NOT EXISTS %s ("
                    + "%s PRIMARY KEY, "
                    + "%s, "
                    + "%s)",
                getCollectionTableName(collectionName),
                OracleVectorStoreFieldHelper.getKeyColumnNameAndType(recordDefinition.getKeyField()),
                getColumnNamesAndTypes(new ArrayList<>(recordDefinition.getDataFields()),
                    getSupportedDataTypes()),
                OracleVectorStoreFieldHelper.getVectorColumnNamesAndTypes(
                    new ArrayList<>(vectorFields)));

            String insertCollectionQuery = this.getInsertCollectionQuery(collectionsTable);

            try (Connection connection = dataSource.getConnection()) {
                // set auto commit of, either all statements should be executed or none
                connection.setAutoCommit(false);
                try (Statement statement = connection.createStatement()) {
                    // Create table
                    statement.addBatch(createStorageTable);
                    LOGGER.finest("Creating collection " + collectionName +
                        " using statement: " + createStorageTable);

                    // Index filterable data columns
                    for (VectorStoreRecordDataField dataField : recordDefinition.getDataFields()) {
                        if (dataField.isFilterable()) {
                            String dataFieldIndex = OracleVectorStoreFieldHelper.createIndexForDataField(
                                getCollectionTableName(collectionName), dataField, supportedDataTypes);
                            statement.addBatch(dataFieldIndex);
                            LOGGER.finest("Creating index on column "
                                + dataField.getEffectiveStorageName() + " using the statement: "
                                + dataFieldIndex);
                        }
                    }

                    // Create index for vectorFields
                    for (VectorStoreRecordVectorField vectorField : vectorFields) {
                        String createVectorIndex = OracleVectorStoreFieldHelper.getCreateVectorIndexStatement(
                            vectorField, getCollectionTableName(collectionName));
                        if (createVectorIndex != null) {
                            statement.addBatch(createVectorIndex);
                            LOGGER.finest("Creating index on vector column "
                                + vectorField.getEffectiveStorageName() + " using the statement: "
                                + createVectorIndex);

                        }
                    }
                    statement.executeBatch();

                    // Insert the collection to the store (collections table) using MERGE statement
                    try (PreparedStatement insert = connection.prepareStatement(
                        insertCollectionQuery)) {
                        insert.setString(1, collectionName);
                        insert.execute();
                        LOGGER.finest("Inserting collection to store using statement: " +
                            insertCollectionQuery);
                    }

                    connection.commit();
                } catch (SQLException e) {
                    connection.rollback();
                    throw new SKException("Failed to create collection", e);
                }
            } catch (SQLException e) {
                throw new SKException("Failed to create collection", e);
            }
        } finally {
            dbCreationLock.unlock();
        }
    }

    /**
     * <p>
     * Inserts or updates record of a collection given the collection name, the records, the record
     * definition and the upsert options.
     * </p><p>
     * @Note At the moment {@link UpsertRecordOptions} is an empty class. No options are available.
     * </p>
     *
     * @param collectionName the collection name
     * @param records the records to update or insert
     * @param recordDefinition the record definition
     * @param options the options
     */
    @Override
    public void upsertRecords(String collectionName,
        List<?> records,
        VectorStoreRecordDefinition recordDefinition,
        UpsertRecordOptions options) {

        final String NEW_VALUE = "new";
        final String EXISTING_VALUE = "existing";

        // generate the comma separated list of new fields
        // Ex.: new.field1, new.field2 ... new.fieldn
        String insertNewFieldList = recordDefinition.getAllFields().stream()
            .map(f ->  NEW_VALUE + "." + f.getEffectiveStorageName())
            .collect(Collectors.joining(", "));

        // generate the comma separated list of existing fields
        // Ex.: existing.field1, existing.field2 ... existing.fieldn
        String insertExistingFieldList = recordDefinition.getAllFields().stream()
            .map(f ->  EXISTING_VALUE + "." + f.getEffectiveStorageName())
            .collect(Collectors.joining(", "));

        // generate the comma separated list for setting new values on fields
        // Ex.: new.field1 = existing.field1, new.field2 = existing.field2 ... new.fieldn = existing.fieldn
        String updateFieldList = recordDefinition.getAllFields().stream()
            .filter(f -> f != recordDefinition.getKeyField())
            .map(f -> EXISTING_VALUE + "." + f.getEffectiveStorageName() + " = " + NEW_VALUE + "." + f.getEffectiveStorageName())
            .collect(Collectors.joining(", "));

        // generate the comma separated list of placeholders "?" for each field
        // Ex.: ? field1, ? field2 ... ? fieldn
        String namedPlaceholders = recordDefinition.getAllFields().stream().map(f -> "? " + f.getEffectiveStorageName())
            .collect(Collectors.joining(", "));

        // Generate the MERGE statement to perform the upsert.
        String upsertStatement = formatQuery("MERGE INTO %s existing "+
                "USING (SELECT %s FROM DUAL) new ON (existing.%s = new.%s) " +
                "WHEN MATCHED THEN UPDATE SET %s " +
                "WHEN NOT MATCHED THEN INSERT (%s) VALUES (%s)",
            getCollectionTableName(collectionName),
            namedPlaceholders,
            getKeyColumnName(recordDefinition.getKeyField()),
            getKeyColumnName(recordDefinition.getKeyField()),
            updateFieldList,
            insertExistingFieldList,
            insertNewFieldList);

        LOGGER.finest("Generated upsert statement: " + upsertStatement);
        try (Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(upsertStatement)) {
            // Loop through records, set values and add values to batch
            for (Object record : records) {
                setUpsertStatementValues(statement, record, recordDefinition.getAllFields());
                statement.addBatch();
            }

            // Execute the upsert statement
            statement.executeBatch();
        } catch (SQLException e) {
            throw new SKException("Failed to upsert records", e);
        }
    }

    /**
     * Generates the MERGE statement to add the given collection to the store.
     *
     * @param collectionsTable the name of the DB table containing all collections.
     * @return a SQL statement that inserts a collection to the store if it does not exist.
     */
    @Override
    protected String getInsertCollectionQuery(String collectionsTable) {
        return formatQuery(
            "MERGE INTO %s existing "+
                "USING (SELECT ? AS collectionId FROM DUAL) new ON (existing.collectionId = new.collectionId) " +
                "WHEN NOT MATCHED THEN INSERT (existing.collectionId) VALUES (new.collectionId)",
            collectionsTable);
    }

    /**
     * The {@link OracleVectorStoreQueryProvider#upsertRecords(String, List, VectorStoreRecordDefinition, UpsertRecordOptions)}
     * method adds a placeholder for each field. This method sets the value of each field on the
     * MERGE statement with the value of the record. The placeholder and values are set in the order
     * of the fields in the list.
     *
     * @param upsertStatement the MERGE statement
     * @param record the record containing the values
     * @param fields the list of fields.
     */
    private void setUpsertStatementValues(PreparedStatement upsertStatement, Object record,
        List<VectorStoreRecordField> fields) {

        // use the object mapper to convert the record to an equivalent tree mode JsonNode value,
        // this allows to retrieve the values using the effective storage name of the fields and
        // avoids the use of introspection.
        JsonNode jsonNode = objectMapper.valueToTree(record);

        for (int i = 0; i < fields.size(); ++i) {
            VectorStoreRecordField field = fields.get(i);
            try {

                JsonNode valueNode = jsonNode.get(field.getEffectiveStorageName());

                // Some field types require special treatment to convert the java type to the
                // DB type
                if (field instanceof VectorStoreRecordVectorField) {

                    // Convert the vector field to a string
                    if (field.getFieldType().equals(String.class)) {
                        String json = (valueNode == null || valueNode.isNull())
                            ? null
                            : valueNode.asText();
                        double[] values = (json == null)
                            ? null
                            : objectMapper.readValue(json, double[].class);

                        int dim = ((VectorStoreRecordVectorField) field).getDimensions();
                        if (values != null && values.length != dim) {
                            throw new SKException("Vector dimension mismatch: expected " + dim);
                        }

                        upsertStatement.setObject(i + 1, values, OracleTypes.VECTOR_FLOAT32);
                        continue;
                    }

                    // If the vector field is not set as a string convert to an array of doubles
                    // and set the value
                    if (!field.getFieldType().equals(String.class)) {
                        double[] values = (valueNode == null || valueNode.isNull())
                            ? null
                            : StreamSupport.stream((
                                (ArrayNode)valueNode).spliterator(), false)
                                .mapToDouble(d -> d.asDouble()).toArray();

                        upsertStatement.setObject(i + 1, values, OracleTypes.VECTOR_FLOAT32);
                        continue;
                    }
                } else if (field instanceof VectorStoreRecordDataField) {
                    // Lists are stored as JSON objects, write the list as a JSON string representation
                    // of the list.
                    if (field.getFieldType().equals(List.class)) {
                        upsertStatement.setObject(i + 1, objectMapper.writeValueAsString(valueNode));
                        continue;
                    }
                    // Convert UUID to string before setting the value.
                    if (field.getFieldType().equals(UUID.class)) {
                        upsertStatement.setObject(i + 1, valueNode.isNull() ? null : valueNode.asText());
                        continue;
                    }
                    // Convert OffsetDateTime to TIMESTAMPTZ  before setting the value.
                    if (field.getFieldType().equals(OffsetDateTime.class)) {
                        if (valueNode == null || valueNode.isNull()) {
                            upsertStatement.setNull(i + 1, OracleTypes.TIMESTAMPTZ);
                        } else {
                            OffsetDateTime offsetDateTime = (OffsetDateTime) objectMapper.convertValue(valueNode, field.getFieldType());
                            ((OraclePreparedStatement) upsertStatement).setTIMESTAMPTZ(i + 1,
                                TIMESTAMPTZ.of(offsetDateTime));
                        }
                        continue;
                    }
                }

                // For all other field type use setObject with the field value
                upsertStatement.setObject(i + 1,
                    objectMapper.convertValue(valueNode,field.getFieldType()));
            } catch (SQLException | JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * <p>
     * Executes a vector search query, using the search options and returns the results. The results
     * are mapped to the specified record type using the provided mapper. The query is executed
     * against the specified collection.
     * </p><p>
     *
     * </p>
     * @param collectionName   the collection name
     * @param vector           the vector to search with
     * @param options          the search options
     * @param recordDefinition the record definition
     * @param mapper           the mapper, responsible for mapping the result set to the record
     *                         type.
     * @return the search results
     * @param <Record> the record type
     */
    @Override
    public <Record> VectorSearchResults<Record> search(String collectionName, List<Float> vector,
        VectorSearchOptions options, VectorStoreRecordDefinition recordDefinition,
        VectorStoreRecordMapper<Record, ResultSet> mapper) {

        // Gets the search vector field and its distance function. If not vector field was provided,
        // use the first one
        VectorStoreRecordVectorField vectorField = options.getVectorFieldName() == null
            ? recordDefinition.getVectorFields().get(0)
            : (VectorStoreRecordVectorField) recordDefinition
                .getField(options.getVectorFieldName());
        DistanceFunction distanceFunction = vectorField == null ? null : vectorField.getDistanceFunction();
        if (options.getVectorFieldName() != null && vectorField == null) {
            throw new SKException("");
        }

        // get list of fields that should be returned by the query
        List<VectorStoreRecordField> fields = (options.isIncludeVectors())
            ? recordDefinition.getAllFields()
            : recordDefinition.getNonVectorFields();

        // get search filters and get the list of parameters for the filters
        String filter = getFilter(options.getVectorSearchFilter(), recordDefinition);
        List<Object> parameters = getFilterParameters(options.getVectorSearchFilter());

        // generate SQL statement
        String selectQuery = "SELECT "
            + (vector == null ? "0 as distance, " :
                formatQuery("VECTOR_DISTANCE(%s, ?, %s) distance, ", vectorField.getEffectiveStorageName(), toOracleDistanceFunction(distanceFunction)))
            + getQueryColumnsFromFields(fields)
            + " FROM " + getCollectionTableName(collectionName)
            + (filter != null && !filter.isEmpty() ? " WHERE " + filter : "")
            + " ORDER BY distance"
            + (options.getSkip() > 0 ? " OFFSET " + options.getSkip() + " ROWS" : "")
            + (options.getTop() > 0 ? " FETCH " + (options.getSkip() > 0 ? "NEXT " : "FIRST ") + options.getTop() + " ROWS ONLY" : "");
        LOGGER.finest("Search using statement: " + selectQuery);

        // Execute the statement
        List<VectorSearchResult<Record>> records = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(selectQuery)) {
            // set parameters from filters
            int parameterIndex = 1;
            // if a vector was provided for similarity search set the value of the vector
            if (vector != null) {
                statement.setString(parameterIndex++,
                    objectMapper.writeValueAsString(vector));
            }
            // set all parameters.
            for (Object parameter : parameters) {
                if (parameter != null) {
                    setSearchParameter(statement, parameterIndex++, parameter.getClass(), parameter);
                }
            }

            // Calls to defineColumnType reduce the number of network requests. When Oracle JDBC knows that it is
            // fetching VECTOR, CLOB, and/or JSON columns, the first request it sends to the database can include a LOB
            // prefetch size (VECTOR and JSON are value-based-lobs). If defineColumnType is not called, then JDBC needs
            // to send an additional request with the LOB prefetch size, after the first request has the database
            // respond with the column data types. To request all data, the prefetch size is Integer.MAX_VALUE.
            OracleStatement oracleStatement = statement.unwrap(OracleStatement.class);
            int columnIndex = 1;
            // define distance column as double
            defineDataColumnType(columnIndex++, oracleStatement, Double.class);
            // define columns for returned fiels
            for (VectorStoreRecordField field : fields) {
                if (!(field instanceof VectorStoreRecordVectorField))
                    defineDataColumnType(columnIndex++, oracleStatement, field.getFieldType());
                else
                    oracleStatement.defineColumnType(columnIndex++, OracleTypes.VECTOR_FLOAT32,
                        Integer.MAX_VALUE);
            }
            oracleStatement.setLobPrefetchSize(Integer.MAX_VALUE); // Workaround for Oracle JDBC bug 37030121

            // Execute the statement and get the results
            try (ResultSet rs = statement.executeQuery()) {
                GetRecordOptions getRecordOptions = new GetRecordOptions(options.isIncludeVectors());
                while (rs.next()) {
                    // Cosine distance function. 1 - cosine similarity.
                    double score = Math.abs(rs.getDouble("distance"));
                    if (distanceFunction == DistanceFunction.COSINE_SIMILARITY) {
                        score = 1d - score;
                    }
                    // Use the mapper to convert to result set to records
                    records.add(new VectorSearchResult<>(mapper.mapStorageModelToRecord(rs, getRecordOptions), score));
                }
            }
        } catch (SQLException | JsonProcessingException e) {
            throw  new SKException("Search failed", e);
        }

        return new VectorSearchResults<>(records);
    }

    /**
     * Sets the parameter value
     * @param statement the statement
     * @param index the parameter index
     * @param type the parameter type
     * @param value the value
     */
    private void setSearchParameter(PreparedStatement statement, int index, Class<?> type, Object value) {

        try {
            // Use JSON string to set lists
            if (List.class.equals(type)) {
                statement.setObject(index, objectMapper.writeValueAsString(value));
                System.out.println(
                    "Set values: " + objectMapper.writeValueAsString(value));
                return;
            }
            // convert UUID to string
            if (UUID.class.equals(type)) {
                statement.setString(index, value.toString());
                return;
            }
            // convert OffsetDateType to TIMESTAMPTZ
            if (OffsetDateTime.class.equals(type)) {
                if (value == null) {
                    statement.setNull(index, OracleTypes.TIMESTAMPTZ);
                } else {
                    OffsetDateTime offsetDateTime = (OffsetDateTime) value;
                    ((OraclePreparedStatement) statement).setTIMESTAMPTZ(index,
                        TIMESTAMPTZ.of(offsetDateTime));
                    System.out.println("Set values: " + offsetDateTime);
                }
                return;
            }
            // use setBigDecimal to set BigDecimal value
            if (BigDecimal.class.equals(type)) {
                if (value == null) {
                    statement.setNull(index, OracleTypes.DECIMAL);
                } else {
                    BigDecimal bigDecimal = (BigDecimal) value;
                    ((OraclePreparedStatement) statement).setBigDecimal(index,
                        bigDecimal);
                    System.out.println("Set values: " + bigDecimal);
                }
                return;
            }

            // for all other types set object with the given value
            statement.setObject(index, value);

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }


    /**
     * Defines the type that will be used to retrieve data from a given database table column.
     * @param columnIndex the index of the column
     * @param statement the statement
     * @param fieldType the java field type
     * @throws SQLException if an error occurs while defining the column type
     */
    private void defineDataColumnType(int columnIndex, OracleStatement statement, Class<?> fieldType) throws SQLException {
        // swich between supported classes and define the column type on the statement
        switch (supportedDataTypes.get(fieldType)) {
            case OracleDataTypesMapping.STRING_CLOB:
                statement.defineColumnType(columnIndex, OracleTypes.CLOB, Integer.MAX_VALUE);
                break;
            case OracleDataTypesMapping.BYTE:
                statement.defineColumnType(columnIndex, OracleTypes.NUMBER);
                break;
            case OracleDataTypesMapping.SHORT:
                statement.defineColumnType(columnIndex, OracleTypes.NUMBER);
                break;
            case OracleDataTypesMapping.INTEGER:
                statement.defineColumnType(columnIndex, OracleTypes.INTEGER);
                break;
            case OracleDataTypesMapping.LONG:
                statement.defineColumnType(columnIndex, OracleTypes.BIGINT);
                break;
            case OracleDataTypesMapping.FLOAT:
                statement.defineColumnType(columnIndex, OracleTypes.BINARY_FLOAT);
                break;
            case OracleDataTypesMapping.DOUBLE:
                statement.defineColumnType(columnIndex, OracleTypes.BINARY_DOUBLE);
                break;
            case OracleDataTypesMapping.DECIMAL:
                statement.defineColumnType(columnIndex, OracleTypes.BINARY_DOUBLE);
                break;
            case OracleDataTypesMapping.BOOLEAN:
                statement.defineColumnType(columnIndex, OracleTypes.BOOLEAN);
                break;
            case OracleDataTypesMapping.OFFSET_DATE_TIME:
                statement.defineColumnType(columnIndex, OracleTypes.TIMESTAMPTZ);
                break;
            case OracleDataTypesMapping.JSON:
                statement.defineColumnType(columnIndex, OracleTypes.JSON, Integer.MAX_VALUE);
                break;
            case OracleDataTypesMapping.BYTE_ARRAY:
                statement.defineColumnType(columnIndex, OracleTypes.RAW);
            default:
                statement.defineColumnType(columnIndex, OracleTypes.VARCHAR);
        }
    }

    /**
     * Converts a {@link DistanceFunction} to the equivalent Oracle distance function.
     * @param distanceFunction the distance function
     * @return the Oracle distance function
     */
    private String toOracleDistanceFunction(DistanceFunction distanceFunction) {
        switch (distanceFunction) {
            case DOT_PRODUCT:
                return "DOT";
            case COSINE_SIMILARITY:
            case COSINE_DISTANCE:
                return "COSINE";
            case EUCLIDEAN_DISTANCE:
                return "EUCLIDEAN";
            default:
                return "COSINE";
        }
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
        // TODO: this method should be protected, not public
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
                return anyTagEqualToFilterClause.getValue();
            } else {
                throw new SKException("Unsupported filter clause type '"
                    + filterClause.getClass().getSimpleName() + "'.");
            }
        }).collect(Collectors.toList());
    }

    /**
     * Gets the filter clause for an equal to filter
     * @param filterClause The equal to filter clause to get the filter string for.
     * @return the filter clause
     */
    @Override
    public String getEqualToFilter(EqualToFilterClause filterClause) {
        String fieldName = JDBCVectorStoreQueryProvider
            .validateSQLidentifier(filterClause.getFieldName());
        Object value = filterClause.getValue();

        if (value == null) {
            return String.format("%s is NULL", fieldName);
        } else {
            return String.format("%s = ?", fieldName);
        }
    }

    /**
     * Gets the filter clause for an any tag equal to filter
     * @param filterClause The any tag equal to filter clause to get the filter string for.
     * @return the filter clause
     */
    @Override
    public String getAnyTagEqualToFilter(AnyTagEqualToFilterClause filterClause) {
        String fieldName = JDBCVectorStoreQueryProvider
                .validateSQLidentifier(filterClause.getFieldName());

        return String.format("JSON_EXISTS(%s, '$[*]?(@ == $v_%s)' PASSING ? AS \"v_%s\")",
            fieldName, fieldName, fieldName);
    }

    /**
     * Gets the mapper used to map a ResultSet to records
     * @param recordClass      the record class
     * @param vectorStoreRecordDefinition the record definition
     * @return the vector store record mapper
     * @param <Record> the type of the records
     */
    @Override
    public <Record> VectorStoreRecordMapper<Record, ResultSet> getVectorStoreRecordMapper(
        Class<Record> recordClass,
        VectorStoreRecordDefinition vectorStoreRecordDefinition) {
        return OracleVectorStoreRecordMapper.<Record>builder()
            .withRecordClass(recordClass)
            .withVectorStoreRecordDefinition(vectorStoreRecordDefinition)
            .withSupportedDataTypesMapping(getSupportedDataTypes())
            .build();
    }

    /**
     * Gets a builder that allows to build an OracleVectorStoreQueryProvider
     * @return the builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * OracleVectorStoreQueryProvider builder.
     */
    public static class Builder
        extends JDBCVectorStoreQueryProvider.Builder {

        /**
         * The data source
         */
        private DataSource dataSource;

        /**
         * The collections table
         */
        private String collectionsTable = DEFAULT_COLLECTIONS_TABLE;

        /**
         * The prefix for collection table names
         */
        private String prefixForCollectionTables = DEFAULT_PREFIX_FOR_COLLECTION_TABLES;

        /**
         * The object mapper
         */
        private ObjectMapper objectMapper = new ObjectMapper();

        /**
         * The string type mapping choice
         */
        private StringTypeMapping stringTypeMapping = StringTypeMapping.USE_VARCHAR;

        /**
         * The size of varchar columns
         */
        private int defaultVarcharSize = 2000;


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

        /**
         * Sets the object mapper used to map records to and from results
         * @param objectMapper the object mapper
         * @return the builder
         */
        public Builder withObjectMapper(
            ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
            return this;
        }

        /**
         * Sets the desired String type mapping.
         * @param stringTypeMapping the desired String type mapping. The default value is
         *                          {@link StringTypeMapping#USE_VARCHAR}
         * @return the builder
         */
        public Builder withStringTypeMapping (StringTypeMapping stringTypeMapping) {
            this.stringTypeMapping = stringTypeMapping;
            return this;
        }

        /**
         * Sets the default size of the VARHCHAR fields.
         * @param defaultVarcharSize the default size of the VARHCHAR fields. By default, the size
         *                           is 2000.
         * @return then builder
         */
        public Builder withDefaultVarcharSize (int defaultVarcharSize) {
            this.defaultVarcharSize = defaultVarcharSize;
            return this;
        }

        /**
         * Builds and Oracle vector store query provider.
         * @return the query provider
         */
        @Override
        public OracleVectorStoreQueryProvider build() {
            return new OracleVectorStoreQueryProvider(dataSource, collectionsTable,
                prefixForCollectionTables, defaultVarcharSize, stringTypeMapping, objectMapper);
        }
    }
}

