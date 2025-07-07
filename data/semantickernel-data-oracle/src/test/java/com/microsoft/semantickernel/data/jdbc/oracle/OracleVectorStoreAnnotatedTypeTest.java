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

import com.microsoft.semantickernel.data.jdbc.JDBCVectorStore;
import com.microsoft.semantickernel.data.jdbc.JDBCVectorStoreOptions;
import com.microsoft.semantickernel.data.jdbc.JDBCVectorStoreRecordCollectionOptions;
import com.microsoft.semantickernel.data.vectorstorage.VectorStoreRecordCollection;
import com.microsoft.semantickernel.data.vectorstorage.definition.DistanceFunction;
import com.microsoft.semantickernel.data.vectorstorage.definition.IndexKind;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordDataField;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordDefinition;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordKeyField;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordVectorField;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class OracleVectorStoreAnnotatedTypeTest extends OracleCommonVectorStoreRecordCollectionTest {

    @ParameterizedTest
    @MethodSource("supportedDataTypes")
    void testDataTypes(String dataFieldName, Class<?> dataFieldType, Object dataFieldValue, Class<?> fieldSubType) {
        VectorStoreRecordKeyField keyField = VectorStoreRecordKeyField.builder()
            .withName("id")
            .withStorageName("id")
            .withFieldType(String.class)
            .build();

        VectorStoreRecordDataField dataField;
        if (fieldSubType != null) {
            dataField = VectorStoreRecordDataField.builder()
                .withName("value")
                .withStorageName("value_field")
                .withFieldType(dataFieldType, fieldSubType)
                .isFilterable(true)
                .build();
        } else {
            dataField = VectorStoreRecordDataField.builder()
                .withName("value")
                .withStorageName("value_field")
                .withFieldType(dataFieldType)
                .isFilterable(true)
                .build();
        }
        VectorStoreRecordDataField dataTypeField;
            dataTypeField = VectorStoreRecordDataField.builder()
                .withName("valueType")
                .withStorageName("value_type")
                .withFieldType(String.class)
                .isFilterable(false)
                .build();


        VectorStoreRecordVectorField dummyVector = VectorStoreRecordVectorField.builder()
            .withName("vectorValue")
            .withStorageName("vectorValue")
            .withFieldType(Float[].class)
            .withDimensions(8)
            .withDistanceFunction(DistanceFunction.COSINE_DISTANCE)
            .withIndexKind(IndexKind.IVFFLAT)
            .build();

        VectorStoreRecordDefinition definition = VectorStoreRecordDefinition.fromFields(
            Arrays.asList(keyField, dataTypeField, dataField, dummyVector)
        );

        OracleVectorStoreQueryProvider queryProvider = OracleVectorStoreQueryProvider.builder()
            .withDataSource(DATA_SOURCE)
            .build();

        JDBCVectorStore vectorStore = JDBCVectorStore.builder()
            .withDataSource(DATA_SOURCE)
            .withOptions(JDBCVectorStoreOptions.builder()
                .withQueryProvider(queryProvider)
                .build())
            .build();

        String collectionName = "test_datatype_" + dataFieldName;

        VectorStoreRecordCollection<String, ClassWithAnnotatedTypes> collection =
            vectorStore.getCollection(collectionName,
                JDBCVectorStoreRecordCollectionOptions.<ClassWithAnnotatedTypes> builder()
                    .withRecordClass(ClassWithAnnotatedTypes.class)
                    .withRecordDefinition(definition).build());

        collection.createCollectionAsync().block();

        String key = "testid";

        ClassWithAnnotatedTypes record =
            new ClassWithAnnotatedTypes(key, dataFieldName, dataFieldValue, new Float[] { 0.5f, 3.2f, 7.1f, -4.0f, 2.8f, 10.0f, -1.3f, 5.5f });

        collection.upsertAsync(record, null).block();

        ClassWithAnnotatedTypes result = collection.getAsync(key, null).block();
        assertNotNull(result);
        if (record.getValue().getClass().equals(OffsetDateTime.class)) {
            assertTrue(((OffsetDateTime)dataFieldValue).isEqual((OffsetDateTime)record.getValue()));
        } else if (dataFieldName == "byte_array") {
            assertArrayEquals((byte[]) dataFieldValue, (byte[])record.getValue());
        } else {
            assertEquals(dataFieldValue, result.getValue());
        }

        collection.deleteCollectionAsync().block();
    }

    private static Stream<Arguments> supportedDataTypes() {
        return Stream.of(
            Arguments.of("string", String.class, "asd123", null),
            Arguments.of("boolean", Boolean.class, true, null),
            Arguments.of("boolean", Boolean.class, false, null),
            Arguments.of("byte", Byte.class, (byte) 127, null),
            Arguments.of("short", Short.class, (short) 3, null),
            Arguments.of("integer", Integer.class, 321, null),
            Arguments.of("long", Long.class, 5L, null),
            Arguments.of("float", Float.class, 3.14f, null),
            Arguments.of("double", Double.class, 3.14159265358d, null),
            Arguments.of("decimal", BigDecimal.class, new BigDecimal("12345.67"), null),
            Arguments.of("timestamp", OffsetDateTime.class, OffsetDateTime.now(), null),
            Arguments.of("uuid", UUID.class, UUID.randomUUID(), null),
            Arguments.of("byte_array", byte[].class, new byte[] {1, 2, 3}, String.class),
            Arguments.of("json", List.class, Arrays.asList("a", "s", "d"), String.class)
        );
    }

}
