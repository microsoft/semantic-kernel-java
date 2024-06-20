// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.presidio.models;

import com.fasterxml.jackson.annotation.JsonProperty;


public record AnalyzerResult(
    Integer start,
    Integer end,
    Float score,
    @JsonProperty("entity_type")
    String entityType,
    @JsonProperty("recognition_metadata")
    RecognitionMetadata recognitionMetadata,
    @JsonProperty("analysis_explanation")
    AnalysisExplanation analysisExplanation
) {

}
