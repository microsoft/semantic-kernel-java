package com.microsoft.semantickernel.memory.recordattributes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface VectorStoreRecordDataAttribute {
    String value() default "";
    boolean hasEmbedding() default false;
    String embeddingFieldName() default "";
}