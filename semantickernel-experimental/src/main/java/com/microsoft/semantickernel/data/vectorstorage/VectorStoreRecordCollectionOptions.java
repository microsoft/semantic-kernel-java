// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.vectorstorage;

import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordDefinition;

/**
 * Represents the options for a collection of vector store records.
 *
 * @param <Key> the type of the key
 * @param <Record> the type of the record
 */
public interface VectorStoreRecordCollectionOptions<Key, Record> {
    /**
     * Gets the key class.
     *
     * @return the key class
     */
    Class<Key> getKeyClass();

    /**
     * Gets the record class.
     *
     * @return the record class
     */
    Class<Record> getRecordClass();

    /**
     * Gets the record definition.
     *
     * @return the record definition
     */
    VectorStoreRecordDefinition getRecordDefinition();
}
