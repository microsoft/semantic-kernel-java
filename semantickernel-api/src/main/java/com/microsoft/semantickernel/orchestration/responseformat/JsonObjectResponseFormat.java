// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.orchestration.responseformat;

import com.fasterxml.jackson.annotation.JsonCreator;

public class JsonObjectResponseFormat extends ResponseFormat {

    @JsonCreator
    public JsonObjectResponseFormat() {
        super(Type.JSON_OBJECT);
    }
}
