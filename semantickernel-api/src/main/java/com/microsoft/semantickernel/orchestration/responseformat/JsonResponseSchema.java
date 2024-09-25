// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.orchestration.responseformat;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class JsonResponseSchema extends ResponseSchema {

    private final String name;
    private final String schema;
    private final boolean strict;

    @JsonCreator
    public JsonResponseSchema(
        @JsonProperty("name") String name,
        @JsonProperty("schema") String schema,
        @JsonProperty("strict") boolean strict) {
        this.name = name;
        this.schema = schema;
        this.strict = strict;
    }

    public String getName() {
        return name;
    }

    public String getSchema() {
        return schema;
    }

    public boolean isStrict() {
        return strict;
    }

}
