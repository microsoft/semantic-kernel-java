// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.aiservices.openai.chatcompletion;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Foo<T> {

    @JsonProperty("bar")
    private final T bar;

    @JsonCreator
    public Foo(
        @JsonProperty("bar") T bar) {
        this.bar = bar;
    }

    @JsonProperty("bar")
    public T getBar() {
        return bar;
    }
}