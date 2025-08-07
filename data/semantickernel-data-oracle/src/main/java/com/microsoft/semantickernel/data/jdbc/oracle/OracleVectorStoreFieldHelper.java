/*
 ** Oracle Database Vector Store Connector for Semantic Kernel (Java)
 **
 ** Copyright (c) 2025 Oracle and/or its affiliates. All rights reserved.
 **
 ** The MIT License (MIT)
 **
 ** Permission is hereby granted, free of charge, to any person obtaining a copy
 ** of this software and associated documentation files (the "Software"), to
 ** deal in the Software without restriction, including without limitation the
 ** rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 ** sell copies of the Software, and to permit persons to whom the Software is
 ** furnished to do so, subject to the following conditions:
 **
 ** The above copyright notice and this permission notice shall be included in
 ** all copies or substantial portions of the Software.
 **
 ** THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 ** IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 ** FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 ** AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 ** LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 ** FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 ** IN THE SOFTWARE.
 */
package com.microsoft.semantickernel.data.jdbc.oracle;

import com.microsoft.semantickernel.data.jdbc.oracle.OracleVectorStoreQueryProvider.StringTypeMapping;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordDataField;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordKeyField;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordVectorField;
import com.microsoft.semantickernel.exceptions.SKException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Helper class for field operations. Handles mapping between field java types to DB types and
 * generating SQL statement to create field indexes.
 */
class OracleVectorStoreFieldHelper {

    /**
     * Object naming regular expression
     */
    private static final String OBJECT_NAMING_REGEXP = "[a-zA-Z_][a-zA-Z0-9_]{1,128}";
    /**
     * The logger
     */
    private static final Logger LOGGER = Logger.getLogger(OracleVectorStoreFieldHelper.class.getName());

    /**
     * Maps supported key java classes to Oracle database types
     */
    private static final HashMap<Class<?>, String> supportedKeyTypes = new HashMap();
    static {
        supportedKeyTypes.put(String.class, String.format(OracleDataTypesMapping.STRING_VARCHAR, 255));
    }

    /**
     * Maps supported vector java classes to Oracle database types
     */
    private static final Map<Class<?>, String> supportedVectorTypes = new HashMap();
    static {
        supportedVectorTypes.put(String.class, OracleDataTypesMapping.VECTOR_FLOAT);
        supportedVectorTypes.put(List.class, OracleDataTypesMapping.VECTOR_FLOAT);
        supportedVectorTypes.put(Collection.class, OracleDataTypesMapping.VECTOR_FLOAT);
        supportedVectorTypes.put(float[].class, OracleDataTypesMapping.VECTOR_FLOAT);
        supportedVectorTypes.put(Float[].class, OracleDataTypesMapping.VECTOR_FLOAT);
    }

    /**
     * Maps supported data java classes to Oracle database types
     */
    private static final HashMap<Class<?>, String> supportedDataTypes = new HashMap();
    static {
        supportedDataTypes.put(byte.class, OracleDataTypesMapping.BYTE);
        supportedDataTypes.put(Byte.class, OracleDataTypesMapping.BYTE);
        supportedDataTypes.put(short.class, OracleDataTypesMapping.SHORT);
        supportedDataTypes.put(Short.class, OracleDataTypesMapping.SHORT);
        supportedDataTypes.put(int.class, OracleDataTypesMapping.INTEGER);
        supportedDataTypes.put(Integer.class, OracleDataTypesMapping.INTEGER);
        supportedDataTypes.put(long.class, OracleDataTypesMapping.LONG);
        supportedDataTypes.put(Long.class, OracleDataTypesMapping.LONG);
        supportedDataTypes.put(Float.class, OracleDataTypesMapping.FLOAT);
        supportedDataTypes.put(float.class, OracleDataTypesMapping.FLOAT);
        supportedDataTypes.put(Double.class, OracleDataTypesMapping.DOUBLE);
        supportedDataTypes.put(double.class, OracleDataTypesMapping.DOUBLE);
        supportedDataTypes.put(BigDecimal.class, OracleDataTypesMapping.DECIMAL);
        supportedDataTypes.put(Boolean.class, OracleDataTypesMapping.BOOLEAN);
        supportedDataTypes.put(boolean.class, OracleDataTypesMapping.BOOLEAN);
        supportedDataTypes.put(OffsetDateTime.class, OracleDataTypesMapping.OFFSET_DATE_TIME);
        supportedDataTypes.put(UUID.class, OracleDataTypesMapping.UUID);
        supportedDataTypes.put(byte[].class, OracleDataTypesMapping.BYTE_ARRAY);
        supportedDataTypes.put(List.class, OracleDataTypesMapping.JSON);
    }

