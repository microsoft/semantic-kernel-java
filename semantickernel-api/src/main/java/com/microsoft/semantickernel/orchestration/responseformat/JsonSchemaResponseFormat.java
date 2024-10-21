// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.orchestration.responseformat;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.semantickernel.exceptions.SKException;
import javax.annotation.Nullable;

/**
 * A response represented in a JSON schema format.
 */
public class JsonSchemaResponseFormat extends ResponseFormat {

    private final JsonResponseSchema jsonSchema;

    /** 
     * Used by Jackson deserialization to create a new instance
     * of the {@link JsonSchemaResponseFormat} class.
     * @param jsonSchema The JSON schema.
     */
    @JsonCreator
    public JsonSchemaResponseFormat(
        @JsonProperty("json_schema") JsonResponseSchema jsonSchema) {
        super(Type.JSON_SCHEMA);
        this.jsonSchema = jsonSchema;
    }

    /**
     * Gets the JSON schema.
     * @return The JSON schema.
     */
    @JsonProperty("json_schema")
    public JsonResponseSchema getJsonSchema() {
        return jsonSchema;
    }

    /**
     * Creates a new instance of the {@link JsonSchemaResponseFormat} class.
     * @return The new instance.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * A builder for the {@link JsonSchemaResponseFormat} class.
     */
    public static class Builder {

        @Nullable
        private JsonResponseSchema jsonResponseSchema = null;
        @Nullable
        private String jsonSchema = null;
        @Nullable
        private String name = null;
        private boolean strict = true;

        /**
         * Sets the response format.
         * @param clazz The class.
         * @param responseSchemaGenerator The response schema generator.
         * @return The builder.
         */
        public Builder setResponseFormat(Class<?> clazz,
            ResponseSchemaGenerator responseSchemaGenerator) {
            name = clazz.getSimpleName();
            return setJsonSchema(responseSchemaGenerator.generateSchema(clazz));
        }

        /**
         * Sets the response format. Uses Jackson to generate the schema
         * from the {@code clazz}
         * @param clazz The class.
         * @return The builder.
         */
        public Builder setResponseFormat(Class<?> clazz) {
            name = clazz.getSimpleName();
            setJsonSchema(ResponseSchemaGenerator.jacksonGenerator().generateSchema(clazz));
            return this;
        }

        /**
         * Sets the JSON response schema.
         * @param jsonResponseSchema The JSON response schema.
         * @return The builder.
         */
        public Builder setJsonResponseSchema(JsonResponseSchema jsonResponseSchema) {
            this.jsonResponseSchema = jsonResponseSchema;
            return this;
        }

        /**
         * Sets the JSON schema.
         * @param jsonSchema The JSON schema.
         * @return The builder.
         */
        public Builder setJsonSchema(String jsonSchema) {
            this.jsonSchema = jsonSchema;
            return this;
        }

        /**
         * Sets the name of the JSON schema.
         * @param name The schema name.
         * @return The builder.
         */
        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets whether the schema is strict.
         * @param strict Whether the schema is strict.
         * @return The builder.
         */
        public Builder setStrict(boolean strict) {
            this.strict = strict;
            return this;
        }

        /**
         * Builds the {@link JsonSchemaResponseFormat} instance.
         * @return The {@link JsonSchemaResponseFormat} instance.
         */
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
