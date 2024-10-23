// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.vectorstorage.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents a data attribute in a record.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface VectorStoreRecordData {
    /**
     * Storage name of the field.
     * This value is only used when JSON Serialization using Jackson is not supported in a VectorStore.
     * When Jackson is supported, @JsonProperty should be used to specify an alternate field name in the storage database.
     * @return The storage name of the field.
     */
    String storageName() default "";

    /**
     * Whether the field is filterable.
     * @return {@code true} if the field is filterable.
     */
    boolean isFilterable() default false;

    /**
     * Whether the field is full text searchable.
     * @return {@code true} if the field is full text searchable.
     */
    boolean isFullTextSearchable() default false;
}