    /**
     * Suffix added to the effective column name to generate the index name for a vector column.
     */
    public static final String VECTOR_INDEX_SUFFIX = "_VECTOR_INDEX";

    /**
     * Gets the mapping between the supported Java key types and the Oracle database type.
     *
     * @return the mapping between the supported Java key types and the Oracle database type.
     */
    static Map<Class<?>, String> getSupportedKeyTypes() {

        return Collections.unmodifiableMap(supportedKeyTypes);
    }

    /**
     * Gets the mapping between the supported Java data types and the Oracle database type.
     *
     * @return the mapping between the supported Java data types and the Oracle database type.
     */
    static Map<Class<?>, String> getSupportedDataTypes(
        StringTypeMapping stringTypeMapping, int defaultVarCharLength) {
        String stringType = stringTypeMapping.equals(StringTypeMapping.USE_VARCHAR)
            ? String.format(OracleDataTypesMapping.STRING_VARCHAR, defaultVarCharLength)
            : OracleDataTypesMapping.STRING_CLOB;
        supportedDataTypes.put(String.class, stringType);
        LOGGER.finest("Mapping String columns to " + stringType);
        return Collections.unmodifiableMap(supportedDataTypes);
    }

    /**
     * Gets the mapping between the supported Java data types and the Oracle database type.
     *
     * @return the mapping between the supported Java data types and the Oracle database type.
     */
    static Map<Class<?>, String> getSupportedVectorTypes() {

        return Collections.unmodifiableMap(supportedVectorTypes);
    }

    /**
     * Generates the statement to create the index according to the vector field definition.
     *
     * @return the CREATE VECTOR INDEX statement to create the index according to the vector
     *         field definition.
     */
    static String getCreateVectorIndexStatement(VectorStoreRecordVectorField field, String collectionTableName) {
        switch (field.getIndexKind()) {
            case IVFFLAT:
                return "CREATE VECTOR INDEX IF NOT EXISTS "
                    + validateObjectNaming(getIndexName(field.getEffectiveStorageName()))
                    + " ON "
                    + validateObjectNaming(collectionTableName)
                    + "( " + validateObjectNaming(field.getEffectiveStorageName()) + " ) "
                    + " ORGANIZATION NEIGHBOR PARTITIONS "
                    + " WITH DISTANCE COSINE "
                    + "PARAMETERS ( TYPE IVF )";
            case HNSW:
                return "CREATE VECTOR INDEX IF NOT EXISTS "
                    + validateObjectNaming(getIndexName(field.getEffectiveStorageName()))
                    + " ON "
                    + validateObjectNaming(collectionTableName)
                    + "( " + validateObjectNaming(field.getEffectiveStorageName()) + " ) "
                    + "ORGANIZATION INMEMORY GRAPH "
                    + "WITH DISTANCE COSINE "
                    + "PARAMETERS (TYPE HNSW)";
            case UNDEFINED:
                return null;
            default:
                LOGGER.warning("Unsupported index kind: " + field.getIndexKind());
                return null;
        }
    }

