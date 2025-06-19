package com.microsoft.semantickernel.data.jdbc.oracle;

import com.microsoft.semantickernel.data.jdbc.oracle.OracleVectorStoreQueryProvider.StringTypeMapping;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordDataField;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordField;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordKeyField;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordVectorField;
import oracle.jdbc.OracleTypes;
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
 * Helper class for field operations.
 */
public class OracleVectorStoreFieldHelper {
    private static final Logger LOGGER = Logger.getLogger(OracleVectorStoreQueryProvider.class.getName());

    /**
     * Maps supported key java classes to Oracle database types
     */
    private static final HashMap<Class<?>, String> supportedKeyTypes = new HashMap() {
        {
            put(String.class, String.format(OracleDataTypesMapping.STRING_VARCHAR, 255));
            put(short.class, OracleDataTypesMapping.SHORT);
            put(Short.class, OracleDataTypesMapping.SHORT);
            put(int.class, OracleDataTypesMapping.INTEGER);
            put(Integer.class, OracleDataTypesMapping.INTEGER);
            put(long.class, OracleDataTypesMapping.LONG);
            put(Long.class, OracleDataTypesMapping.LONG);
            put(UUID .class, OracleDataTypesMapping.UUID);
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
/*
            put(byte[].class,"VECTOR(%s, INT8)");
            put(Byte[].class,"VECTOR(%s, INT8)");
            put(double[].class,"VECTOR(%s, FLOAT64)");
            put(Double[].class,"VECTOR(%s, FLOAT64)");
            put(boolean[].class,"VECTOR(%s, BINARY)");
            put(Boolean[].class,"VECTOR(%s, BINARY)");
 */
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
     * Maps vector type to OracleTypes. Only needed if types other than FLOAT_32 are supported.
     */
    private static final Map<Class<?>, Integer> mapOracleTypeToVector = new HashMap() {
        {
            put(float[].class, OracleTypes.VECTOR_FLOAT32);
            put(Float[].class, OracleTypes.VECTOR_FLOAT32);
/*
            put(byte[].class, OracleTypes.VECTOR_INT8);
            put(Byte[].class, OracleTypes.VECTOR_INT8);
            put(Double[].class, OracleTypes.VECTOR_FLOAT64);
            put(double[].class, OracleTypes.VECTOR_FLOAT64);
            put(Boolean[].class, OracleTypes.VECTOR_BINARY);
            put(boolean[].class, OracleTypes.VECTOR_BINARY);
*/
        }
    };

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

        if (stringTypeMapping.equals(StringTypeMapping.USE_VARCHAR)) {
            supportedDataTypes.put(String.class, String.format(OracleDataTypesMapping.STRING_VARCHAR, defaultVarCharLength));
        } else {
            supportedDataTypes.put(String.class, OracleDataTypesMapping.STRING_CLOB);
        }
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
            String dataFieldIndex = "CREATE MULTIVALUE INDEX %s ON %s t (t.%s.%s)";
            return String.format(dataFieldIndex,
                collectionTableName + "_" + dataField.getEffectiveStorageName(),
                collectionTableName,
                dataField.getEffectiveStorageName(),
                getFunctionForType(supportedDataTypes.get(dataField.getFieldSubType())));
        }  else {
            String dataFieldIndex = "CREATE INDEX %s ON %s (%s ASC)";
            return String.format(dataFieldIndex,
                collectionTableName + "_" + dataField.getEffectiveStorageName(),
                collectionTableName,
                dataField.getEffectiveStorageName()
            );
        }
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
     * Gets the type of the vector given the field definition. This method is not needed if only
     *
     * @param field the vector field definition.
     * @return returns the type of vector for the given field type.
     */
    public static String getTypeForVectorField(VectorStoreRecordVectorField field) {
        String dimension = field.getDimensions() > 0 ? String.valueOf(field.getDimensions()) : "*";
        return String.format(supportedVectorTypes.get(field.getFieldType()), dimension);
/* Not needed since all types are FLOAT32
        if (field.getFieldSubType() != null) {
            String vectorType;
            switch (field.getFieldSubType().getName()) {
                case "java.lang.Double":
                    vectorType = "FLOAT64";
                    break;
                case "java.lang.Byte":
                    vectorType = "INT8";
                    break;
                case "java.lang.Boolean":
                    vectorType = "BINARY";
                    break;
                default:
                    vectorType = "FLOAT32";
            }
            return String.format(supportedVectorTypes.get(field.getFieldType()), dimension, vectorType);
        } else {
            return String.format(supportedVectorTypes.get(field.getFieldType()), dimension);
        }
 */
    }

    /**
     * Gets the JDBC oracle of the vector field definition.
     * @param field the vector field definition.
     * @return the JDBC oracle type.
     */
    public static int getOracleTypeForVectorField(VectorStoreRecordVectorField field) {
        if (field.getFieldSubType() == null) {
            Integer oracleType = mapOracleTypeToVector.get(field.getFieldType());
            if (oracleType != null) {
                return oracleType.intValue();
            } else {
                // field was declared as list with no subtype, assume FLOAT
                return OracleTypes.VECTOR_FLOAT32;
            }

        } else {
            switch (field.getFieldSubType().getName()) {
                case "java.lang.Double":
                    return OracleTypes.VECTOR_FLOAT64;
                case "java.lang.Byte":
                    return OracleTypes.VECTOR_INT8;
                case "java.lang.Boolean":
                    return OracleTypes.VECTOR_BINARY;
                default:
                    return OracleTypes.VECTOR_FLOAT32;
            }
        }
    }

    public static boolean isUUID (VectorStoreRecordField field) {
        return (field.getFieldType().getName() == "java.util.UUID");
    }

    /**
     * Generates the index name given the field name. by suffixing "_VECTOR_INDEX" to the field name.
     * @param effectiveStorageName the field name.
     * @return the index name.
     */
    private static String getIndexName(String effectiveStorageName) {
        return effectiveStorageName + "_VECTOR_INDEX";
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

}
