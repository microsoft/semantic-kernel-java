// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.presidio.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RecognitionMetadata(
    @JsonProperty("recognizer_identifier")
    String recognizerIdentifier,
    @JsonProperty("recognizer_name")
    String recognizerName
) {

}
