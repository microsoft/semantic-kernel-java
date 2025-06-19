package com.microsoft.semantickernel.data.jdbc.oracle;

import com.microsoft.semantickernel.data.jdbc.JDBCVectorStore;
import com.microsoft.semantickernel.data.jdbc.JDBCVectorStoreOptions;
import com.microsoft.semantickernel.data.jdbc.JDBCVectorStoreRecordCollectionOptions;
import com.microsoft.semantickernel.data.vectorstorage.VectorStoreRecordCollection;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OracleVectorStoreDataTypeTest extends OracleCommonVectorStoreRecordCollectionTest {
    private static final double MIN_NUMBER = 1.0E-130;
    private static final BigDecimal BIG_NUMBER = BigDecimal.valueOf(9999999999999999.99);

    @ParameterizedTest
    @MethodSource("supportedDataTypes")
    void testDataTypes(ClassWithAllBoxedTypes values) {

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

        ClassWithAllBoxedTypes record = values;

        collection.upsertAsync(record, null).block();

        ClassWithAllBoxedTypes result = collection.getAsync(values.getId(), null).block();
        assertNotNull(result);

        assertEquals(values.getBooleanValue(), result.getBooleanValue());
        assertArrayEquals(values.getByteArrayValue(), result.getByteArrayValue());
        assertEquals(values.getByteValue(), result.getByteValue());
        assertEquals(values.getDoubleValue(), result.getDoubleValue());
        assertEquals(values.getFloatValue(), result.getFloatValue());
        assertEquals(values.getIntegerValue(), result.getIntegerValue());
        assertEquals(values.getListOfFloatValue(), result.getListOfFloatValue());
        assertEquals(values.getLongValue(), result.getLongValue());
        if (values.getOffsetDateTimeValue() != null) {
            assertTrue(values.getOffsetDateTimeValue().isEqual(result.getOffsetDateTimeValue()));
        } else {
            assertTrue(result.getOffsetDateTimeValue() == null);
        }
        assertEquals(values.getShortValue(), result.getShortValue());
        assertEquals(values.getUuidValue(), result.getUuidValue());

        collection.deleteCollectionAsync().block();
    }

    @ParameterizedTest
    @MethodSource("supportedDataPrimitiveTypes")
    void testPrimitiveDataTypes(ClassWithAllPrimitiveTypes values) {

        OracleVectorStoreQueryProvider queryProvider = OracleVectorStoreQueryProvider.builder()
            .withDataSource(DATA_SOURCE)
            .build();

        JDBCVectorStore vectorStore = JDBCVectorStore.builder()
            .withDataSource(DATA_SOURCE)
            .withOptions(JDBCVectorStoreOptions.builder()
                .withQueryProvider(queryProvider)
                .build())
            .build();

        VectorStoreRecordCollection<String, ClassWithAllPrimitiveTypes> collection =
            vectorStore.getCollection("PrimitiveTypes",
                JDBCVectorStoreRecordCollectionOptions.<ClassWithAllPrimitiveTypes>builder()
                    .withRecordClass(ClassWithAllPrimitiveTypes.class)
                    .build()).createCollectionAsync().block();

        collection.createCollectionAsync().block();

        ClassWithAllPrimitiveTypes record = values;

        collection.upsertAsync(record, null).block();

        ClassWithAllPrimitiveTypes result = collection.getAsync(values.getId(), null).block();
        assertNotNull(result);

        assertEquals(values.getBooleanValue(), result.getBooleanValue());
        assertArrayEquals(values.getByteArrayValue(), result.getByteArrayValue());
        assertEquals(values.getByteValue(), result.getByteValue());
        assertEquals(values.getDoubleValue(), result.getDoubleValue());
        assertEquals(values.getFloatValue(), result.getFloatValue());
        assertEquals(values.getIntegerValue(), result.getIntegerValue());
        assertEquals(values.getListOfFloatValue(), result.getListOfFloatValue());
        assertEquals(values.getLongValue(), result.getLongValue());
        if (values.getOffsetDateTimeValue() != null) {
            assertTrue(values.getOffsetDateTimeValue().isEqual(result.getOffsetDateTimeValue()));
        } else {
            assertTrue(result.getOffsetDateTimeValue() == null);
        }
        assertEquals(values.getShortValue(), result.getShortValue());
        assertEquals(values.getUuidValue(), result.getUuidValue());

        collection.deleteCollectionAsync().block();
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
                    Float.MIN_VALUE, MIN_NUMBER, BigDecimal.valueOf(MIN_NUMBER),
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
                    new float[] { 0.5f, 3.2f, 7.1f, -4.0f, 2.8f, 10.0f, -1.3f, 5.5f }
                )
            ),
            Arguments.of(
                new ClassWithAllPrimitiveTypes(
                    "ID2", false, Byte.MIN_VALUE, Short.MIN_VALUE, Integer.MIN_VALUE, Long.MIN_VALUE,
                    Float.MIN_VALUE, MIN_NUMBER, BigDecimal.valueOf(MIN_NUMBER),
                    OffsetDateTime.now(), UUID.randomUUID(), new byte[] {Byte.MIN_VALUE, -10, 0, 10, Byte.MAX_VALUE},
                    Arrays.asList(Float.MIN_VALUE, -10f, 0f, 10f, Float.MAX_VALUE),
                    new float[] { 0.5f, 3.2f, 7.1f, -4.0f, 2.8f, 10.0f, -1.3f, 5.5f }
                )
            ),
            Arguments.of(
                new ClassWithAllPrimitiveTypes(
                    "ID3", false, Byte.MAX_VALUE, Short.MAX_VALUE, Integer.MAX_VALUE, Long.MAX_VALUE,
                    Float.MAX_VALUE, BIG_NUMBER.doubleValue(), BIG_NUMBER.subtract(BigDecimal.valueOf(0.01d)),
                    OffsetDateTime.now(), UUID.randomUUID(), null,
                    null,
                    new float[] { 0.5f, 3.2f, 7.1f, -4.0f, 2.8f, 10.0f, -1.3f, 5.5f }
                )
            ),
            Arguments.of(
                new ClassWithAllPrimitiveTypes(
                    "ID3", false, (byte)0, (short)0, 0, 0l,
                    0f, 0d, null,
                    null, null, null,
                    null,
                    null
                )
            )
        );
    }

}
