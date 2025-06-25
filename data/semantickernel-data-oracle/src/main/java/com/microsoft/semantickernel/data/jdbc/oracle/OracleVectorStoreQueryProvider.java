package com.microsoft.semantickernel.data.jdbc.oracle;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.fasterxml.jackson.databind.util.StdDateFormat;
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
import oracle.sql.TIMESTAMPLTZ;
import oracle.sql.TIMESTAMPTZ;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;
import javax.sql.DataSource;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLType;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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

    private final Map<Class<?>, String> annotatedTypeMapping;

    private static final Object dbCreationLock = new Object();

    private static final Logger logger = Logger.getLogger(OracleVectorStoreQueryProvider.class.getName());

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
     * Constructor
     * @param dataSource the datasiyrce
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
        ObjectMapper objectMapper,
        Map<Class<?>, String> annotatedTypeMapping) {
        super(
            dataSource,
            collectionsTable,
            prefixForCollectionTables,
            OracleVectorStoreFieldHelper.getSupportedKeyTypes(),
            OracleVectorStoreFieldHelper.getSupportedDataTypes(stringTypeMapping, defaultVarcharSize),
            OracleVectorStoreFieldHelper.getSupportedVectorTypes());
        this.collectionsTable = collectionsTable;
        this.objectMapper = objectMapper;
        this.objectMapper.registerModule(new JavaTimeModule());
        this.annotatedTypeMapping = annotatedTypeMapping;
    }

    @Override
    @SuppressFBWarnings("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
    @GuardedBy("dbCreationLock")
    public void createCollection(String collectionName,
        VectorStoreRecordDefinition recordDefinition) {

        synchronized (dbCreationLock) {

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
                connection.createStatement().execute(formatQuery("DROP TABLE IF EXISTS %s", getCollectionTableName(collectionName)));
                connection.setAutoCommit(false);
                try (Statement statement = connection.createStatement()) {
                    // Create table
                    System.out.println(createStorageTable);
                    statement.addBatch(createStorageTable);

                    // Index filterable columns
                    for (VectorStoreRecordDataField dataField : recordDefinition.getDataFields()) {
                        if (dataField.isFilterable()) {
                            String dataFieldIndex = OracleVectorStoreFieldHelper.createIndexForDataField(
                                getCollectionTableName(collectionName), dataField, supportedDataTypes);
                            System.out.println(dataFieldIndex);
                            statement.addBatch(dataFieldIndex);
                        }
                    }

                    // Create indexed for vectorFields
                    for (VectorStoreRecordVectorField vectorField : vectorFields) {
                        String createVectorIndex = OracleVectorStoreFieldHelper.getCreateVectorIndexStatement(
                            vectorField, getCollectionTableName(collectionName));
                        if (createVectorIndex != null) {
                            System.out.println(createVectorIndex);
                            statement.addBatch(createVectorIndex);
                        }
                    }
                    statement.executeBatch();

                    try (PreparedStatement insert = connection.prepareStatement(
                        insertCollectionQuery)) {
                        System.out.println(insertCollectionQuery);
                        insert.setString(1, collectionName);
                        insert.execute();
                    }

                    connection.commit();
                } catch (SQLException e) {
                    connection.rollback();
                    throw new SKException("Failed to create collection", e);
                }
            } catch (SQLException e) {
                throw new SKException("Failed to create collection", e);
            }
        }
    }

    @Override
    protected String getInsertCollectionQuery(String collectionsTable) {
        return formatQuery(
            "MERGE INTO %s existing "+
                "USING (SELECT ? AS collectionId FROM DUAL) new ON (existing.collectionId = new.collectionId) " +
                "WHEN NOT MATCHED THEN INSERT (existing.collectionId) VALUES (new.collectionId)",
            collectionsTable);
    }

    @Override
    public void upsertRecords(String collectionName, List<?> records, VectorStoreRecordDefinition recordDefinition, UpsertRecordOptions options) {

        final String NEW_VALUE = "new";
        final String EXISTING_VALUE = "existing";

        String insertNewFieldList = recordDefinition.getAllFields().stream()
            .map(f ->  NEW_VALUE + "." + f.getEffectiveStorageName())
            .collect(Collectors.joining(", "));

        String insertExistingFieldList = recordDefinition.getAllFields().stream()
            .map(f ->  EXISTING_VALUE + "." + f.getEffectiveStorageName())
            .collect(Collectors.joining(", "));

        String updateFieldList = recordDefinition.getAllFields().stream()
            .filter(f -> f != recordDefinition.getKeyField())
            .map(f -> EXISTING_VALUE + "." + f.getEffectiveStorageName() + " = " + NEW_VALUE + "." + f.getEffectiveStorageName())
            .collect(Collectors.joining(", "));

        String namedWildcard = recordDefinition.getAllFields().stream().map(f -> "? " + f.getEffectiveStorageName())
            .collect(Collectors.joining(", "));

        String upsertQuery = formatQuery("MERGE INTO %s existing "+
                "USING (SELECT %s FROM DUAL) new ON (existing.%s = new.%s) " +
                "WHEN MATCHED THEN UPDATE SET %s " +
                "WHEN NOT MATCHED THEN INSERT (%s) VALUES (%s)",
            getCollectionTableName(collectionName),
            namedWildcard,
            getKeyColumnName(recordDefinition.getKeyField()),
            getKeyColumnName(recordDefinition.getKeyField()),
            updateFieldList,
            insertExistingFieldList,
            insertNewFieldList);

        System.out.println(upsertQuery);
        try (Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(upsertQuery)) {
            for (Object record : records) {
                setUpsertStatementValues(statement, record, recordDefinition.getAllFields());
                statement.addBatch();
            }

            statement.executeBatch();
        } catch (SQLException e) {
            throw new SKException("Failed to upsert records", e);
        }
    }

    private void setUpsertStatementValues(PreparedStatement statement, Object record,
        List<VectorStoreRecordField> fields) {
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        String datePattern = "yyyy-MM-dd HH:mm:ss.SSSZ";
        SimpleDateFormat df = new SimpleDateFormat(datePattern);
        objectMapper.setDateFormat(df);
        JsonNode jsonNode = objectMapper.valueToTree(record);

        for (int i = 0; i < fields.size(); ++i) {
            VectorStoreRecordField field = fields.get(i);
            try {
                JsonNode valueNode = jsonNode.get(field.getEffectiveStorageName());

                if (field instanceof VectorStoreRecordVectorField) {
                    // Convert the vector field to a string
                    if (!field.getFieldType().equals(String.class)) {
                        double[] values = (valueNode == null || valueNode.isNull())
                            ? null
                            : StreamSupport.stream((
                                (ArrayNode)valueNode).spliterator(), false)
                                .mapToDouble(d -> d.asDouble()).toArray();
                        statement.setObject(i + 1, values,
                            OracleVectorStoreFieldHelper.getOracleTypeForVectorField((VectorStoreRecordVectorField)field));
                        System.out.println("Set values: " + values);
                        continue;
                    }
                } else if (field instanceof VectorStoreRecordDataField) {
                    // Convert List field to a string
                    if (field.getFieldType().equals(List.class)) {
                        statement.setObject(i + 1, objectMapper.writeValueAsString(valueNode));
                        System.out.println(
                            "Set values: " + objectMapper.writeValueAsString(valueNode));
                        continue;
                    }
                    if (OracleVectorStoreFieldHelper.isUUID(field)) {
                        if (valueNode == null || valueNode.isNull()) {
                            statement.setNull(i + 1, OracleTypes.RAW);
                        } else {
                            UUID uuid = UUID.fromString(valueNode.textValue());
                            ByteBuffer bb = ByteBuffer.allocate(16);
                            bb.putLong(uuid.getMostSignificantBits());
                            bb.putLong(uuid.getLeastSignificantBits());
                            statement.setBytes(i + 1, bb.array());
                            System.out.println("Set values: " + objectMapper.convertValue(valueNode,
                                field.getFieldType()));
                        }
                        continue;
                    }
                    if (field.getFieldType().equals(OffsetDateTime.class)) {
                        if (valueNode == null || valueNode.isNull()) {
                            statement.setNull(i + 1, OracleTypes.TIMESTAMPTZ);
                        } else {
                            OffsetDateTime offsetDateTime = OffsetDateTime.parse(
                                valueNode.asText());
                            ((OraclePreparedStatement) statement).setTIMESTAMPTZ(i + 1,
                                TIMESTAMPTZ.of(offsetDateTime));
                            System.out.println("Set values: " + objectMapper.convertValue(valueNode,
                                field.getFieldType()));
                        }
                        continue;
                    }
                }

                statement.setObject(i + 1,
                    objectMapper.convertValue(valueNode,field.getFieldType()));
                System.out.println("Set values: " + objectMapper.convertValue(valueNode, field.getFieldType()));
            } catch (SQLException | JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public <Record> VectorSearchResults<Record> search(String collectionName, List<Float> vector,
        VectorSearchOptions options, VectorStoreRecordDefinition recordDefinition,
        VectorStoreRecordMapper<Record, ResultSet> mapper) {

        VectorStoreRecordVectorField firstVectorField = recordDefinition.getVectorFields()
            .get(0);
        VectorStoreRecordVectorField vectorField = options.getVectorFieldName() == null
            ? firstVectorField
            : (VectorStoreRecordVectorField) recordDefinition
                .getField(options.getVectorFieldName());
        DistanceFunction distanceFunction = vectorField.getDistanceFunction();

        List<VectorStoreRecordField> fields;
        if (options.isIncludeVectors()) {
            fields = recordDefinition.getAllFields();
        } else {
            fields = recordDefinition.getNonVectorFields();
        }

        String filter = getFilter(options.getVectorSearchFilter(), recordDefinition);
        List<Object> parameters = getFilterParameters(options.getVectorSearchFilter());

        String selectQuery = "SELECT "
            + (vector == null ? "0 as distance, " :
                formatQuery("VECTOR_DISTANCE(%s, ?, %s) distance, ", vectorField.getEffectiveStorageName(), toOracleDistanceFunction(distanceFunction)))
            + getQueryColumnsFromFields(fields)
            + " FROM " + getCollectionTableName(collectionName)
            + (filter != null && !filter.isEmpty() ? " WHERE " + filter : "")
            + " ORDER BY distance"
            + (options.getSkip() > 0 ? " OFFSET " + options.getSkip() + " ROWS" : "")
            + (options.getTop() > 0 ? " FETCH " + (options.getSkip() > 0 ? "NEXT " : "FIRST ") + options.getTop() + " ROWS ONLY" : "");

        System.out.println(selectQuery);
        List<VectorSearchResult<Record>> records = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(selectQuery)) {
            // set parameters from filters
            int parameterIndex = 1;
            if (vector != null) {
                System.out.println("Set vector parameter with index " + parameterIndex + " to: "
                    + objectMapper.writeValueAsString(vector));
                statement.setString(parameterIndex++,
                    objectMapper.writeValueAsString(vector));
            }
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
            defineDataColumnType(columnIndex++, oracleStatement, Double.class);
            for (VectorStoreRecordField field : fields) {
                if (!(field instanceof VectorStoreRecordVectorField))
                    defineDataColumnType(columnIndex++, oracleStatement, field.getFieldType());
                else
                    oracleStatement.defineColumnType(columnIndex++,
                        OracleVectorStoreFieldHelper.getOracleTypeForVectorField((VectorStoreRecordVectorField) field),
                        Integer.MAX_VALUE);
            }
            oracleStatement.setLobPrefetchSize(Integer.MAX_VALUE); // Workaround for Oracle JDBC bug 37030121

            // get result set
            try (ResultSet rs = statement.executeQuery()) {
                GetRecordOptions getRecordOptions = new GetRecordOptions(options.isIncludeVectors());
                while (rs.next()) {
                    // Cosine distance function. 1 - cosine similarity.
                    double score = Math.abs(rs.getDouble("distance"));
                    if (distanceFunction == DistanceFunction.COSINE_SIMILARITY) {
                        score = 1d - score;
                    }
                    records.add(new VectorSearchResult<>(mapper.mapStorageModelToRecord(rs, getRecordOptions), score));
                }
            }
        } catch (SQLException | JsonProcessingException e) {
            logger.info(e.getMessage());
            throw  new SKException("Search failed", e);
        }

        return new VectorSearchResults<>(records);
    }

    private void setSearchParameter(PreparedStatement statement, int index, Class<?> type, Object value) {

        try {
            if (List.class.equals(type)) {
                statement.setObject(index, objectMapper.writeValueAsString(value));
                System.out.println(
                    "Set values: " + objectMapper.writeValueAsString(value));
                return;
            }
            if (UUID.class.equals(type)) {
                if (value == null) {
                    statement.setNull(index, OracleTypes.RAW);
                } else {
                    UUID uuid = (UUID)value;
                    ByteBuffer bb = ByteBuffer.allocate(16);
                    bb.putLong(uuid.getMostSignificantBits());
                    bb.putLong(uuid.getLeastSignificantBits());
                    statement.setBytes(index, bb.array());
                    System.out.println("Set values: " + uuid);
                }
                return;
            }
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
            System.out.println("Set parameter " + index + " to: " + value);
            statement.setObject(index, value);

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }


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
            case OracleDataTypesMapping.UUID:
                statement.defineColumnType(columnIndex, OracleTypes.RAW);
                break;
            case OracleDataTypesMapping.BYTE_ARRAY:
                statement.defineColumnType(columnIndex, OracleTypes.RAW);
            default:
                statement.defineColumnType(columnIndex, OracleTypes.VARCHAR);
        }
    }


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

    @Override
    public String getAnyTagEqualToFilter(AnyTagEqualToFilterClause filterClause) {
        String fieldName = JDBCVectorStoreQueryProvider
                .validateSQLidentifier(filterClause.getFieldName());

        return String.format("JSON_EXISTS(%s, '$[*]?(@ == $v_%s)' PASSING ? AS \"v_%s\")",
            fieldName, fieldName, fieldName);
    }

    @Override
    public <Record> VectorStoreRecordMapper<Record, ResultSet> getVectorStoreRecordMapper(
        Class<Record> recordClass,
        VectorStoreRecordDefinition vectorStoreRecordDefinition) {
        return OracleVectorStoreRecordMapper.<Record>builder()
            .withRecordClass(recordClass)
            .withVectorStoreRecordDefinition(vectorStoreRecordDefinition)
            .withSupportedDataTypesMapping(getSupportedDataTypes())
            .withAnnotatedTypeMapping(annotatedTypeMapping)
            .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder
        extends JDBCVectorStoreQueryProvider.Builder {

        private DataSource dataSource;
        private String collectionsTable = DEFAULT_COLLECTIONS_TABLE;
        private String prefixForCollectionTables = DEFAULT_PREFIX_FOR_COLLECTION_TABLES;
        private ObjectMapper objectMapper = new ObjectMapper();
        private StringTypeMapping stringTypeMapping = StringTypeMapping.USE_VARCHAR;
        private int defaultVarcharSize = 2000;

        private Map<Class<?>, String> annotatedTypeMapping = null;


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

        public Builder withObjectMapper(
            ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
            return this;
        }

        public Builder withAnnotatedTypeMapping(Map<Class<?>, String> annotatedTypeMapping) {
            this.annotatedTypeMapping = annotatedTypeMapping;
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
         * Sets the default size of the VARHCHAR2 fields.
         * @param defaultVarcharSize the default size of the VARHCHAR2 fields. By default, the size
         *                           is 2000.
         * @return then builder
         */
        public Builder withDefaultVarcharSize (int defaultVarcharSize) {
            this.defaultVarcharSize = defaultVarcharSize;
            return this;
        }

        @Override
        public OracleVectorStoreQueryProvider build() {
            return new OracleVectorStoreQueryProvider(dataSource, collectionsTable,
                prefixForCollectionTables, defaultVarcharSize, stringTypeMapping, objectMapper,
                annotatedTypeMapping);
        }
    }
}