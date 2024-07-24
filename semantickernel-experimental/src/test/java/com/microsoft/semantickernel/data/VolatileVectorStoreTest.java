// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
            vectorStore.getCollection(collectionName, Hotel.class, null).createCollectionAsync()
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
