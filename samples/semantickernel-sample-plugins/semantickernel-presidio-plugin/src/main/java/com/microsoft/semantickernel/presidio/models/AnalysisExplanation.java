// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.presidio.models;

public record AnalysisExplanation(
    String recognizer,
    String pattern_name,
    String pattern,
    Float original_score,
    Float score,
    String textual_explanation,
    Float score_context_improvement,
    String supportive_context_word,
    Float validation_result
) {

}
