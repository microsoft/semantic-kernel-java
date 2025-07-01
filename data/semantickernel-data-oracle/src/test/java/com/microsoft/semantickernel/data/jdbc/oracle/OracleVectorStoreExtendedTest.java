package com.microsoft.semantickernel.data.jdbc.oracle;

import com.microsoft.semantickernel.data.jdbc.JDBCVectorStore;
import com.microsoft.semantickernel.data.jdbc.JDBCVectorStoreOptions;
import com.microsoft.semantickernel.data.jdbc.JDBCVectorStoreRecordCollectionOptions;
import com.microsoft.semantickernel.data.jdbc.oracle.OracleVectorStoreQueryProvider.StringTypeMapping;
import com.microsoft.semantickernel.data.vectorsearch.VectorSearchFilter;
import com.microsoft.semantickernel.data.vectorsearch.VectorSearchResults;
import com.microsoft.semantickernel.data.vectorstorage.VectorStoreRecordCollection;
import com.microsoft.semantickernel.data.vectorstorage.annotations.VectorStoreRecordData;
import com.microsoft.semantickernel.data.vectorstorage.annotations.VectorStoreRecordKey;
import com.microsoft.semantickernel.data.vectorstorage.annotations.VectorStoreRecordVector;
import com.microsoft.semantickernel.data.vectorstorage.definition.DistanceFunction;
import com.microsoft.semantickernel.data.vectorstorage.definition.IndexKind;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordDefinition;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordKeyField;
import com.microsoft.semantickernel.data.vectorstorage.options.GetRecordOptions;
import com.microsoft.semantickernel.data.vectorstorage.options.VectorSearchOptions;
import com.microsoft.semantickernel.exceptions.SKException;

