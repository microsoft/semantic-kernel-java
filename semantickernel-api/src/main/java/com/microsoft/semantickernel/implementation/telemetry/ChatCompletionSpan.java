// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.implementation.telemetry;

import com.azure.ai.openai.models.ChatCompletions;
import com.azure.ai.openai.models.CompletionsUsage;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Scope;
import java.util.function.Function;
import javax.annotation.Nullable;
import reactor.util.context.Context;
import reactor.util.context.ContextView;

public class ChatCompletionSpan extends SemanticKernelTelemetrySpan {

    public ChatCompletionSpan(
        Span span,
        Function<Context, Context> reactorContextModifier,
        Scope spanScope,
        Scope contextScope) {
        super(span, reactorContextModifier, spanScope, contextScope);
    }

    public static ChatCompletionSpan startChatCompletionSpan(
        SemanticKernelTelemetry telemetry,
        ContextView contextView,
        @Nullable String modelName,
        String modelProvider,
        @Nullable Integer maxTokens,
        @Nullable Double temperature,
        @Nullable Double topP) {
        return startCompletionSpan(
            telemetry,
            contextView,
            "chat.completions",
            modelName,
            modelProvider,
            maxTokens,
            temperature, topP);
    }

    public ChatCompletionSpan startTextCompletionSpan(
        SemanticKernelTelemetry telemetry,
        ContextView contextView,
        @Nullable String modelName,
        String modelProvider,
        @Nullable Integer maxTokens,
        @Nullable Double temperature,
        @Nullable Double topP) {
        return startCompletionSpan(
            telemetry,
            contextView,
            "text.completions",
            modelName,
            modelProvider,
            maxTokens,
            temperature, topP);
    }

    public static ChatCompletionSpan startCompletionSpan(
        SemanticKernelTelemetry telemetry,
        ContextView contextView,
        String operationName,
        @Nullable String modelName,
        String modelProvider,
        @Nullable Integer maxTokens,
        @Nullable Double temperature,
        @Nullable Double topP) {
        if (modelName == null) {
            modelName = "unknown";
        }

        SpanBuilder builder = telemetry.spanBuilder(operationName + " " + modelName)
            .setSpanKind(SpanKind.CLIENT)
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

        Span span = builder.startSpan();

        return build(
            span,
            contextView,
            (contextModifier, spanScope, contextScope) -> new ChatCompletionSpan(
                span,
                contextModifier,
                spanScope,
                contextScope));
    }

    public void endSpanWithUsage(ChatCompletions chatCompletions) {
        CompletionsUsage usage = chatCompletions.getUsage();
        getSpan().setStatus(StatusCode.OK);
        getSpan()
            .setAttribute("gen_ai.usage.output_tokens", usage.getCompletionTokens());
        getSpan().setAttribute("gen_ai.usage.input_tokens", usage.getPromptTokens());
        close();
    }

    public void endSpanWithError(Throwable throwable) {
        getSpan().setStatus(StatusCode.ERROR, throwable.getMessage());
        close();
    }
}
