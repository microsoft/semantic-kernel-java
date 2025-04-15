// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.implementation.telemetry;

import com.microsoft.semantickernel.orchestration.FunctionResult;
import com.microsoft.semantickernel.semanticfunctions.KernelArguments;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Scope;
import java.util.function.Function;
import reactor.util.context.Context;
import reactor.util.context.ContextView;

public class FunctionSpan extends SemanticKernelTelemetrySpan {

    public FunctionSpan(
        Span span,
        Function<Context, Context> reactorContextModifier,
        Scope spanScope,
        Scope contextScope) {
        super(span, reactorContextModifier, spanScope, contextScope);
    }

    public static FunctionSpan build(
        SemanticKernelTelemetry telemetry,
        ContextView contextView,
        String pluginName,
        String name,
        KernelArguments arguments) {

        SpanBuilder builder = telemetry.spanBuilder(
            String.format("function_invocation %s-%s", pluginName, name))
            .setSpanKind(SpanKind.INTERNAL)
            .setAttribute("semantic_kernel.function.invocation.name", name)
            .setAttribute("semantic_kernel.function.invocation.plugin_name", pluginName);

        Span span = builder.startSpan();

        return build(
            span,
            contextView,
            (contextModifier, spanScope, contextScope) -> new FunctionSpan(
                span,
                contextModifier,
                spanScope,
                contextScope));
    }

    public <T> void onFunctionSuccess(FunctionResult<T> result) {
        try {
            getSpan().setStatus(StatusCode.OK);
        } finally {
            close();
        }
    }

    public void onFunctionError(Throwable error) {
        try {
            getSpan().setStatus(StatusCode.ERROR, error.getMessage());
            getSpan().recordException(error);
        } finally {
            close();
        }
    }
}
