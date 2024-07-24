// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    private List<Hotel> getHotels() {
        return Arrays.asList(
            new Hotel("id_1", "Hotel 1", 1, "Hotel 1 description", Arrays.asList(1.0f, 2.0f, 3.0f),
                4.0),
            new Hotel("id_2", "Hotel 2", 2, "Hotel 2 description", Arrays.asList(1.0f, 2.0f, 3.0f),
                3.0),
            new Hotel("id_3", "Hotel 3", 3, "Hotel 3 description", Arrays.asList(1.0f, 2.0f, 3.0f),
                5.0),
            new Hotel("id_4", "Hotel 4", 4, "Hotel 4 description", Arrays.asList(1.0f, 2.0f, 3.0f),
                4.0),
            new Hotel("id_5", "Hotel 5", 5, "Hotel 5 description", Arrays.asList(1.0f, 2.0f, 3.0f),
                5.0));
    }

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
}
