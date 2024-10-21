// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.microsoft.semantickernel.connectors.data.jdbc.JDBCVectorStoreRecordCollection;
import com.microsoft.semantickernel.connectors.data.jdbc.filter.SQLEqualToFilterClause;
import com.microsoft.semantickernel.data.filter.EqualToFilterClause;
import com.microsoft.semantickernel.data.vectorsearch.VectorSearchFilter;
import com.microsoft.semantickernel.data.vectorsearch.VectorSearchResult;
import com.microsoft.semantickernel.data.vectorstorage.definition.DistanceFunction;
import com.microsoft.semantickernel.data.vectorstorage.options.VectorSearchOptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

public class VolatileVectorStoreRecordCollectionTest {

    private static VolatileVectorStoreRecordCollection<Hotel> recordCollection;

    @BeforeAll
    public static void setup() {
        recordCollection = new VolatileVectorStoreRecordCollection<>(
            "hotels",
            VolatileVectorStoreRecordCollectionOptions.<Hotel>builder()
                .withRecordClass(Hotel.class)
                .build());
        recordCollection.createCollectionIfNotExistsAsync().block();
    }

    @BeforeEach
    public void clearCollection() {
        recordCollection.deleteCollectionAsync().block();
        recordCollection.createCollectionAsync().block();
    }

    private static List<Hotel> getHotels() {
        return Arrays.asList(
            new Hotel("id_1", "Hotel 1", 1, "Hotel 1 description",
                Arrays.asList(0.5f, 3.2f, 7.1f, -4.0f, 2.8f, 10.0f, -1.3f, 5.5f), null, null, null,
                4.0),
            new Hotel("id_2", "Hotel 2", 2, "Hotel 2 description",
                Arrays.asList(-2.0f, 8.1f, 0.9f, 5.4f, -3.3f, 2.2f, 9.9f, -4.5f), null, null, null,
                4.0),
            new Hotel("id_3", "Hotel 3", 3, "Hotel 3 description",
                Arrays.asList(4.5f, -6.2f, 3.1f, 7.7f, -0.8f, 1.1f, -2.2f, 8.3f), null, null, null,
                5.0),
            new Hotel("id_4", "Hotel 4", 4, "Hotel 4 description",
                Arrays.asList(7.0f, 1.2f, -5.3f, 2.5f, 6.6f, -7.8f, 3.9f, -0.1f), null, null, null,
                4.0),
            new Hotel("id_5", "Hotel 5", 5, "Hotel 5 description",
                Arrays.asList(-3.5f, 4.4f, -1.2f, 9.9f, 5.7f, -6.1f, 7.8f, -2.0f), null, null, null,
                4.0));
    }

    /**
     * Search embeddings similar to the third hotel embeddings.
     * In order of similarity:
     * 1. Hotel 3
     * 2. Hotel 1
     * 3. Hotel 4
     */
    private static final List<Float> SEARCH_EMBEDDINGS = Arrays.asList(4.5f, -6.2f, 3.1f, 7.7f,
        -0.8f, 1.1f, -2.2f, 8.2f);

    @Test
    public void createAndDeleteCollectionAsync() {
        assertEquals(true, recordCollection.collectionExistsAsync().block());

        recordCollection.deleteCollectionAsync().block();
        assertEquals(false, recordCollection.collectionExistsAsync().block());

        recordCollection.createCollectionAsync().block();
        assertEquals(true, recordCollection.collectionExistsAsync().block());
    }

    @Test
    public void upsertRecordAsync() {
        List<Hotel> hotels = getHotels();
        for (Hotel hotel : hotels) {
            recordCollection.upsertAsync(hotel, null).block();
        }

        for (Hotel hotel : hotels) {
            Hotel retrievedHotel = recordCollection.getAsync(hotel.getId(), null).block();
            assertNotNull(retrievedHotel);
            assertEquals(hotel.getId(), retrievedHotel.getId());
            assertEquals(hotel.getName(), retrievedHotel.getName());
            assertEquals(hotel.getDescription(), retrievedHotel.getDescription());
        }
    }

