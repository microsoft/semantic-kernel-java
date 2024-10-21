// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.orchestration.responseformat;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Represents a text response format.
 */
public class TextResponseFormat extends ResponseFormat {

    /**
     * Used by Jackson to creates a new instance of the
     * {@link TextResponseFormat} class.
     */
    @JsonCreator
    public TextResponseFormat() {
        super(Type.TEXT);
    }
}
