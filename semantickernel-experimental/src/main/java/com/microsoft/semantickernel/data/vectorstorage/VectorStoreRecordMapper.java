// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.vectorstorage;

import com.microsoft.semantickernel.data.vectorstorage.options.GetRecordOptions;

import javax.annotation.Nullable;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A mapper to convert between a record and a storage model.
 *
 * @param <Record> the record type
 * @param <StorageModel> the storage model type
 */
public class VectorStoreRecordMapper<Record, StorageModel> {
    @Nullable
    private final Function<Record, StorageModel> recordToStorageModelMapper;
    private final BiFunction<StorageModel, GetRecordOptions, Record> storageModelToRecordMapper;

    /**
     * Constructs a new instance of the VectorStoreRecordMapper.
     *
     * @param recordToStorageModelMapper the function to convert a record to a storage model
     * @param storageModelToRecordMapper the function to convert a storage model to a record
     */
    protected VectorStoreRecordMapper(
        @Nullable Function<Record, StorageModel> recordToStorageModelMapper,
        BiFunction<StorageModel, GetRecordOptions, Record> storageModelToRecordMapper) {
        this.recordToStorageModelMapper = recordToStorageModelMapper;
        this.storageModelToRecordMapper = storageModelToRecordMapper;
    }

    /**
     * Gets the function to convert a record to a storage model.
     *
     * @return the function to convert a record to a storage model
     */
    @Nullable
    public Function<Record, StorageModel> getRecordToStorageModelMapper() {
        return recordToStorageModelMapper;
    }

    /**
     * Gets the function to convert a storage model to a record.
     *
     * @return the function to convert a storage model to a record
     */
    public BiFunction<StorageModel, GetRecordOptions, Record> getStorageModelToRecordMapper() {
        return storageModelToRecordMapper;
    }

    /**
     * Converts a record to a storage model.
     *
     * @param record the record to convert
     * @return the storage model
     */
    public StorageModel mapRecordToStorageModel(Record record) {
        return getRecordToStorageModelMapper().apply(record);
    }

    /**
     * Converts a storage model to a record.
     *
     * @param storageModel the storage model to convert
     * @param options the options
     * @return the record
     */
    public Record mapStorageModelToRecord(StorageModel storageModel, GetRecordOptions options) {
        return getStorageModelToRecordMapper().apply(storageModel, options);
    }
}
