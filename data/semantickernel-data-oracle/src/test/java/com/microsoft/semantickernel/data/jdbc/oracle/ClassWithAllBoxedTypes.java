/*
 ** Semantic Kernel Oracle connector version 1.0.
 **
 ** Copyright (c) 2025 Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */
package com.microsoft.semantickernel.data.jdbc.oracle;

import com.microsoft.semantickernel.data.vectorstorage.annotations.VectorStoreRecordData;
import com.microsoft.semantickernel.data.vectorstorage.annotations.VectorStoreRecordKey;
import com.microsoft.semantickernel.data.vectorstorage.annotations.VectorStoreRecordVector;
import com.microsoft.semantickernel.data.vectorstorage.definition.DistanceFunction;
import com.microsoft.semantickernel.data.vectorstorage.definition.IndexKind;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class ClassWithAllBoxedTypes {

    @VectorStoreRecordKey
    private final String id;

    @VectorStoreRecordData(isFilterable = true)
    private final Boolean booleanValue;

    @VectorStoreRecordData(isFilterable = true)
    private final Byte byteValue;

    @VectorStoreRecordData(isFilterable = true)
    private final Short shortValue;

    @VectorStoreRecordData(isFilterable = true)
    private final Integer integerValue;

    @VectorStoreRecordData(isFilterable = true)
    private final Long longValue;

    @VectorStoreRecordData(isFilterable = true)
    private final Float floatValue;

    @VectorStoreRecordData(isFilterable = true)
    private final Double doubleValue;

    @VectorStoreRecordData(isFilterable = true)
    private final BigDecimal decimalValue;

    @VectorStoreRecordData(isFilterable = true)
    private final OffsetDateTime offsetDateTimeValue;

    @VectorStoreRecordData(isFilterable = true)
    private final UUID uuidValue;

    @VectorStoreRecordData(isFilterable = true)
    private final byte[] byteArrayValue;

    @VectorStoreRecordData(isFilterable = true)
    private final List<Float> listOfFloatValue;

    @VectorStoreRecordVector(dimensions = 8, distanceFunction = DistanceFunction.COSINE_DISTANCE, indexKind = IndexKind.IVFFLAT)
    private final Float[] vectorValue;


    public ClassWithAllBoxedTypes() {
        this(null, false, Byte.MIN_VALUE,Short.MIN_VALUE, 0, 0l, 0f, 0d, null, null, null, null, null, null);
    };
    public ClassWithAllBoxedTypes(String id, Boolean booleanValue, Byte byteValue,
        Short shortValue, Integer integerValue, Long longValue, Float floatValue, Double doubleValue,
        BigDecimal decimalValue, OffsetDateTime offsetDateTimeValue, UUID uuidValue,
        byte[] byteArrayValue, List<Float> listOfFloatValue, Float[] vectorValue) {
        this.id = id;
        this.booleanValue = booleanValue;
        this.byteValue = byteValue;
        this.shortValue = shortValue;
        this.integerValue = integerValue;
        this.longValue = longValue;
        this.floatValue = floatValue;
        this.doubleValue = doubleValue;
        this.decimalValue = decimalValue;
        this.offsetDateTimeValue = offsetDateTimeValue;
        this.uuidValue = uuidValue;
        this.byteArrayValue = byteArrayValue;
        this.listOfFloatValue = listOfFloatValue;
        this.vectorValue = vectorValue;
    }

    public String getId() {
        return id;
    }

    public Boolean getBooleanValue() {
        return booleanValue;
    }

    public Byte getByteValue() {
        return byteValue;
    }

    public Short getShortValue() {
        return shortValue;
    }

    public Integer getIntegerValue() {
        return integerValue;
    }

    public Long getLongValue() {
        return longValue;
    }

    public Float getFloatValue() {
        return floatValue;
    }

    public Double getDoubleValue() {
        return doubleValue;
    }


    public BigDecimal getDecimalValue() {
        return decimalValue;
    }

    public OffsetDateTime getOffsetDateTimeValue() {
        return offsetDateTimeValue;
    }

    public UUID getUuidValue() {
        return uuidValue;
    }

    public byte[] getByteArrayValue() {
        return byteArrayValue;
    }

    public List<Float> getListOfFloatValue() {
        return listOfFloatValue;
    }

    public Float[] getVectorValue() {
        return vectorValue;
    }
}
