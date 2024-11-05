// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.implementation.telemetry;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.instrumentation.reactor.v3_1.ContextPropagationOperator;
import java.io.Closeable;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import org.slf4j.Logger;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.util.context.ContextView;

public abstract class SemanticKernelTelemetrySpan implements Closeable {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(
        SemanticKernelTelemetrySpan.class);

    private static final long SPAN_TIMEOUT_MS = Long.parseLong((String) System.getProperties()
        .getOrDefault("semantickernel.telemetry.span_timeout", "120000"));

    private final Span span;
    private final Function<reactor.util.context.Context, reactor.util.context.Context> reactorContextModifier;
    private final Scope spanScope;
    private final Scope contextScope;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    // Timeout to close the span if it was not closed within the specified time to avoid memory leaks
    private final Disposable watchdog;

    // This is a finalizer guardian to ensure that the span is closed if it was not closed explicitly
    @SuppressWarnings("unused")
    private final Object finalizerGuardian = new Object() {
        @Override
        protected void finalize() {
            if (closed.get() == false) {
                LOGGER.warn("Span was not closed");
                close();
            }
        }
    };

    public SemanticKernelTelemetrySpan(Span span,
        Function<reactor.util.context.Context, reactor.util.context.Context> reactorContextModifier,
        Scope spanScope, Scope contextScope) {
        this.span = span;
        this.reactorContextModifier = reactorContextModifier;
        this.spanScope = spanScope;
        this.contextScope = contextScope;

        watchdog = Mono.just(1)
            .delay(Duration.ofMillis(SPAN_TIMEOUT_MS))
            .subscribe(i -> {
                if (closed.get() == false) {
                    LOGGER.warn("Span was not closed, timing out");
                    close();
                }
            });
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
                try {
                    span.end();
                } catch (Exception e) {
                    LOGGER.error("Error closing span", e);
                }
            }
            if (contextScope != null) {
                try {
                    contextScope.close();
                } catch (Exception e) {
                    LOGGER.error("Error closing context scope", e);
                }
            }
            if (spanScope != null) {
                try {
                    spanScope.close();
                } catch (Exception e) {
                    LOGGER.error("Error closing span scope", e);
                }
            }
            watchdog.dispose();
        }
    }

    public Span getSpan() {
        return span;
    }
}
