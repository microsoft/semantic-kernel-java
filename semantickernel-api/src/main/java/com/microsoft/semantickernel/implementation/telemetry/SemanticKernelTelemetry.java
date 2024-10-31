// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.implementation.telemetry;

import com.microsoft.semantickernel.orchestration.InvocationContext;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.Tracer;
import javax.annotation.Nullable;

public class SemanticKernelTelemetry {

    public static final String OPEN_AI_PROVIDER = "openai";

    private final Tracer tracer;

    @Nullable
    private final SpanContext spanContext;

    public SemanticKernelTelemetry(
        Tracer tracer,
        @Nullable SpanContext spanContext) {

        this.tracer = tracer;
        this.spanContext = spanContext;
    }

    public SemanticKernelTelemetry() {
        this(
            GlobalOpenTelemetry.getTracer("SemanticKernel"),
            null);
    }

    public static SemanticKernelTelemetry getTelemetry(
        @Nullable InvocationContext invocationContext) {
        if (invocationContext != null) {
            return invocationContext.getTelemetry();
        }
        return new SemanticKernelTelemetry();
    }

    private Tracer getTracer() {
        return tracer;
    }

    public SpanBuilder spanBuilder(String operationName) {
        SpanBuilder sb = tracer.spanBuilder(operationName);

        if (spanContext != null) {
            sb.addLink(spanContext);
        }
        return sb;
    }
}
