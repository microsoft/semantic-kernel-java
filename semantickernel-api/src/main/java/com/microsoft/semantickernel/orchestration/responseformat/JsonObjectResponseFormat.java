// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.orchestration.responseformat;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * A response represented in a JSON format.
 */
public class JsonObjectResponseFormat extends ResponseFormat {

    /**
     * Used by Jackson deserialization to create a new instance
     * of the {@link JsonObjectResponseFormat} class.
     */
    @JsonCreator
    public JsonObjectResponseFormat() {
        super(Type.JSON_OBJECT);
    }
}
