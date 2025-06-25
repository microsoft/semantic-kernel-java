package com.microsoft.semantickernel.data.jdbc.oracle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.microsoft.semantickernel.data.vectorstorage.annotations.VectorStoreRecordData;
import com.microsoft.semantickernel.data.vectorstorage.annotations.VectorStoreRecordKey;
import com.microsoft.semantickernel.data.vectorstorage.annotations.VectorStoreRecordVector;
import com.microsoft.semantickernel.data.vectorstorage.definition.DistanceFunction;
import com.microsoft.semantickernel.data.vectorstorage.definition.IndexKind;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ClassWithAnnotatedTypes {

    private final String id;

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = As.EXTERNAL_PROPERTY , property = "value_type")
    @JsonSubTypes({
        @JsonSubTypes.Type(value = String.class, name="java.lang.String"),
        @JsonSubTypes.Type(value = Boolean.class, name="java.lang.Boolean"),
        @JsonSubTypes.Type(value = Byte.class, name="java.lang.Byte"),
        @JsonSubTypes.Type(value = Short.class, name="java.lang.Short"),
        @JsonSubTypes.Type(value = Integer.class, name="java.lang.Integer"),
        @JsonSubTypes.Type(value = Long.class, name="java.lang.Long"),
        @JsonSubTypes.Type(value = Float.class, name="java.lang.Float"),
        @JsonSubTypes.Type(value = Double.class, name="java.lang.Double"),
        @JsonSubTypes.Type(value = BigDecimal.class, name="java.math.BigDecimal"),
        @JsonSubTypes.Type(value = OffsetDateTime.class, name="java.time.OffsetDateTime"),
        @JsonSubTypes.Type(value = UUID.class, name="java.util.UUID"),
        @JsonSubTypes.Type(value = byte[].class, name="byte_array"),
        @JsonSubTypes.Type(value = List.class, name="listOfStrings")
    })
    private Object value;

    private final Float[] vectorValue;


    public ClassWithAnnotatedTypes() {
        this(null, null, null);
    };
    public ClassWithAnnotatedTypes(String id, Object value, Float[] vectorValue) {
        this.id = id;
        this.value = value;
        this.vectorValue = vectorValue;
    }

    public String getId() {
        return id;
    }

    public Object getValue() {
        return value;
    }

    public Float[] getVectorValue() {
        return vectorValue;
    }
}
