// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class VolatileVectorStoreTest {

    private static VolatileVectorStore vectorStore;

    @BeforeAll
    public static void setup() {
        vectorStore = new VolatileVectorStore();
    }

    @Test
    public void collectionNamesAsync() {
        List<String> collectionNames = Arrays.asList("hotels1", "hotels2", "hotels3");

        for (String collectionName : collectionNames) {
            vectorStore.getCollection(collectionName,
                VolatileVectorStoreRecordCollectionOptions.<Hotel>builder()
                    .withRecordClass(Hotel.class)
                    .build())
                .createCollectionAsync()
                .block();
        }

        List<String> retrievedCollectionNames = vectorStore.getCollectionNamesAsync().block();
        assertNotNull(retrievedCollectionNames);
        assertEquals(collectionNames.size(), retrievedCollectionNames.size());
        for (String collectionName : collectionNames) {
            assertTrue(retrievedCollectionNames.contains(collectionName));
        }
    }
}
