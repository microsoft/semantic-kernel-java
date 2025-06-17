package com.microsoft.semantickernel.data.jdbc.oracle;

/**
 * Defines oracle database type constants for supported field types.
 */
public class OracleDataTypesMapping {
    public static final String STRING_VARCHAR = "NVARCHAR2(%s)";
    public static final String STRING_CLOB = "CLOB";
    public static final String BOOLEAN = "BOOLEAN";
    public static final String BYTE = "NUMBER(3)";
    public static final String BYTE_ARRAY = "RAW(2000)";
    public static final String SHORT = "NUMBER(5)";
    public static final String INTEGER = "NUMBER(10)";
    public static final String LONG = "NUMBER(19)";
    public static final String FLOAT = "BINARY_FLOAT";
    public static final String DOUBLE = "BINARY_DOUBLE";
    public static final String DECIMAL = "NUMBER(18,2)";
    public static final String OFFSET_DATE_TIME = "TIMESTAMP(7) WITH TIME ZONE";
    public static final String UUID = "RAW(16)";
    public static final String JSON = "JSON";
    public static final String VECTOR_FLOAT = "VECTOR(%s, FLOAT32)";
}
