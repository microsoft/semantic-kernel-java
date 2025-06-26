/*
 ** Semantic Kernel Oracle connector version 1.0.
 **
 ** Copyright (c) 2025 Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */
package com.microsoft.semantickernel.data.jdbc.oracle;

/**
 * Defines oracle database type constants for supported java types.
 */
public class OracleDataTypesMapping {

    /**
     * Oracle database type used when strings are mapped to VARCHAR
     */
    public static final String STRING_VARCHAR = "VARCHAR2(%s)";
    /**
     * Oracle database type used when strings are mapped to CLOB
     */
    public static final String STRING_CLOB = "CLOB";
    /**
     * Oracle database type used to map booleans
     */
    public static final String BOOLEAN = "BOOLEAN";
    /**
     * Oracle database type used to map bytes
     */
    public static final String BYTE = "NUMBER(3)";
    /**
     * Oracle database type used to map byte arrays
     */
    public static final String BYTE_ARRAY = "RAW(2000)";
    /**
     * Oracle database type used to map shorts
     */
    public static final String SHORT = "NUMBER(5)";
    /**
     * Oracle database type used to map ints
     */
    public static final String INTEGER = "NUMBER(10)";
    /**
     * Oracle database type used to map longs
     */
    public static final String LONG = "NUMBER(19)";
    /**
     * Oracle database type used to map float
     */
    public static final String FLOAT = "BINARY_FLOAT";
    /**
     * Oracle database type used to map double
     */
    public static final String DOUBLE = "BINARY_DOUBLE";
    /**
     * Oracle database type used to map BigDecimal
     */
    public static final String DECIMAL = "NUMBER";
    /**
     * Oracle database type used to map offset date time
     */
    public static final String OFFSET_DATE_TIME = "TIMESTAMP(7) WITH TIME ZONE";
    /**
     * Oracle database type used to map UUID
     */
    public static final String UUID = "VARCHAR2(36)";
    /**
     * Oracle database type used to map lists
     */
    public static final String JSON = "JSON";
    /**
     * Oracle database type used to map vectors (the parameter is the dimension of the vector)
     */
    public static final String VECTOR_FLOAT = "VECTOR(%s, FLOAT32)";
}
