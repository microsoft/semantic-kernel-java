// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.implementation.telemetry;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.instrumentation.reactor.v3_1.ContextPropagationOperator;
import java.io.Closeable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import org.slf4j.Logger;
import reactor.util.context.ContextView;

public abstract class SemanticKernelTelemetrySpan implements Closeable {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(
        SemanticKernelTelemetrySpan.class);

    private final Span span;
    private final Function<reactor.util.context.Context, reactor.util.context.Context> reactorContextModifier;
    private final Scope spanScope;
    private final Scope contextScope;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    public SemanticKernelTelemetrySpan(Span span,
        Function<reactor.util.context.Context, reactor.util.context.Context> reactorContextModifier,
        Scope spanScope, Scope contextScope) {
        this.span = span;
        this.reactorContextModifier = reactorContextModifier;
        this.spanScope = spanScope;
        this.contextScope = contextScope;
    }

    public interface SpanConstructor<T extends SemanticKernelTelemetrySpan> {

        public T build(
            Function<reactor.util.context.Context, reactor.util.context.Context> contextModifier,
            Scope spanScope,
            Scope contextScope);
    }

    // Does need to be closed but as we are doing this in a reactive app, cant enforce the try with resources
    @SuppressWarnings("MustBeClosedChecker")
    public static <T extends SemanticKernelTelemetrySpan> T build(
        Span span,
        ContextView contextView,
        SpanConstructor<T> builder) {
        LOGGER.trace("Starting Span: {}", span);

        Context currentOtelContext = ContextPropagationOperator
            .getOpenTelemetryContextFromContextView(
                contextView,
                Context.current());

        Context otelContext = span.storeInContext(currentOtelContext);
        Scope contextScope = otelContext.makeCurrent();
        Scope spanScope = span.makeCurrent();

        Function<reactor.util.context.Context, reactor.util.context.Context> reactorContextModifier = ctx -> {
            return ContextPropagationOperator.storeOpenTelemetryContext(ctx, otelContext);
        };

        return builder.build(reactorContextModifier, spanScope, contextScope);
    }

    public Function<reactor.util.context.Context, reactor.util.context.Context> getReactorContextModifier() {
        return reactorContextModifier;
    }

    public void close() {
        if (closed.compareAndSet(false, true)) {
            LOGGER.trace("Closing span: {}", span);
            if (span.isRecording()) {
                span.end();
            }
            if (contextScope != null) {
                contextScope.close();
            }
            if (spanScope != null) {
                spanScope.close();
            }
        }
    }

    public Span getSpan() {
        return span;
    }
}
