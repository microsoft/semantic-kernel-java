// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.presidio.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.semantickernel.presidio.models.anonymizerType.AnonymizerType;
import java.util.List;
import java.util.Map;

public record AnonymizeRequest(
    String text,
    Map<String, ? extends AnonymizerType> anonymizers,
    @JsonProperty("analyzer_results")
    List<AnalyzerResult> analyzerResults) {

}
