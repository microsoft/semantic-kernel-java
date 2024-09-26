// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.vectorstorage.attributes;

import com.microsoft.semantickernel.data.vectorstorage.definition.DistanceFunction;
import javax.annotation.Nullable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents a vector attribute in a record.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface VectorStoreRecordVectorAttribute {

    /**
     * Number of dimensions in the vector.
     */
    int dimensions();

    /**
     * Storage name of the field.
     */
    String storageName() default "";

    /**
     * Type of index to be used for the vector.
     */
    String indexKind() default "";

    /**
     * Distance function to be used for to compute the distance between vectors.
     */
    @Nullable
    DistanceFunction distanceFunction();

}