// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.memory;

import com.microsoft.semantickernel.builders.SemanticKernelBuilder;

import java.util.function.Function;

/**
 * A mapper to convert between a record and a storage model.
 *
 * @param <Record> the record type
 * @param <StorageModel> the storage model type
 */
public class VectorStoreRecordMapper<Record, StorageModel> {
    private Function<Record, StorageModel> toStorageModelMapper;
    private Function<StorageModel, Record> toRecordMapper;

    public static <Record, StorageModel> Builder<Record, StorageModel> builder() {
        return new Builder<>();
    }

    /**
     * Gets the function to convert a record to a storage model.
     *
     * @return the function to convert a record to a storage model
     */
    public Function<Record, StorageModel> getToStorageModelMapper() {
        return toStorageModelMapper;
    }

    /**
     * Gets the function to convert a storage model to a record.
     *
     * @return the function to convert a storage model to a record
     */
    public Function<StorageModel, Record> getToRecordMapper() {
        return toRecordMapper;
    }

    /**
     * Converts a record to a storage model.
     *
     * @param record the record to convert
     * @return the storage model
     */
    public StorageModel toStorageModel(Record record) {
        return toStorageModelMapper.apply(record);
    }

    /**
     * Converts a storage model to a record.
     *
     * @param storageModel the storage model to convert
     * @return the record
     */
    public Record toRecord(StorageModel storageModel) {
        return toRecordMapper.apply(storageModel);
    }

    public static class Builder<RecordDataModel, StorageModel>
        implements SemanticKernelBuilder<VectorStoreRecordMapper<RecordDataModel, StorageModel>> {
        private Function<RecordDataModel, StorageModel> toStorageModelMapper;
        private Function<StorageModel, RecordDataModel> toDataModelMapper;

        /**
         * Sets the function to convert a record to a storage model.
         *
         * @param toStorageModelMapper the function to convert a record to a storage model
         * @return the builder
         */
        public Builder<RecordDataModel, StorageModel> toStorageModelMapper(
            Function<RecordDataModel, StorageModel> toStorageModelMapper) {
            this.toStorageModelMapper = toStorageModelMapper;
            return this;
        }

        /**
         * Sets the function to convert a storage model to a record.
         *
         * @param toDataModelMapper the function to convert a storage model to a record
         * @return the builder
         */
        public Builder<RecordDataModel, StorageModel> toRecordMapper(
            Function<StorageModel, RecordDataModel> toDataModelMapper) {
            this.toDataModelMapper = toDataModelMapper;
            return this;
        }

        /**
         * Builds the vector store record mapper.
         *
         * @return VectorStoreRecordMapper
         */
        @Override
        public VectorStoreRecordMapper<RecordDataModel, StorageModel> build() {
            VectorStoreRecordMapper<RecordDataModel, StorageModel> vectorStoreRecordMapper = new VectorStoreRecordMapper<>();
            vectorStoreRecordMapper.toStorageModelMapper = toStorageModelMapper;
            vectorStoreRecordMapper.toRecordMapper = toDataModelMapper;
            return vectorStoreRecordMapper;
        }
    }
}
