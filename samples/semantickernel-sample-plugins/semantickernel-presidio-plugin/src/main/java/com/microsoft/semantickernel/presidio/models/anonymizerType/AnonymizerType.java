// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.presidio.models.anonymizerType;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AnonymizerType {

    private String type;

    AnonymizerType(
        @JsonProperty("type") String type) {
        this.type = type;
    }

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    public static class Replace extends AnonymizerType {

        private final String new_value;

        @JsonCreator
        public Replace(
            @JsonProperty("new_value") String new_value) {
            super("replace");
            this.new_value = new_value;
        }

        @JsonProperty("new_value")
        public String getNewValue() {
            return new_value;
        }
    }
}
