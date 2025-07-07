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

import com.microsoft.semantickernel.data.vectorstorage.annotations.VectorStoreRecordData;
import com.microsoft.semantickernel.data.vectorstorage.annotations.VectorStoreRecordKey;
import com.microsoft.semantickernel.data.vectorstorage.annotations.VectorStoreRecordVector;
import com.microsoft.semantickernel.data.vectorstorage.definition.DistanceFunction;
import com.microsoft.semantickernel.data.vectorstorage.definition.IndexKind;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class ClassWithAllPrimitiveTypes {

    @VectorStoreRecordKey
    private final String id;

    @VectorStoreRecordData(isFilterable = true)
    private final Boolean booleanValue;

    @VectorStoreRecordData(isFilterable = true)
    private final byte byteValue;

    @VectorStoreRecordData(isFilterable = true)
    private final short shortValue;

    @VectorStoreRecordData(isFilterable = true)
    private final int integerValue;

    @VectorStoreRecordData(isFilterable = true)
    private final long longValue;

    @VectorStoreRecordData(isFilterable = true)
    private final float floatValue;

    @VectorStoreRecordData(isFilterable = true)
    private final double doubleValue;

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
    private final float[] vectorValue;


    public ClassWithAllPrimitiveTypes() {
        this(null, false, Byte.MIN_VALUE,Short.MIN_VALUE, 0, 0l, 0f, 0d, null, null, null, null, null, null);
    };
    public ClassWithAllPrimitiveTypes(String id, boolean booleanValue, byte byteValue,
                                    short shortValue, int integerValue, long longValue, float floatValue, double doubleValue,
                                    BigDecimal decimalValue, OffsetDateTime offsetDateTimeValue, UUID uuidValue,
                                    byte[] byteArrayValue, List<Float> listOfFloatValue, float[] vectorValue) {
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

    public boolean getBooleanValue() {
        return booleanValue;
    }

    public byte getByteValue() {
        return byteValue;
    }

    public short getShortValue() {
        return shortValue;
    }

    public int getIntegerValue() {
        return integerValue;
    }

    public long getLongValue() {
        return longValue;
    }

    public float getFloatValue() {
        return floatValue;
    }

    public double getDoubleValue() {
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

    public float[] getVectorValue() {
        return vectorValue;
    }
}
