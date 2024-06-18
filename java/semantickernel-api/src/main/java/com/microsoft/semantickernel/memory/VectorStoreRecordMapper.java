package com.microsoft.semantickernel.memory;

import com.microsoft.semantickernel.builders.SemanticKernelBuilder;

import java.util.function.Function;

public class VectorStoreRecordMapper<Record, StorageModel> {
    private Function<Record, StorageModel> toStorageModelMapper;
    private Function<StorageModel, Record> toRecordMapper;

    public static <Record, StorageModel> Builder<Record, StorageModel> builder() {
        return new Builder<>();
    }

    public Function<Record, StorageModel> getToStorageModelMapper() {
        return toStorageModelMapper;
    }

    public Function<StorageModel, Record> getToRecordMapper() {
        return toRecordMapper;
    }

    public StorageModel toStorageModel(Record record) {
        return toStorageModelMapper.apply(record);
    }

    public Record toRecord(StorageModel storageModel) {
        return toRecordMapper.apply(storageModel);
    }

    public static class Builder<RecordDataModel, StorageModel> implements SemanticKernelBuilder<VectorStoreRecordMapper<RecordDataModel, StorageModel>> {
        private Function<RecordDataModel, StorageModel> toStorageModelMapper;
        private Function<StorageModel, RecordDataModel> toDataModelMapper;

        public Builder<RecordDataModel, StorageModel> toStorageModelMapper(Function<RecordDataModel, StorageModel> toStorageModelMapper) {
            this.toStorageModelMapper = toStorageModelMapper;
            return this;
        }

        public Builder<RecordDataModel, StorageModel> toRecordMapper(Function<StorageModel, RecordDataModel> toDataModelMapper) {
            this.toDataModelMapper = toDataModelMapper;
            return this;
        }

        public VectorStoreRecordMapper<RecordDataModel, StorageModel> build() {
            VectorStoreRecordMapper<RecordDataModel, StorageModel> vectorStoreRecordMapper = new VectorStoreRecordMapper<>();
            vectorStoreRecordMapper.toStorageModelMapper = toStorageModelMapper;
            vectorStoreRecordMapper.toRecordMapper = toDataModelMapper;
            return vectorStoreRecordMapper;
        }
    }
}
