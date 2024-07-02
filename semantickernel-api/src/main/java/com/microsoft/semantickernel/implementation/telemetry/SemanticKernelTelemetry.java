// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.implementation.telemetry;

import com.azure.ai.openai.models.CompletionsUsage;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.StatusCode;
import javax.annotation.Nullable;

public class SemanticKernelTelemetry {

    public static final String OPEN_AI_PROVIDER = "openai";

    public static Span startChatCompletionSpan(
        @Nullable String modelName,
        String modelProvider,
        @Nullable Integer maxTokens,
        @Nullable Double temperature,
        @Nullable Double topP) {
        return startCompletionSpan("chat.completions", modelName, modelProvider, maxTokens,
            temperature, topP);
    }

    public static Span startTextCompletionSpan(
        @Nullable String modelName,
        String modelProvider,
        @Nullable Integer maxTokens,
        @Nullable Double temperature,
        @Nullable Double topP) {
        return startCompletionSpan("text.completions", modelName, modelProvider, maxTokens,
            temperature, topP);
    }

    private static Span startCompletionSpan(
        String operationName,
        @Nullable String modelName,
        String modelProvider,
        @Nullable Integer maxTokens,
        @Nullable Double temperature,
        @Nullable Double topP) {
        OpenTelemetry otel = GlobalOpenTelemetry.get();

        if (modelName == null) {
            modelName = "unknown";
        }
        SpanBuilder builder = otel
            .getTracer("SemanticKernel")
            .spanBuilder(operationName + " " + modelName)
            .setAttribute("gen_ai.request.model", modelName)
            .setAttribute("gen_ai.operation.name", operationName)
            .setAttribute("gen_ai.system", modelProvider);

        if (maxTokens != null) {
            builder.setAttribute("gen_ai.request.max_tokens", maxTokens);
        }
        if (temperature != null) {
            builder.setAttribute("gen_ai.request.temperature", temperature);
        }
        if (topP != null) {
            builder.setAttribute("gen_ai.request.top_p", topP);
        }

        return builder.startSpan();
    }

    public static void endSpanWithUsage(Span span, CompletionsUsage usage) {
        span.setStatus(StatusCode.OK);
        span.setAttribute("gen_ai.response.completion_tokens", usage.getCompletionTokens());
        span.setAttribute("gen_ai.response.prompt_tokens", usage.getPromptTokens());
        span.end();
    }

    public static void endSpanWithError(Span span) {
        span.setStatus(StatusCode.ERROR);
        span.end();
    }
}
