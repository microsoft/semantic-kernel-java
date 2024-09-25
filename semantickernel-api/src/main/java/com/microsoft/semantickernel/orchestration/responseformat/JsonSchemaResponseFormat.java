// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.orchestration.responseformat;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.semantickernel.exceptions.SKException;
import javax.annotation.Nullable;

public class JsonSchemaResponseFormat extends ResponseFormat {

    private final JsonResponseSchema jsonSchema;

    @JsonCreator
    public JsonSchemaResponseFormat(
        @JsonProperty("json_schema") JsonResponseSchema jsonSchema) {
        super(Type.JSON_SCHEMA);
        this.jsonSchema = jsonSchema;
    }

    @JsonProperty("json_schema")
    public JsonResponseSchema getJsonSchema() {
        return jsonSchema;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        @Nullable
        private JsonResponseSchema jsonResponseSchema = null;
        @Nullable
        private String jsonSchema = null;
        @Nullable
        private String name = null;
        private boolean strict = true;

        public Builder setResponseFormat(Class<?> clazz,
            ResponseSchemaGenerator responseSchemaGenerator) {
            name = clazz.getSimpleName();
            return setJsonSchema(responseSchemaGenerator.generateSchema(clazz));
        }

        public Builder setResponseFormat(Class<?> clazz) {
            name = clazz.getSimpleName();
            setJsonSchema(ResponseSchemaGenerator.jacksonGenerator().generateSchema(clazz));
            return this;
        }

        public Builder setJsonResponseSchema(JsonResponseSchema jsonResponseSchema) {
            this.jsonResponseSchema = jsonResponseSchema;
            return this;
        }

        public Builder setJsonSchema(String jsonSchema) {
            this.jsonSchema = jsonSchema;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setStrict(boolean strict) {
            this.strict = strict;
            return this;
        }

        public JsonSchemaResponseFormat build() {

            if (jsonResponseSchema != null) {
                return new JsonSchemaResponseFormat(jsonResponseSchema);
            }

            if (jsonSchema == null) {
                throw new SKException("Response format not set");
            }

            if (name == null) {
                throw new SKException("Json format name not set");
            }

            return new JsonSchemaResponseFormat(new JsonResponseSchema(name, jsonSchema, strict));
        }
    }
}
