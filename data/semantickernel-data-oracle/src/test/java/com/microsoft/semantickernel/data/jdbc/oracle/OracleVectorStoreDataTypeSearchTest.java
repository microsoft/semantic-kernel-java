<<<<<<< add-oracle-store
/*
 ** Semantic Kernel Oracle connector version 1.0.
 **
 ** Copyright (c) 2025 Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */
=======
>>>>>>> main
package com.microsoft.semantickernel.data.jdbc.oracle;

import com.microsoft.semantickernel.data.jdbc.JDBCVectorStore;
import com.microsoft.semantickernel.data.jdbc.JDBCVectorStoreOptions;
import com.microsoft.semantickernel.data.jdbc.JDBCVectorStoreRecordCollectionOptions;
import com.microsoft.semantickernel.data.vectorsearch.VectorSearchFilter;
import com.microsoft.semantickernel.data.vectorsearch.VectorSearchResults;
import com.microsoft.semantickernel.data.vectorstorage.VectorStoreRecordCollection;
import com.microsoft.semantickernel.data.vectorstorage.options.VectorSearchOptions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OracleVectorStoreDataTypeSearchTest extends OracleCommonVectorStoreRecordCollectionTest {
    private static final double MIN_DOUBLE = 1.0E-130;
    private static final double MIN_DECIMAL = -1.0E125;
    private static final BigDecimal BIG_NUMBER = BigDecimal.valueOf(9999999999999999.99);



    @ParameterizedTest
    @MethodSource("supportedDataTypes")
    void testDataTypesSearch (ClassWithAllBoxedTypes record) {
        VectorStoreRecordCollection<String, ClassWithAllBoxedTypes> collection = setupBoxed();

        collection.upsertAsync(record, null).block();

        // boolean
        VectorSearchResults<ClassWithAllBoxedTypes> results = collection.searchAsync(
            null,
            VectorSearchOptions.builder()
                .withVectorSearchFilter(
                    VectorSearchFilter.builder()
                    .equalTo("booleanValue", record.getBooleanValue()).build()
        ).build()).block();

        assertEquals(1, results.getTotalCount());
        assertEquals(record.getBooleanValue(), results.getResults().get(0).getRecord().getBooleanValue());

        // byte
        results = collection.searchAsync(
            null,
            VectorSearchOptions.builder()
                .withVectorSearchFilter(
                    VectorSearchFilter.builder()
                        .equalTo("byteValue", record.getByteValue()).build()
                ).build()).block();

        assertEquals(1, results.getTotalCount());
        assertEquals(record.getByteValue(), results.getResults().get(0).getRecord().getByteValue());

        // short
        results = collection.searchAsync(
            null,
            VectorSearchOptions.builder()
                .withVectorSearchFilter(
                    VectorSearchFilter.builder()
                        .equalTo("shortValue", record.getShortValue()).build()
                ).build()).block();

        assertEquals(1, results.getTotalCount());
        assertEquals(record.getShortValue(), results.getResults().get(0).getRecord().getShortValue());

        // integer
        results = collection.searchAsync(
            null,
            VectorSearchOptions.builder()
                .withVectorSearchFilter(
                    VectorSearchFilter.builder()
                        .equalTo("integerValue", record.getIntegerValue()).build()
                ).build()).block();

        assertEquals(1, results.getTotalCount());
        assertEquals(record.getIntegerValue(), results.getResults().get(0).getRecord().getIntegerValue());

        // long
        results = collection.searchAsync(
            null,
            VectorSearchOptions.builder()
                .withVectorSearchFilter(
                    VectorSearchFilter.builder()
                        .equalTo("longValue", record.getLongValue()).build()
                ).build()).block();

        assertEquals(1, results.getTotalCount());
        assertEquals(record.getLongValue(), results.getResults().get(0).getRecord().getLongValue());

        // float
        results = collection.searchAsync(
            null,
            VectorSearchOptions.builder()
                .withVectorSearchFilter(
                    VectorSearchFilter.builder()
                        .equalTo("floatValue", record.getFloatValue()).build()
                ).build()).block();

        assertEquals(1, results.getTotalCount());
        assertEquals(record.getFloatValue(), results.getResults().get(0).getRecord().getFloatValue());

        // double
        results = collection.searchAsync(
            null,
            VectorSearchOptions.builder()
                .withVectorSearchFilter(
                    VectorSearchFilter.builder()
                        .equalTo("doubleValue", record.getDoubleValue()).build()
                ).build()).block();

        assertEquals(1, results.getTotalCount());
        assertEquals(record.getDoubleValue(), results.getResults().get(0).getRecord().getDoubleValue());

<<<<<<< add-oracle-store
=======
        System.out.println(record.getDecimalValue());
        System.out.println(record.getDecimalValue().doubleValue());
        System.out.println(results.getResults().get(0).getRecord().getDecimalValue());
        System.out.println(results.getResults().get(0).getRecord().getDecimalValue().doubleValue());

>>>>>>> main
        // decimal
        results = collection.searchAsync(
            null,
            VectorSearchOptions.builder()
                .withVectorSearchFilter(
                    VectorSearchFilter.builder()
                        .equalTo("decimalValue", record.getDecimalValue()).build()
                ).build()).block();

        assertEquals(1, results.getTotalCount());
        if (record.getDecimalValue() != null) {
            assertEquals(0, record.getDecimalValue()
                .compareTo(results.getResults().get(0).getRecord().getDecimalValue()));
        } else {
            assertEquals(record.getDecimalValue(),
                results.getResults().get(0).getRecord().getDecimalValue());
        }

        // offset date time
        results = collection.searchAsync(
            null,
            VectorSearchOptions.builder()
                .withVectorSearchFilter(
                    VectorSearchFilter.builder()
                        .equalTo("offsetDateTimeValue", record.getOffsetDateTimeValue()).build()
                ).build()).block();

        assertEquals(1, results.getTotalCount());
        if (record.getOffsetDateTimeValue() != null) {
            assertTrue(record.getOffsetDateTimeValue()
                .isEqual(results.getResults().get(0).getRecord().getOffsetDateTimeValue()));
        } else {
            assertEquals(record.getOffsetDateTimeValue(),
                results.getResults().get(0).getRecord().getOffsetDateTimeValue());
        }

        // UUID
        results = collection.searchAsync(
            null,
            VectorSearchOptions.builder()
                .withVectorSearchFilter(
                    VectorSearchFilter.builder()
                        .equalTo("uuidValue", record.getUuidValue()).build()
                ).build()).block();

        assertEquals(1, results.getTotalCount());
        assertEquals(record.getUuidValue(), results.getResults().get(0).getRecord().getUuidValue());

        // byte array
        results = collection.searchAsync(
            null,
            VectorSearchOptions.builder()
                .withVectorSearchFilter(
                    VectorSearchFilter.builder()
                        .equalTo("byteArrayValue", record.getByteArrayValue()).build()
                ).build()).block();

        assertEquals(1, results.getTotalCount());
        assertArrayEquals(record.getByteArrayValue(), results.getResults().get(0).getRecord().getByteArrayValue());

        collection.deleteCollectionAsync().block();

    }


    public VectorStoreRecordCollection<String, ClassWithAllBoxedTypes> setupBoxed() {
        OracleVectorStoreQueryProvider queryProvider = OracleVectorStoreQueryProvider.builder()
            .withDataSource(DATA_SOURCE)
            .build();

        JDBCVectorStore vectorStore = JDBCVectorStore.builder()
            .withDataSource(DATA_SOURCE)
            .withOptions(JDBCVectorStoreOptions.builder()
                .withQueryProvider(queryProvider)
                .build())
            .build();

        VectorStoreRecordCollection<String, ClassWithAllBoxedTypes> collection =
            vectorStore.getCollection("BoxedTypes",
                JDBCVectorStoreRecordCollectionOptions.<ClassWithAllBoxedTypes>builder()
                    .withRecordClass(ClassWithAllBoxedTypes.class)
                    .build()).createCollectionAsync().block();

        collection.createCollectionAsync().block();

        return collection;
    }


    private static Stream<Arguments> supportedDataTypes() {
        return Stream.of(
            Arguments.of(
                new ClassWithAllBoxedTypes(
                    "ID1", true, (byte) 127, (short) 3, 321, 5L,
                    3.14f, 3.14159265358d, new BigDecimal("12345.67"),
                    OffsetDateTime.now(), UUID.randomUUID(), "abc".getBytes(StandardCharsets.UTF_8),
                    Arrays.asList(1.0f, 2.6f),
                    new Float[] { 0.5f, 3.2f, 7.1f, -4.0f, 2.8f, 10.0f, -1.3f, 5.5f }
                )
            ),
            Arguments.of(
                new ClassWithAllBoxedTypes(
                    "ID2", false, Byte.MIN_VALUE, Short.MIN_VALUE, Integer.MIN_VALUE, Long.MIN_VALUE,
                    Float.MIN_VALUE, MIN_DOUBLE, BigDecimal.valueOf(MIN_DECIMAL),
                    OffsetDateTime.now(), UUID.randomUUID(), new byte[] {Byte.MIN_VALUE, -10, 0, 10, Byte.MAX_VALUE},
                    Arrays.asList(Float.MIN_VALUE, -10f, 0f, 10f, Float.MAX_VALUE),
                    new Float[] { 0.5f, 3.2f, 7.1f, -4.0f, 2.8f, 10.0f, -1.3f, 5.5f }
                )
            ),
            Arguments.of(
                new ClassWithAllBoxedTypes(
                    "ID3", false, Byte.MAX_VALUE, Short.MAX_VALUE, Integer.MAX_VALUE, Long.MAX_VALUE,
                    Float.MAX_VALUE, BIG_NUMBER.doubleValue(), BIG_NUMBER.subtract(BigDecimal.valueOf(0.01d)),
                    OffsetDateTime.now(), UUID.randomUUID(), null,
                    null,
                    new Float[] { 0.5f, 3.2f, 7.1f, -4.0f, 2.8f, 10.0f, -1.3f, 5.5f }
                )
            ),
            Arguments.of(
                new ClassWithAllBoxedTypes(
                    "ID3", null, null, null, null, null,
                    null, null, null,
                    null, null, null,
                    null,
                    null
                )
            )
        );
    }

    private static Stream<Arguments> supportedDataPrimitiveTypes() {
        return Stream.of(
            Arguments.of(
                new ClassWithAllPrimitiveTypes(
                    "ID1", true, (byte) 127, (short) 3, 321, 5L,
                    3.14f, 3.14159265358d, new BigDecimal("12345.67"),
                    OffsetDateTime.now(), UUID.randomUUID(), "abc".getBytes(StandardCharsets.UTF_8),
                    Arrays.asList(1.0f, 2.6f),
                    new float[]{0.5f, 3.2f, 7.1f, -4.0f, 2.8f, 10.0f, -1.3f, 5.5f}
                )
            ),
            Arguments.of(
                new ClassWithAllPrimitiveTypes(
                    "ID2", false, Byte.MIN_VALUE, Short.MIN_VALUE, Integer.MIN_VALUE,
                    Long.MIN_VALUE,
                    Float.MIN_VALUE, MIN_DOUBLE, BigDecimal.valueOf(MIN_DECIMAL),
                    OffsetDateTime.now(), UUID.randomUUID(),
                    new byte[]{Byte.MIN_VALUE, -10, 0, 10, Byte.MAX_VALUE},
                    Arrays.asList(Float.MIN_VALUE, -10f, 0f, 10f, Float.MAX_VALUE),
                    new float[]{0.5f, 3.2f, 7.1f, -4.0f, 2.8f, 10.0f, -1.3f, 5.5f}
                )
            ),
            Arguments.of(
                new ClassWithAllPrimitiveTypes(
                    "ID3", false, Byte.MAX_VALUE, Short.MAX_VALUE, Integer.MAX_VALUE,
                    Long.MAX_VALUE,
                    Float.MAX_VALUE, BIG_NUMBER.doubleValue(),
                    BIG_NUMBER.subtract(BigDecimal.valueOf(0.01d)),
                    OffsetDateTime.now(), UUID.randomUUID(), null,
                    null,
                    new float[]{0.5f, 3.2f, 7.1f, -4.0f, 2.8f, 10.0f, -1.3f, 5.5f}
                )
            ),
            Arguments.of(
                new ClassWithAllPrimitiveTypes(
                    "ID3", false, (byte) 0, (short) 0, 0, 0l,
                    0f, 0d, null,
                    null, null, null,
                    null,
                    null
                )
            )
        );
    }
}
