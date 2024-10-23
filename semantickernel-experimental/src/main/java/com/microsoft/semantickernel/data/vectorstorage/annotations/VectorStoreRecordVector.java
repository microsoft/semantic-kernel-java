// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.vectorstorage.annotations;

import com.microsoft.semantickernel.data.vectorstorage.definition.DistanceFunction;
import com.microsoft.semantickernel.data.vectorstorage.definition.IndexKind;

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
public @interface VectorStoreRecordVector {

    /**
     * Number of dimensions in the vector.
     * @return The number of dimensions in the vector.
     */
    int dimensions();

    /**
     * Storage name of the field.
     * @return The storage name of the field.
     */
    String storageName() default "";

    /**
     * Type of index to be used for the vector.
     * @return The type of index to be used for the vector.
     */
    @Nullable
    IndexKind indexKind() default IndexKind.UNDEFINED;

    /**
     * Distance function to be used for to compute the distance between vectors.
     * @return The distance function to be used for to compute the distance between vectors.
     */
    @Nullable
    DistanceFunction distanceFunction() default DistanceFunction.UNDEFINED;

}