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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class ClassWithAnnotatedTypes {

    private final String id;

    @JsonProperty("value_type")
    private final String valueType;

    @JsonProperty("value_field")
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = As.EXTERNAL_PROPERTY , property = "value_type")
    @JsonSubTypes({
        @JsonSubTypes.Type(value = String.class, name="string"),
        @JsonSubTypes.Type(value = Boolean.class, name="boolean"),
        @JsonSubTypes.Type(value = Byte.class, name="byte"),
        @JsonSubTypes.Type(value = Short.class, name="short"),
        @JsonSubTypes.Type(value = Integer.class, name="integer"),
        @JsonSubTypes.Type(value = Long.class, name="long"),
        @JsonSubTypes.Type(value = Float.class, name="float"),
        @JsonSubTypes.Type(value = Double.class, name="double"),
        @JsonSubTypes.Type(value = BigDecimal.class, name="decimal"),
        @JsonSubTypes.Type(value = OffsetDateTime.class, name="timestamp"),
        @JsonSubTypes.Type(value = UUID.class, name="uuid"),
        @JsonSubTypes.Type(value = byte[].class, name="byte_array"),
        @JsonSubTypes.Type(value = List.class, name="json")
    })
    private Object value;

    private final Float[] vectorValue;


    public ClassWithAnnotatedTypes() {
        this(null, null, null, null);
    };
    public ClassWithAnnotatedTypes(String id, String valueType, Object value, Float[] vectorValue) {
        this.id = id;
        this.valueType = valueType;
        this.value = value;
        this.vectorValue = vectorValue;
    }

    public String getId() {
        return id;
    }

    public String getValueType() { return valueType; }

    public Object getValue() {
        return value;
    }

    public Float[] getVectorValue() {
        return vectorValue;
    }
}
