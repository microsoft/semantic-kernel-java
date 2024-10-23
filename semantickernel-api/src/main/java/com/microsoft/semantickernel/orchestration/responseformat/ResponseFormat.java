// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.orchestration.responseformat;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

/**
 * Base class for response formats.
 */
@JsonTypeInfo(use = Id.NAME, include = As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = JsonSchemaResponseFormat.class, name = "json_schema", names = {
                "json_schema", "JSON_SCHEMA" }),
        @JsonSubTypes.Type(value = TextResponseFormat.class, name = "text", names = { "text",
                "TEXT" }),
        @JsonSubTypes.Type(value = JsonObjectResponseFormat.class, name = "json_object", names = {
                "json_object", "JSON_OBJECT" }),

})
public abstract class ResponseFormat {

    /**
     * The type of the response format.
     */
    public static enum Type {
        /**
         * Only valid for openai chat completion, with GPT-4 and gpt-3.5-turbo-1106+ models.
         */
        JSON_OBJECT,
        /**
         * Only valid for openai chat completion, with GPT-4 and gpt-3.5-turbo-1106+ models.
         */
        JSON_SCHEMA,
        /**
         * The response is in text format.
         */
        TEXT;
    }

    private final Type type;

    /**
     * Creates a new instance of the {@link ResponseFormat} class.
     *
     * @param type The type of the response format.
     */
    public ResponseFormat(Type type) {
        this.type = type;
    }

    /**
     * Gets the type of the response format.
     *
     * @return The type.
     */
    @JsonProperty("type")
    public Type getType() {
        return type;
    }

}
