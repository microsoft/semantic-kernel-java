// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data;

import com.microsoft.semantickernel.data.recorddefinition.VectorStoreRecordDefinition;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class VolatileVectorStoreRecordCollectionOptions<Record>
    implements VectorStoreRecordCollectionOptions<String, Record> {
    private final Class<Record> recordClass;
    @Nullable
    private final VectorStoreRecordDefinition recordDefinition;

    /**
     * Creates a new instance of the Volatile vector store record collection options.
     *
     * @param recordClass The record class.
     * @param recordDefinition The record definition.
     */
    public VolatileVectorStoreRecordCollectionOptions(@Nonnull Class<Record> recordClass,
        @Nullable VectorStoreRecordDefinition recordDefinition) {
        this.recordClass = recordClass;
        this.recordDefinition = recordDefinition;
    }

    /**
     * Creates a new builder.
     *
     * @param <Record> the record type
     * @return the builder
     */
    public static <Record> Builder<Record> builder() {
        return new Builder<>();
    }

    /**
     * Gets the key class.
     *
     * @return the key class
     */
    @Override
    public Class<String> getKeyClass() {
        return String.class;
    }

    /**
     * Gets the record class.
     *
     * @return the record class
     */
    public Class<Record> getRecordClass() {
        return recordClass;
    }

    /**
     * Gets the record definition.
     *
     * @return the record definition
     */
    public VectorStoreRecordDefinition getRecordDefinition() {
        return recordDefinition;
    }

    /**
     * Builder for Volatile vector store record collection options.
     *
     * @param <Record> the record type
     */
    public static class Builder<Record> {
        @Nullable
        private Class<Record> recordClass;
        @Nullable
        private VectorStoreRecordDefinition recordDefinition;

        /**
         * Sets the record class.
         *
         * @param recordClass the record class
         * @return the builder
         */
        public Builder<Record> withRecordClass(Class<Record> recordClass) {
            this.recordClass = recordClass;
            return this;
        }

        /**
         * Sets the record definition.
         *
         * @param recordDefinition the record definition
         * @return the builder
         */
        public Builder<Record> withRecordDefinition(VectorStoreRecordDefinition recordDefinition) {
            this.recordDefinition = recordDefinition;
            return this;
        }

        /**
         * Builds the options.
         *
         * @return the options
         */
        public VolatileVectorStoreRecordCollectionOptions<Record> build() {
            if (recordClass == null) {
                throw new IllegalArgumentException("recordClass is required");
            }

            return new VolatileVectorStoreRecordCollectionOptions<>(recordClass, recordDefinition);
        }
    }
}
