// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.aiservices.openai.chatcompletion;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Bar {

    private final String bar;

    public Bar(
        @JsonProperty("bar") String bar) {
        this.bar = bar;
    }

    public String getBar() {
        return bar;
    }

}
