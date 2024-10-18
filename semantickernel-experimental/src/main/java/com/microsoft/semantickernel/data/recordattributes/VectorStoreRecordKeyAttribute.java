// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.recordattributes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents the key attribute in a record.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface VectorStoreRecordKeyAttribute {
    /**
     * Storage name of the field.
     * @return The storage name of the field.
     */
    String storageName() default "";
}