import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OracleVectorStoreExtendedTest extends OracleCommonVectorStoreRecordCollectionTest {

    // Test vector types
    @Test
    void testUseStringVec() {
        VectorStoreRecordCollection<String, DummyRecordForVecString> collection =
            createCollection(
                "use_string_vec",
                DummyRecordForVecString.class,
                null);

        DummyRecordForVecString d1 = new DummyRecordForVecString("id1", "description1", "[1.1, 2.2, 3.3, 4.4, 5.5, 6.6, 7.7, 8.8]");
        DummyRecordForVecString d2 = new DummyRecordForVecString("id2", "description2", "[1.1, 2.2, 3.3, 4.4, 5.5, 6.6, 7.7, 8.8]");

        collection.upsertBatchAsync(Arrays.asList(d1,d2), null).block();

        DummyRecordForVecString rec = collection.getAsync("id1",
            GetRecordOptions.builder().includeVectors(true).build()).block();

        assertNotNull(rec);
        assertEquals("[1.1,2.2,3.3,4.4,5.5,6.6,7.7,8.8]", rec.getVec());

        collection.deleteCollectionAsync().block();
    }

    @Test
    void testUseCollectionVec() {
        VectorStoreRecordCollection<String, DummyRecordForVecCollection> collection =
            createCollection(
                "use_collection_vec",
                DummyRecordForVecCollection.class,
                null);

        List<Float> v1 = Arrays.asList(0.5f, 3.2f, 7.1f, -4.0f, 2.8f, 10f, -1.3f, 5.5f);
        List<Float> v2 = Arrays.asList(-2f,  8.1f, 0.9f,  5.4f, -3.3f, 2.2f, 9.9f, -4.5f);
        DummyRecordForVecCollection d1 = new DummyRecordForVecCollection("id1", "", v1);
        DummyRecordForVecCollection d2 = new DummyRecordForVecCollection("id2", "", v2);

        collection.upsertBatchAsync(Arrays.asList(d1,d2), null).block();

        DummyRecordForVecCollection rec = collection.getAsync("id1",
            GetRecordOptions.builder().includeVectors(true).build()).block();

        assertNotNull(rec);
        assertEquals(8, rec.getVec().size());
        assertIterableEquals(v1, rec.getVec());

        collection.deleteCollectionAsync().block();
    }

    // Test corner-case
    @Test
    void testUseCLOB() {
        VectorStoreRecordCollection<String, DummyRecordForCLOB> collection =
            createCollection(
                "use_clob",
                DummyRecordForCLOB.class,
                OracleVectorStoreQueryProvider.StringTypeMapping.USE_CLOB);

        DummyRecordForCLOB d1 = new DummyRecordForCLOB("id1", "clob-description", null);
        DummyRecordForCLOB d2 = new DummyRecordForCLOB("id2", "clob-description2", vec(0));

        collection.upsertBatchAsync(Arrays.asList(d1,d2), null).block();

        try (Connection c = DATA_SOURCE.getConnection()) {
            PreparedStatement st = c.prepareStatement(
                "SELECT DATA_TYPE FROM USER_TAB_COLUMNS " +
                    "WHERE TABLE_NAME = 'SKCOLLECTION_USE_CLOB' AND COLUMN_NAME = 'DESCRIPTION'"
            );
            ResultSet rs = st.executeQuery();
            rs.next();
            assertEquals("CLOB", rs.getString(1));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            collection.deleteCollectionAsync().block();
        }
    }

    @Test
    void testClobLongText() {
        VectorStoreRecordCollection<String, DummyRecordForCLOB> collection =
            createCollection(
                "clob_long_text",
                DummyRecordForCLOB.class,
                OracleVectorStoreQueryProvider.StringTypeMapping.USE_CLOB);

        String longText = String.join("", java.util.Collections.nCopies(6000, "a"));
        DummyRecordForCLOB r = new DummyRecordForCLOB("big", longText, vec(0));
        collection.upsertAsync(r, null).block();

        DummyRecordForCLOB out = collection.getAsync("big", null).block();
        assertEquals(longText.length(), out.getDescription().length());
        assertTrue(out.getDescription().startsWith("aaaa"));

        collection.deleteCollectionAsync().block();
    }

    @Test
    void testMultipleFilter() {
        VectorStoreRecordCollection<String, DummyRecordForMultipleFilter> collection =
            createCollection(
                "multiple_filter", 
                DummyRecordForMultipleFilter.class, 
                null);

        DummyRecordForMultipleFilter d1 = new DummyRecordForMultipleFilter("id1", 4, 120,  floatVec(0f));
        DummyRecordForMultipleFilter d2 = new DummyRecordForMultipleFilter("id2", 4, 100,  floatVec(0f));
        DummyRecordForMultipleFilter d3 = new DummyRecordForMultipleFilter("id3", 3, 100,  floatVec(0f));

        collection.upsertBatchAsync(Arrays.asList(d1,d2,d3), null).block();

        VectorSearchFilter filter = VectorSearchFilter.builder()
            .equalTo("price",100)
            .equalTo("stars", 4)
            .build();

        VectorSearchResults<DummyRecordForMultipleFilter> results =
            collection.searchAsync(null,
                VectorSearchOptions.builder()
                .withVectorSearchFilter(filter)
                .build()
            ).block();

        assertEquals(1, results.getTotalCount());
        assertEquals("id2", results.getResults().get(0).getRecord().getId());

        collection.deleteCollectionAsync().block();
    }

    @Test
    void testVectorDimensionMismatch() {
        VectorStoreRecordCollection<String, DummyRecord> collection =
            createCollection(
                "vector_dimension_mismatch", 
                DummyRecord.class, 
                null);

        // Empty vector rejected
        DummyRecord d1 = new DummyRecord("id1", 4, 120d,  new float[]{});
        SKException ex = assertThrows(SKException.class,
            () -> collection.upsertBatchAsync(Arrays.asList(d1), null).block());
        System.out.println(ex.getMessage());
        assertTrue(ex.getCause().getMessage().contains("ORA-51803"));

        // Vector dimension mismatch
        DummyRecord d2 = new DummyRecord("id1", 4, 120d,  new float[]{1.1f,2.2f,3.3f,4.4f,5.5f});
        SKException ex2 = assertThrows(SKException.class,
            () -> collection.upsertBatchAsync(Arrays.asList(d2), null).block());
        assertTrue(ex2.getCause().getMessage().contains("ORA-51803"));

        collection.deleteCollectionAsync().block();
    }

    @Test
    void testNullFieldValue() {
        VectorStoreRecordCollection<String, DummyRecord> collection =
            createCollection("test_null", DummyRecord.class, null);

        DummyRecord d1 = new DummyRecord("id1", 4, null,  floatVec(1));
        collection.upsertBatchAsync(Arrays.asList(d1), null).block();

        VectorSearchFilter filter = VectorSearchFilter.builder()
            .equalTo("price",null)//
            .build();

        VectorSearchResults<DummyRecord> results = collection.searchAsync(
            null,
            VectorSearchOptions.builder()
                .withVectorSearchFilter(filter)
                .build()
        ).block();

        assertEquals(1, results.getTotalCount());
        assertEquals("id1", results.getResults().get(0).getRecord().getId());

        collection.deleteCollectionAsync().block();
    }

    @Test
    void testSkipAndTop() {
        VectorStoreRecordCollection<String, DummyRecord> collection =
            createCollection(
                "test_skip_and_top",
                DummyRecord.class,
                null);

        List<DummyRecord> l1 = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            l1.add(new DummyRecord("id" + i, i, (double) i, floatVec(i)));
        }
        collection.upsertBatchAsync(l1, null).block();

        VectorSearchResults<DummyRecord> results = collection.searchAsync(
            Collections.nCopies(8,0f),
            VectorSearchOptions.builder()
                .withIncludeVectors(true)
                .withSkip(5)
                .withTop(3)
                .build()
        ).block();

        assertEquals(3, results.getResults().size());
        List<String> ids = results.getResults().stream().map(r -> r.getRecord().getId()).collect(
            Collectors.toList());
        assertEquals(Arrays.asList("id6","id7","id8"), ids);

        collection.deleteCollectionAsync().block();
    }

    // corner case for OracleVectorStoreRecordMapper
    @Test
    void testMapRecordToStorageModel_throws() {
        VectorStoreRecordKeyField keyField = VectorStoreRecordKeyField.builder()
            .withName("id")
            .withStorageName("id")
            .withFieldType(String.class)
            .build();

        VectorStoreRecordDefinition definition =
            VectorStoreRecordDefinition.fromFields(
                Arrays.asList(keyField)
            );

        OracleVectorStoreRecordMapper<DummyRecord> mapper =
            OracleVectorStoreRecordMapper.<DummyRecord> builder()
                .withRecordClass(DummyRecord.class)
                .withVectorStoreRecordDefinition(definition)
                .build();

        UnsupportedOperationException ex = assertThrows(
            UnsupportedOperationException.class,
            () -> mapper.mapRecordToStorageModel(new DummyRecord()));
        assertEquals("Not implemented", ex.getMessage());
    }

    private <T> VectorStoreRecordCollection<String, T> createCollection(
        String collectionName,
        Class<T> recordClass,
        OracleVectorStoreQueryProvider.StringTypeMapping stringTypeMapping) {

        OracleVectorStoreQueryProvider.Builder builder =
            OracleVectorStoreQueryProvider.builder()
            .withDataSource(DATA_SOURCE);

        if (stringTypeMapping != null) {
            builder.withStringTypeMapping(stringTypeMapping);
        }
        OracleVectorStoreQueryProvider queryProvider = builder.build();

        JDBCVectorStore vectorStore = JDBCVectorStore.builder()
            .withDataSource(DATA_SOURCE)
            .withOptions(JDBCVectorStoreOptions.builder()
                .withQueryProvider(queryProvider)
                .build())
            .build();

        VectorStoreRecordCollection<String, T> collection =
            vectorStore.getCollection(collectionName,
                JDBCVectorStoreRecordCollectionOptions.<T>builder()
                    .withRecordClass(recordClass)
                    .build()).createCollectionAsync().block();

        return collection;
    }

    private List<Float> vec(float x) {
        return Arrays.asList(x, x+1, x+2, x+3, x+4, x+5, x+6, x+7);
    }

    private float[] floatVec(float x) {
        return new float[] { x, x+1, x+2, x+3, x+4, x+5, x+6, x+7 };
    }

    private static class DummyRecordForVecString {
        @VectorStoreRecordKey
        private final String id;

        @VectorStoreRecordData(isFilterable = false)
        private final String description;

        @VectorStoreRecordVector(dimensions = 8, distanceFunction = DistanceFunction.COSINE_DISTANCE, indexKind = IndexKind.IVFFLAT)
        private final String vec;

        public DummyRecordForVecString() {
            this(null, null, null);
        }
        public DummyRecordForVecString(String id, String description, String vec) {
            this.id = id;
            this.description = description;
            this.vec = vec;
        }

        public String getId() {
            return id;
        }
        public String getDescription() {
            return description;
        }
        public String getVec() {
            return vec;
        }
    }

    private static class DummyRecordForVecCollection{
        @VectorStoreRecordKey
        private String id;

        @VectorStoreRecordData(isFilterable = false)
        private String description;

        @VectorStoreRecordVector(dimensions = 8, distanceFunction = DistanceFunction.COSINE_DISTANCE, indexKind = IndexKind.IVFFLAT)
        private Collection<Float> vec;

        public DummyRecordForVecCollection() {
            this(null, null, null);
        }
        public DummyRecordForVecCollection(String id, String description, Collection<Float> vec) {
            this.id = id;
            this.description = description;
            this.vec = vec;
        }

        public String getId() {
            return id;
        }
        public String getDescription() {
            return description;
        }
        public Collection<Float> getVec() {
            return vec;
        }
    }

    private static class DummyRecordForCLOB {
        @VectorStoreRecordKey
        private String id;

        @VectorStoreRecordData(isFilterable = false)
        private String description;

        @VectorStoreRecordVector(dimensions = 8, distanceFunction = DistanceFunction.COSINE_DISTANCE, indexKind = IndexKind.IVFFLAT)
        private List<Float> vec;

        private DummyRecordForCLOB() {
            this(null, null, null);
        }
      private DummyRecordForCLOB(String id, String description, List<Float> vec) {
        this.id = id;
        this.description = description;
        this.vec = vec;
      }

      public String getId() {
          return id;
      }
      public String getDescription() {
          return description;
      }
      public List<Float> getVec() {
          return vec;
      }
    }

    private static class DummyRecordForMultipleFilter {
        @VectorStoreRecordKey
        private String id;

        @VectorStoreRecordData(isFilterable = true)
        private int stars;

        @VectorStoreRecordData(isFilterable = true)
        private double price;

        @VectorStoreRecordVector(dimensions = 8, distanceFunction = DistanceFunction.COSINE_DISTANCE, indexKind = IndexKind.IVFFLAT)
        private float[] vec;

        public DummyRecordForMultipleFilter() {
            this(null, 0, 0d, null);
        }

        public DummyRecordForMultipleFilter(String id, int stars, double price, float[] vec) {
            this.id = id;
            this.stars = stars;
            this.price = price;
            this.vec = vec;
        }

        public String getId() {
            return id;
        }
        public int getStars() {
            return stars;
        }
        public double getPrice() {
            return price;
        }
        public float[] getVec() {
            return vec;
        }
    }

    private static class DummyRecord {
        @VectorStoreRecordKey
        private String id;

        @VectorStoreRecordData(isFilterable = true)
        private int stars;

        @VectorStoreRecordData(isFilterable = true)
        private Double price;

        @VectorStoreRecordVector(dimensions = 8, distanceFunction = DistanceFunction.COSINE_DISTANCE, indexKind = IndexKind.IVFFLAT)
        private float[] vec;

        public DummyRecord() {
            this(null, 0, 0d, null);
        }

        public DummyRecord(String id, int stars, Double price, float[] vec) {
            this.id = id;
            this.stars = stars;
            this.price = price;
            this.vec = vec;
        }

        public String getId() {
            return id;
        }
        public int getStars() {
            return stars;
        }
        public Double getPrice() {
            return price;
        }
        public float[] getVec() {
            return vec;
        }
    }
}
