// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.orchestration.responseformat;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The schema for a response in JSON format.
 */
public class JsonResponseSchema extends ResponseSchema {

    private final String name;
    private final String schema;
    private final boolean strict;

    /**
     * Used by Jackson deserialization to create a new 
     * instance of the {@link JsonResponseSchema} class.
     *
     * @param name   The name of the schema.
     * @param schema The schema.
     * @param strict Whether the schema is strict.
     */
    @JsonCreator
    public JsonResponseSchema(
        @JsonProperty("name") String name,
        @JsonProperty("schema") String schema,
        @JsonProperty("strict") boolean strict) {
        this.name = name;
        this.schema = schema;
        this.strict = strict;
    }

    /**
     * Gets the name of the schema.
     * @return The name of the schema.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the schema.
     * @return The schema.
     */
    public String getSchema() {
        return schema;
    }

    /**
     * Gets whether the schema is strict.
     * @return Whether the schema is strict.
     */
    public boolean isStrict() {
        return strict;
    }

}
