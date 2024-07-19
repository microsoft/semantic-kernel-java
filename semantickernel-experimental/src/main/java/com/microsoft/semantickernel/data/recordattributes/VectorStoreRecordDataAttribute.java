// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.recordattributes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface VectorStoreRecordDataAttribute {
    String storageName() default "";

    boolean hasEmbedding() default false;

    String embeddingFieldName() default "";

    boolean isFilterable() default false;
}