// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.recordattributes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents a data attribute in a record.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface VectorStoreRecordDataAttribute {
    /**
     * Storage name of the field.
     * @return The storage name of the field.
     */
    String storageName() default "";

    /**
     * Whether the field has a vector representation.
     * @return {@code true} if the field has a vector representation.
     */
    boolean hasEmbedding() default false;

    /**
     * Name of the field that contains the vector representation.
     * @return The name of the field that contains the vector representation.
     */
    String embeddingFieldName() default "";

    /**
     * Whether the field is filterable.
     * @return {@code true} if the field is filterable.
     */
    boolean isFilterable() default false;
}