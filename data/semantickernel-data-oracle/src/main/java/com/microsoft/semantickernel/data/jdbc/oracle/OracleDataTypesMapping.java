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
    public static final String OFFSET_DATE_TIME = "TIMESTAMP(9) WITH TIME ZONE";
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
