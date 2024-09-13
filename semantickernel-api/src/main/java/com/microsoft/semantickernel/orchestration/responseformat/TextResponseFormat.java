// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.orchestration.responseformat;

import com.fasterxml.jackson.annotation.JsonCreator;

public class TextResponseFormat extends ResponseFormat {

    @JsonCreator
    public TextResponseFormat() {
        super(Type.TEXT);
    }
}
