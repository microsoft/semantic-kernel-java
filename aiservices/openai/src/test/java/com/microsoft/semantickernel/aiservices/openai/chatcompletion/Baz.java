// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.aiservices.openai.chatcompletion;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Baz {

    @JsonProperty("bar")
    private final Bar bar;

    @JsonCreator
    public Baz(
        @JsonProperty("bar") Bar bar) {
        this.bar = bar;
    }

    @JsonProperty("bar")
    public Bar getBar() {
        return bar;
    }
}