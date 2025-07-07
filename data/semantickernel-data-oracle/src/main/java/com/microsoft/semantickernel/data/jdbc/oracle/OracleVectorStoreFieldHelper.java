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
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collection;
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
     * The logger
     */
    private static final Logger LOGGER = Logger.getLogger(OracleVectorStoreFieldHelper.class.getName());

    /**
     * Maps supported key java classes to Oracle database types
     */
    private static final HashMap<Class<?>, String> supportedKeyTypes = new HashMap() {
        {
            put(String.class, String.format(OracleDataTypesMapping.STRING_VARCHAR, 255));
        }
    };

    /**
     * Maps supported vector java classes to Oracle database types
     */
    private static final Map<Class<?>, String> supportedVectorTypes = new HashMap() {
        {
            put(String.class, OracleDataTypesMapping.VECTOR_FLOAT);
            put(List.class, OracleDataTypesMapping.VECTOR_FLOAT);
            put(Collection.class, OracleDataTypesMapping.VECTOR_FLOAT);
            put(float[].class, OracleDataTypesMapping.VECTOR_FLOAT);
            put(Float[].class, OracleDataTypesMapping.VECTOR_FLOAT);
        }
    };

    /**
     * Maps supported data java classes to Oracle database types
     */
    private static final HashMap<Class<?>, String> supportedDataTypes = new HashMap() {
        {
            put(byte.class, OracleDataTypesMapping.BYTE);
            put(Byte.class, OracleDataTypesMapping.BYTE);
            put(short.class, OracleDataTypesMapping.SHORT);
            put(Short.class, OracleDataTypesMapping.SHORT);
            put(int.class, OracleDataTypesMapping.INTEGER);
            put(Integer.class, OracleDataTypesMapping.INTEGER);
            put(long.class, OracleDataTypesMapping.LONG);
            put(Long.class, OracleDataTypesMapping.LONG);
            put(Float.class, OracleDataTypesMapping.FLOAT);
            put(float.class, OracleDataTypesMapping.FLOAT);
            put(Double.class, OracleDataTypesMapping.DOUBLE);
            put(double.class, OracleDataTypesMapping.DOUBLE);
            put(BigDecimal.class, OracleDataTypesMapping.DECIMAL);
            put(Boolean.class, OracleDataTypesMapping.BOOLEAN);
            put(boolean.class, OracleDataTypesMapping.BOOLEAN);
            put(OffsetDateTime.class, OracleDataTypesMapping.OFFSET_DATE_TIME);
            put(UUID.class, OracleDataTypesMapping.UUID);
            put(byte[].class, OracleDataTypesMapping.BYTE_ARRAY);
            put(List.class, OracleDataTypesMapping.JSON);
        }
    };

    /**
     * Suffix added to the effective column name to generate the index name for a vector column.
     */
    public static final String VECTOR_INDEX_SUFFIX = "_VECTOR_INDEX";

    /**
     * Gets the mapping between the supported Java key types and the Oracle database type.
     *
     * @return the mapping between the supported Java key types and the Oracle database type.
     */
    public static HashMap<Class<?>, String> getSupportedKeyTypes() {
        return supportedKeyTypes;
    }

    /**
     * Gets the mapping between the supported Java data types and the Oracle database type.
     *
     * @return the mapping between the supported Java data types and the Oracle database type.
     */
    public static Map<Class<?>, String> getSupportedDataTypes(
        StringTypeMapping stringTypeMapping, int defaultVarCharLength) {
        String stringType = stringTypeMapping.equals(StringTypeMapping.USE_VARCHAR)
            ? String.format(OracleDataTypesMapping.STRING_VARCHAR, defaultVarCharLength)
            : OracleDataTypesMapping.STRING_CLOB;
        supportedDataTypes.put(String.class, stringType);
        LOGGER.finest("Mapping String columns to " + stringType);
        return supportedDataTypes;
    }

    /**
     * Gets the mapping between the supported Java data types and the Oracle database type.
     *
     * @return the mapping between the supported Java data types and the Oracle database type.
     */
    public static Map<Class<?>, String> getSupportedVectorTypes() {
        return supportedVectorTypes;
    }

    /**
     * Generates the statement to create the index according to the vector field definition.
     *
     * @return the CREATE VECTOR INDEX statement to create the index according to the vector
     *         field definition.
     */
    public static String getCreateVectorIndexStatement(VectorStoreRecordVectorField field, String collectionTableName) {
        switch (field.getIndexKind()) {
            case IVFFLAT:
                return "CREATE VECTOR INDEX IF NOT EXISTS "
                    + getIndexName(field.getEffectiveStorageName())
                    + " ON "
                    + collectionTableName + "( " + field.getEffectiveStorageName() + " ) "
                    + " ORGANIZATION NEIGHBOR PARTITIONS "
                    + " WITH DISTANCE COSINE "
                    + "PARAMETERS ( TYPE IVF )";
            case HNSW:
                return "CREATE VECTOR INDEX IF NOT EXISTS " + getIndexName(field.getEffectiveStorageName())
                    + " ON "
                    + collectionTableName + "( " + field.getEffectiveStorageName() + " ) "
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
    public static String createIndexForDataField(String collectionTableName, VectorStoreRecordDataField dataField, Map<Class<?>, String> supportedDataTypes) {
        if (supportedDataTypes.get(dataField.getFieldType()) == "JSON") {
            String dataFieldIndex = "CREATE MULTIVALUE INDEX IF NOT EXISTS %s ON %s t (t.%s.%s)";
            return String.format(dataFieldIndex,
                collectionTableName + "_" + dataField.getEffectiveStorageName(),
                collectionTableName,
                dataField.getEffectiveStorageName(),
                getFunctionForType(supportedDataTypes.get(dataField.getFieldSubType())));
        }  else {
            String dataFieldIndex = "CREATE INDEX IF NOT EXISTS %s ON %s (%s ASC)";
            return String.format(dataFieldIndex,
                collectionTableName + "_" + dataField.getEffectiveStorageName(),
                collectionTableName,
                dataField.getEffectiveStorageName()
            );
        }
    }

    /**
     * Returns vector columns names and types for CREATE TABLE statement
     * @param fields list of vector record fields.
     * @return comma separated list of columns and types for CREATE TABLE statement.
     */
    public static String getVectorColumnNamesAndTypes(List<VectorStoreRecordVectorField> fields) {
        List<String> columns = fields.stream()
            .map(field -> field.getEffectiveStorageName() + " " +
                OracleVectorStoreFieldHelper.getTypeForVectorField(field)
            ).collect(Collectors.toList());

        return String.join(", ", columns);
    }

    /**
     * Returns key column names and type for key column for CREATE TABLE statement
     * @param field the key field.
     * @return column name and type of the key field for CREATE TABLE statement.
     */
    public static String getKeyColumnNameAndType(VectorStoreRecordKeyField field) {
        return field.getEffectiveStorageName() + " " + supportedKeyTypes.get(field.getFieldType());
    }


    /**
     * Generates the index name given the field name. by suffixing "_VECTOR_INDEX" to the field name.
     * @param effectiveStorageName the field name.
     * @return the index name.
     */
    private static String getIndexName(String effectiveStorageName) {
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

}