    /**
     * Generates the statement to create the index according to the field definition.
     *
     * @return the CREATE INDEX statement to create the index according to the field definition.
     */
    static String createIndexForDataField(String collectionTableName, VectorStoreRecordDataField dataField, Map<Class<?>, String> supportedDataTypes) {
        if (supportedDataTypes.get(dataField.getFieldType()) == "JSON") {
            String dataFieldIndex = "CREATE MULTIVALUE INDEX IF NOT EXISTS %s ON %s t (t.%s.%s)";
            return String.format(dataFieldIndex,
                validateObjectNaming(collectionTableName + "_" + dataField.getEffectiveStorageName()),
                validateObjectNaming(collectionTableName),
                validateObjectNaming(dataField.getEffectiveStorageName()),
                getFunctionForType(supportedDataTypes.get(dataField.getFieldSubType())));
        }  else {
            String dataFieldIndex = "CREATE INDEX IF NOT EXISTS %s ON %s (%s ASC)";
            return String.format(dataFieldIndex,
                validateObjectNaming(collectionTableName + "_" + dataField.getEffectiveStorageName()),
                validateObjectNaming(collectionTableName),
                validateObjectNaming(dataField.getEffectiveStorageName())
            );
        }
    }

    /**
     * Returns vector columns names and types for CREATE TABLE statement
     * @param fields list of vector record fields.
     * @return comma separated list of columns and types for CREATE TABLE statement.
     */
    static String getVectorColumnNamesAndTypes(List<VectorStoreRecordVectorField> fields) {
        List<String> columns = fields.stream()
            .map(field -> validateObjectNaming(field.getEffectiveStorageName()) + " " +
                OracleVectorStoreFieldHelper.getTypeForVectorField(field)
            ).collect(Collectors.toList());

        return String.join(", ", columns);
    }

    /**
     * Returns key column names and type for key column for CREATE TABLE statement
     * @param field the key field.
     * @return column name and type of the key field for CREATE TABLE statement.
     */
    static String getKeyColumnNameAndType(VectorStoreRecordKeyField field) {
        return validateObjectNaming(field.getEffectiveStorageName()) + " " + supportedKeyTypes.get(field.getFieldType());
    }


    /**
     * Generates the index name given the field name. by suffixing "_VECTOR_INDEX" to the field name.
     * @param effectiveStorageName the field name.
     * @return the index name.
     */
    static String getIndexName(String effectiveStorageName) {
        return effectiveStorageName + VECTOR_INDEX_SUFFIX;
    }

    /**
     * Gets the type of the vector given the field definition. This method is not needed if only
     *
     * @param field the vector field definition.
     * @return returns the type of vector for the given field type.
     */
    private static String getTypeForVectorField(VectorStoreRecordVectorField field) {
        String dimension = field.getDimensions() > 0 ? String.valueOf(field.getDimensions()) : "*";
        return String.format(supportedVectorTypes.get(field.getFieldType()), dimension);
    }

    /**
     * Gets the function that allows to return the function that converts the JSON value to the
     * data type.
     * @param jdbcType The JDBC type.
     * @return the function that allows to return the function that converts the JSON value to the
     *         data type.
     */
    private static String getFunctionForType(String jdbcType) {
        switch (jdbcType) {
            case OracleDataTypesMapping.BOOLEAN:
                return "boolean()";
            case OracleDataTypesMapping.BYTE:
            case OracleDataTypesMapping.SHORT:
            case OracleDataTypesMapping.INTEGER:
            case OracleDataTypesMapping.LONG:
            case OracleDataTypesMapping.FLOAT:
            case OracleDataTypesMapping.DOUBLE:
            case OracleDataTypesMapping.DECIMAL:
                return "numberOnly()";
            case OracleDataTypesMapping.OFFSET_DATE_TIME:
                return "timestamp()";
            default:
                return "string()";
        }
    }


    /**
     * Validates an SQL identifier.
     *
     * @param identifier the identifier
     * @return the identifier if it is valid
     * @throws SKException if the identifier is invalid
     */
    static String validateObjectNaming(String identifier) {
        if (identifier.matches(OBJECT_NAMING_REGEXP)) {
            return identifier;
        }
        throw new SKException("Invalid SQL identifier: " + identifier);
    }

}
