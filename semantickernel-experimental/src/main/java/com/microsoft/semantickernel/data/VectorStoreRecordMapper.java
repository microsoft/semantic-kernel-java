// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data;

import com.microsoft.semantickernel.builders.SemanticKernelBuilder;

import java.util.function.Function;

/**
 * A mapper to convert between a record and a storage model.
 *
 * @param <Record> the record type
 * @param <StorageModel> the storage model type
 */
public class VectorStoreRecordMapper<Record, StorageModel> {
    private final Function<Record, StorageModel> recordToStorageModelMapper;
    private final Function<StorageModel, Record> storageModelToRecordMapper;

    /**
     * Constructs a new instance of the VectorStoreRecordMapper.
     *
     * @param recordToStorageModelMapper the function to convert a record to a storage model
     * @param storageModelToRecordMapper the function to convert a storage model to a record
     */
    protected VectorStoreRecordMapper(
        Function<Record, StorageModel> recordToStorageModelMapper,
        Function<StorageModel, Record> storageModelToRecordMapper) {
        this.recordToStorageModelMapper = recordToStorageModelMapper;
        this.storageModelToRecordMapper = storageModelToRecordMapper;
    }

    public static <Record, StorageModel> Builder<Record, StorageModel> builder() {
        return new Builder<>();
    }

    /**
     * Gets the function to convert a record to a storage model.
     *
     * @return the function to convert a record to a storage model
     */
    public Function<Record, StorageModel> getRecordToStorageModelMapper() {
        return recordToStorageModelMapper;
    }

    /**
     * Gets the function to convert a storage model to a record.
     *
     * @return the function to convert a storage model to a record
     */
    public Function<StorageModel, Record> getStorageModelToRecordMapper() {
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
     * @return the record
     */
    public Record mapStorageModeltoRecord(StorageModel storageModel) {
        return getStorageModelToRecordMapper().apply(storageModel);
    }

    public static class Builder<Record, StorageModel>
        implements SemanticKernelBuilder<VectorStoreRecordMapper<Record, StorageModel>> {
        private Function<Record, StorageModel> recordToStorageModelMapper;
        private Function<StorageModel, Record> storageModelToRecordMapper;

        /**
         * Sets the function to convert a record to a storage model.
         *
         * @param recordToStorageModelMapper the function to convert a record to a storage model
         * @return the builder
         */
        public Builder<Record, StorageModel> withRecordToStorageModelMapper(
            Function<Record, StorageModel> recordToStorageModelMapper) {
            this.recordToStorageModelMapper = recordToStorageModelMapper;
            return this;
        }

        /**
         * Sets the function to convert a storage model to a record.
         *
         * @param storageModeltoRecordMapper the function to convert a storage model to a record
         * @return the builder
         */
        public Builder<Record, StorageModel> withStorageModelToRecordMapper(
            Function<StorageModel, Record> storageModeltoRecordMapper) {
            this.storageModelToRecordMapper = storageModeltoRecordMapper;
            return this;
        }

        /**
         * Builds the vector store record mapper.
         *
         * @return VectorStoreRecordMapper
         */
        @Override
        public VectorStoreRecordMapper<Record, StorageModel> build() {
            return new VectorStoreRecordMapper<>(
                recordToStorageModelMapper,
                storageModelToRecordMapper);
        }
    }
}
