// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.semantickernel.data.vectorstorage.VectorStoreRecordCollectionOptions;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordDefinition;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents the options for a volatile vector store record collection.
 *
 * @param <Record> the record type
 */
public class VolatileVectorStoreRecordCollectionOptions<Record>
    implements VectorStoreRecordCollectionOptions<String, Record> {
    private final Class<Record> recordClass;
    @Nullable
    private final VectorStoreRecordDefinition recordDefinition;
    @Nullable
    private final ObjectMapper objectMapper;

    /**
     * Creates a new instance of the Volatile vector store record collection options.
     *
     * @param recordClass The record class.
     * @param recordDefinition The record definition.
     * @param objectMapper An instanc of Jackson ObjectMapper.
     */
    @SuppressFBWarnings("EI_EXPOSE_REP2") // ObjectMapper only has package visibility
    public VolatileVectorStoreRecordCollectionOptions(@Nonnull Class<Record> recordClass,
        @Nullable VectorStoreRecordDefinition recordDefinition, ObjectMapper objectMapper) {
        this.recordClass = recordClass;
        this.recordDefinition = recordDefinition;
        this.objectMapper = objectMapper;
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
     * Gets the object mapper.
     *
     * @return the object mapper
     */
    ObjectMapper getObjectMapper() {
        return objectMapper;
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
        @Nullable
        private ObjectMapper objectMapper;

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
         * Sets the object mapper.
         *
         * @param objectMapper the object mapper
         * @return the builder
         */
        @SuppressFBWarnings("EI_EXPOSE_REP2")
        public Builder<Record> withObjectMapper(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
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

            return new VolatileVectorStoreRecordCollectionOptions<>(recordClass, recordDefinition,
                objectMapper);
        }
    }
}
