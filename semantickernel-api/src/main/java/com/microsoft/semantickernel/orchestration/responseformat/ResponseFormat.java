// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.orchestration.responseformat;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

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

    public static enum Type {
        JSON_OBJECT,
        /**
         * Only valid for openai chat completion, with GPT-4 and gpt-3.5-turbo-1106+ models.
         */
        JSON_SCHEMA, TEXT;
    }

    private final Type type;

    public ResponseFormat(Type type) {
        this.type = type;
    }

    @JsonProperty("type")
    public Type getType() {
        return type;
    }

}