    @Test
    public void upsertBatchAsync() {
        List<Hotel> hotels = getHotels();
        recordCollection.upsertBatchAsync(hotels, null).block();

        for (Hotel hotel : hotels) {
            Hotel retrievedHotel = recordCollection.getAsync(hotel.getId(), null).block();
            assertNotNull(retrievedHotel);
            assertEquals(hotel.getId(), retrievedHotel.getId());
            assertEquals(hotel.getName(), retrievedHotel.getName());
            assertEquals(hotel.getDescription(), retrievedHotel.getDescription());
        }
    }

    @Test
    public void getBatchAsync() {
        List<Hotel> hotels = getHotels();
        recordCollection.upsertBatchAsync(hotels, null).block();

        List<String> keys = hotels.stream().map(Hotel::getId).collect(Collectors.toList());
        List<Hotel> retrievedHotels = recordCollection.getBatchAsync(keys, null).block();

        assertNotNull(retrievedHotels);
        assertEquals(keys.size(), retrievedHotels.size());
        for (Hotel hotel : retrievedHotels) {
            assertTrue(keys.contains(hotel.getId()));
        }
    }

    @Test
    public void deleteRecordAsync() {
        List<Hotel> hotels = getHotels();
        recordCollection.upsertBatchAsync(hotels, null).block();

        for (Hotel hotel : hotels) {
            recordCollection.deleteAsync(hotel.getId(), null).block();
            assertNull(recordCollection.getAsync(hotel.getId(), null).block());
        }
    }

    @Test
    public void deleteBatchAsync() {
        List<Hotel> hotels = getHotels();
        recordCollection.upsertBatchAsync(hotels, null).block();

        List<String> keys = hotels.stream().map(Hotel::getId).collect(Collectors.toList());
        recordCollection.deleteBatchAsync(keys, null).block();

        for (String key : keys) {
            assertNull(recordCollection.getAsync(key, null).block());
        }
    }

    @ParameterizedTest
    @EnumSource(DistanceFunction.class)
    public void exactSearch(DistanceFunction distanceFunction) {
        List<Hotel> hotels = getHotels();
        recordCollection.upsertBatchAsync(hotels, null).block();

        VectorSearchOptions options = VectorSearchOptions.builder()
            .withVectorFieldName(distanceFunction.getValue())
            .withLimit(3)
            .build();

        // Embeddings similar to the third hotel
        List<VectorSearchResult<Hotel>> results = recordCollection
            .searchAsync(SEARCH_EMBEDDINGS, options).block();
        assertNotNull(results);
        assertEquals(3, results.size());
        // The third hotel should be the most similar
        assertEquals(hotels.get(2).getId(), results.get(0).getRecord().getId());

        options = VectorSearchOptions.builder()
            .withVectorFieldName(distanceFunction.getValue())
            .withOffset(1)
            .withLimit(-100)
            .build();

        // Skip the first result
        results = recordCollection.searchAsync(SEARCH_EMBEDDINGS, options).block();
        assertNotNull(results);
        assertEquals(1, results.size());
        // The first hotel should be the most similar
        assertEquals(hotels.get(0).getId(), results.get(0).getRecord().getId());
    }

    @ParameterizedTest
    @EnumSource(DistanceFunction.class)
    public void searchWithFilter(DistanceFunction distanceFunction) {
        List<Hotel> hotels = getHotels();
        recordCollection.upsertBatchAsync(hotels, null).block();

        VectorSearchOptions options = VectorSearchOptions.builder()
            .withVectorFieldName(distanceFunction.getValue())
            .withLimit(3)
            .withVectorSearchFilter(
                VectorSearchFilter.builder()
                    .withEqualToFilterClause(new EqualToFilterClause("rating", 4.0)).build())
            .build();

        // Embeddings similar to the third hotel, but as the filter is set to 4.0, the third hotel should not be returned
        List<VectorSearchResult<Hotel>> results = recordCollection
            .searchAsync(SEARCH_EMBEDDINGS, options).block();
        assertNotNull(results);
        assertEquals(3, results.size());
        // The first hotel should be the most similar
        assertEquals(hotels.get(0).getId(), results.get(0).getRecord().getId());
    }
}
