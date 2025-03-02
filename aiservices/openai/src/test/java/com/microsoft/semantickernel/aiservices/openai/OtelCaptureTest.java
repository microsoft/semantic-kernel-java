// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.aiservices.openai;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.models.ChatCompletions;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.Completions;
import com.azure.ai.openai.models.CompletionsOptions;
import com.azure.ai.openai.models.CompletionsUsage;
import com.azure.core.http.rest.Response;
import com.microsoft.semantickernel.aiservices.openai.chatcompletion.OpenAIChatCompletion;
import com.microsoft.semantickernel.aiservices.openai.textcompletion.OpenAITextGenerationService;
import com.microsoft.semantickernel.services.textcompletion.TextGenerationService;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.ArrayList;
import java.util.Collection;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;

public class OtelCaptureTest {

    private static OpenTelemetrySdk otel;
    private static ArrayList<SpanData> spans = new ArrayList<>();

    @BeforeEach
    public void clearSpans() {
        spans.clear();
    }

    @BeforeAll
    public static void setup() {

        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
            .addSpanProcessor(SimpleSpanProcessor.builder(new SpanExporter() {
                @Override
                public CompletableResultCode export(Collection<SpanData> collection) {
                    spans.addAll(collection);
                    return new CompletableResultCode();
                }

                @Override
                public CompletableResultCode flush() {
                    return new CompletableResultCode();
                }

                @Override
                public CompletableResultCode shutdown() {
                    return new CompletableResultCode();
                }
            })
                .build())
            .build();

        GlobalOpenTelemetry.resetForTest();

        otel = OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .buildAndRegisterGlobal();
    }

    @AfterAll
    public static void shutdown() {
        otel.shutdown();
    }

    @Test
    public void otelChatCaptureTest() {
        OpenAIAsyncClient openAIAsyncClient = Mockito.mock(OpenAIAsyncClient.class);

        CompletionsUsage completionsUsage = Mockito.mock(CompletionsUsage.class);
        Mockito.when(completionsUsage.getCompletionTokens()).thenReturn(21);
        Mockito.when(completionsUsage.getPromptTokens()).thenReturn(42);

        ChatCompletions chatCompletions = Mockito.mock(ChatCompletions.class);
        Mockito.when(chatCompletions.getUsage()).thenReturn(completionsUsage);

        Response<ChatCompletions> response = Mockito.mock(Response.class);
        Mockito.when(response.getStatusCode()).thenReturn(200);
        Mockito.when(response.getValue()).thenReturn(chatCompletions);

        Mockito.when(openAIAsyncClient.getChatCompletionsWithResponse(
            Mockito.any(),
            Mockito.<ChatCompletionsOptions>any(),
            Mockito.any())).thenAnswer(invocation -> Mono.just(response));

        OpenAIChatCompletion client = OpenAIChatCompletion.builder()
            .withOpenAIAsyncClient(openAIAsyncClient)
            .withModelId("a-model")
            .build();

        try {
            client.getChatMessageContentsAsync(
                "foo",
                null,
                null).block();
        } catch (Exception e) {
            // Expect to fail
        }

        Assertions.assertFalse(spans.isEmpty());
        Assertions.assertEquals("a-model",
            spans.get(0).getAttributes().get(AttributeKey.stringKey("gen_ai.request.model")));
        Assertions.assertEquals("chat.completions",
            spans.get(0).getAttributes().get(AttributeKey.stringKey("gen_ai.operation.name")));
        Assertions.assertEquals("openai",
            spans.get(0).getAttributes().get(AttributeKey.stringKey("gen_ai.system")));
        Assertions.assertEquals(21,
            spans.get(0).getAttributes()
                .get(AttributeKey.longKey("gen_ai.usage.output_tokens")));
        Assertions.assertEquals(42,
            spans.get(0).getAttributes()
                .get(AttributeKey.longKey("gen_ai.usage.input_tokens")));
    }
}
