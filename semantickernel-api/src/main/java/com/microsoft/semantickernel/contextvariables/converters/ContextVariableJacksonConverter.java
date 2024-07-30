// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.contextvariables.converters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.semantickernel.contextvariables.ContextVariableTypeConverter;
import com.microsoft.semantickernel.contextvariables.ContextVariableTypeConverter.Builder;
import com.microsoft.semantickernel.exceptions.SKException;

/**
 * A utility class for creating {@link ContextVariableTypeConverter} instances that use Jackson for
 * serialization and deserialization.
 */
public final class ContextVariableJacksonConverter {

    /**
     * Creates a new {@link ContextVariableTypeConverter} that uses Jackson for serialization and
     * deserialization.
     *
     * @param type   the type of the context variable
     * @param mapper the {@link ObjectMapper} to use for serialization and deserialization
     * @param <T>    the type of the context variable
     * @return a new {@link ContextVariableTypeConverter}
     */
    public static <T> ContextVariableTypeConverter<T> create(Class<T> type, ObjectMapper mapper) {
        return builder(type, mapper).build();
    }

    /**
     * Creates a new {@link ContextVariableTypeConverter} that uses Jackson for serialization and
     * deserialization.
     *
     * @param type the type of the context variable
     * @param <T>  the type of the context variable
     * @return a new {@link ContextVariableTypeConverter}
     */
    public static <T> ContextVariableTypeConverter<T> create(Class<T> type) {
        return create(type, new ObjectMapper());
    }

    /**
     * Creates a new {@link Builder} for a {@link ContextVariableTypeConverter} that uses Jackson
     * for serialization and deserialization.
     *
     * @param type the type of the context variable
     * @param <T>  the type of the context variable
     * @return a new {@link Builder}
     */
    public static <T> Builder<T> builder(Class<T> type) {
        return builder(type, new ObjectMapper());
    }

    /**
     * Creates a new {@link Builder} for a {@link ContextVariableTypeConverter} that uses Jackson
     * for serialization and deserialization.
     *
     * @param type   the type of the context variable
     * @param mapper the {@link ObjectMapper} to use for serialization and deserialization
     * @param <T>    the type of the context variable
     * @return a new {@link Builder}
     */
    public static <T> Builder<T> builder(Class<T> type, ObjectMapper mapper) {
        return ContextVariableTypeConverter.builder(type)
            .fromPromptString(str -> {
                try {
                    return mapper.readValue(str, type);
                } catch (JsonProcessingException e) {
                    throw new SKException("Failed to deserialize object", e);
                }
            })
            .toPromptString(obj -> {
                try {
                    return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
                } catch (JsonProcessingException e) {
                    throw new SKException("Failed to serialize object", e);
                }
            });
    }
}
