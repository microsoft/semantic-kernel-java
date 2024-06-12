// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.contextvariables.converters;

import com.microsoft.semantickernel.contextvariables.ContextVariableTypeConverter;
import com.microsoft.semantickernel.contextvariables.ContextVariableTypes;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Arrays;

/**
 * A {@link ContextVariableTypeConverter} for {@code java.time.OffsetDateTime} variables. Use
 * {@code ContextVariableTypes.getGlobalVariableTypeForClass(OffsetDateTime.class)} to get an
 * instance of this class.
 *
 * @see ContextVariableTypes#getGlobalVariableTypeForClass(Class)
 */
public class DateTimeContextVariableTypeConverter extends
    ContextVariableTypeConverter<OffsetDateTime> {

    /**
     * Creates a new instance of the {@link DateTimeContextVariableTypeConverter} class.
     */
    public DateTimeContextVariableTypeConverter() {
        super(
            OffsetDateTime.class,
            s -> {
                if (s instanceof String) {
                    return ZonedDateTime.parse((String) s).toOffsetDateTime();
                } else if (s instanceof OffsetDateTime) {
                    return (OffsetDateTime) s;
                }
                return null;
            },
            Object::toString,
            o -> {
                return ZonedDateTime.parse(o).toOffsetDateTime();
            },
            Arrays.asList(
                new DefaultConverter<OffsetDateTime, Instant>(OffsetDateTime.class, Instant.class) {
                    @Override
                    public Instant toObject(OffsetDateTime offsetDateTime) {
                        return offsetDateTime.toInstant();
                    }
                }));
    }
}
