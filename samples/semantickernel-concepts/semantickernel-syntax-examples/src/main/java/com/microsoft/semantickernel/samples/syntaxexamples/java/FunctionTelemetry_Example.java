// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.samples.syntaxexamples.java;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.KeyCredential;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.aiservices.openai.chatcompletion.OpenAIChatCompletion;
import com.microsoft.semantickernel.exceptions.ConfigurationException;
import com.microsoft.semantickernel.implementation.telemetry.SemanticKernelTelemetry;
import com.microsoft.semantickernel.orchestration.InvocationContext;
import com.microsoft.semantickernel.orchestration.InvocationReturnMode;
import com.microsoft.semantickernel.orchestration.ToolCallBehavior;
import com.microsoft.semantickernel.plugin.KernelPluginFactory;
import com.microsoft.semantickernel.samples.syntaxexamples.functions.Example59_OpenAIFunctionCalling.PetPlugin;
import com.microsoft.semantickernel.semanticfunctions.KernelArguments;
import com.microsoft.semantickernel.semanticfunctions.annotations.DefineKernelFunction;
import com.microsoft.semantickernel.semanticfunctions.annotations.KernelFunctionParameter;
import com.microsoft.semantickernel.services.ServiceNotFoundException;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Scope;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import reactor.core.publisher.Mono;

public class FunctionTelemetry_Example {
    /*
     * // Get the Application Insights agent from
     * https://github.com/microsoft/ApplicationInsights-Java, e.g:
     * ```
     * wget -O "/tmp/applicationinsights-agent-3.6.1.jar"
     * "https://github.com/microsoft/ApplicationInsights-Java/releases/download/3.6.1/applicationinsights-agent-3.6.1.jar"
     * ```
     * 
     * // Get your application insights connection string from the Azure portal
     * ```
     * CLIENT_ENDPOINT="<ENDPOINT>" \
     * AZURE_CLIENT_KEY="<KEY>" \
     * APPLICATIONINSIGHTS_CONNECTION_STRING="<CONNECTION STRING>" \
     * MAVEN_OPTS="-javaagent:/tmp/applicationinsights-agent-3.6.1.jar" \
     * ../../../mvnw package exec:java -Dsample="java.FunctionTelemetry_Example"
     * ```
     * 
     * If you open the Application Insights "Live metrics" view while running this example, you
     * should see the telemetry in real-time.
     * Otherwise within a few minutes, you should see the telemetry in the Application Insights ->
     * Investigate -> Transaction search ui in the Azure portal.
     */

    private static final String CLIENT_KEY = System.getenv("CLIENT_KEY");
    private static final String AZURE_CLIENT_KEY = System.getenv("AZURE_CLIENT_KEY");

    // Only required if AZURE_CLIENT_KEY is set
    private static final String CLIENT_ENDPOINT = System.getenv("CLIENT_ENDPOINT");
    private static final String MODEL_ID = "gpt-4o";

    public static void main(String[] args)
        throws ConfigurationException, IOException, NoSuchMethodException, InterruptedException {
        requestsWithSpanContext();
        testNestedCalls();
        requestsWithScope();

        Thread.sleep(1000);
    }

    private static void requestsWithSpanContext() throws IOException {
        Span fakeRequest = GlobalOpenTelemetry.getTracer("Custom")
            .spanBuilder("GET /requestsWithSpanContext")
            .setSpanKind(SpanKind.SERVER)
            .setAttribute("http.request.method", "GET")
            .setAttribute("url.path", "/requestsWithSpanContext")
            .setAttribute("url.scheme", "http")
            .startSpan();

        // Pass span context to the telemetry object to correlate telemetry with the request
        SemanticKernelTelemetry telemetry = new SemanticKernelTelemetry(
            GlobalOpenTelemetry.getTracer("Custom"),
            fakeRequest.getSpanContext());

        sequentialFunctionCalls(telemetry);

        fakeRequest.setStatus(StatusCode.OK);
        fakeRequest.end();
    }

    private static void requestsWithScope() throws IOException {
        Span fakeRequest = GlobalOpenTelemetry.getTracer("Custom")
            .spanBuilder("GET /requestsWithScope")
            .setSpanKind(SpanKind.SERVER)
            .setAttribute("http.request.method", "GET")
            .setAttribute("url.path", "/requestsWithScope")
            .setAttribute("url.scheme", "http")
            .startSpan();

        // Pass span context to the telemetry object to correlate telemetry with the request
        SemanticKernelTelemetry telemetry = new SemanticKernelTelemetry();

        try (Scope scope = fakeRequest.makeCurrent()) {
            sequentialFunctionCalls(telemetry);
        }

        fakeRequest.setStatus(StatusCode.OK);
        fakeRequest.end();
    }

    public static void sequentialFunctionCalls(SemanticKernelTelemetry telemetry) {

        OpenAIAsyncClient client;

        if (AZURE_CLIENT_KEY != null) {
            client = new OpenAIClientBuilder()
                .credential(new AzureKeyCredential(AZURE_CLIENT_KEY))
                .endpoint(CLIENT_ENDPOINT)
                .buildAsyncClient();

        } else {
            client = new OpenAIClientBuilder()
                .credential(new KeyCredential(CLIENT_KEY))
                .buildAsyncClient();
        }

        ChatCompletionService chat = OpenAIChatCompletion.builder()
            .withModelId(MODEL_ID)
            .withOpenAIAsyncClient(client)
            .build();

        var plugin = KernelPluginFactory.createFromObject(new PetPlugin(), "PetPlugin");

        var kernel = Kernel.builder()
            .withAIService(ChatCompletionService.class, chat)
            .withPlugin(plugin)
            .build();

        var chatHistory = new ChatHistory();
        chatHistory.addUserMessage(
            "What is the name and type of the pet with id ca2fc6bc-1307-4da6-a009-d7bf88dec37b?");

        var messages = chat.getChatMessageContentsAsync(
            chatHistory,
            kernel,
            InvocationContext.builder()
                .withToolCallBehavior(ToolCallBehavior.allowAllKernelFunctions(true))
                .withReturnMode(InvocationReturnMode.FULL_HISTORY)
                .withTelemetry(telemetry)
                .build())
            .block();

        chatHistory = new ChatHistory(messages);

        System.out.println(
            "THE NAME AND TYPE IS: " + chatHistory.getLastMessage().get().getContent());
    }

    public static void testNestedCalls() {

        OpenAIAsyncClient client;

        if (AZURE_CLIENT_KEY != null) {
            client = new OpenAIClientBuilder()
                .credential(new AzureKeyCredential(AZURE_CLIENT_KEY))
                .endpoint(CLIENT_ENDPOINT)
                .buildAsyncClient();

        } else {
            client = new OpenAIClientBuilder()
                .credential(new KeyCredential(CLIENT_KEY))
                .buildAsyncClient();
        }

        ChatCompletionService chat = OpenAIChatCompletion.builder()
            .withModelId(MODEL_ID)
            .withOpenAIAsyncClient(client)
            .build();

        var plugin = KernelPluginFactory.createFromObject(new TextAnalysisPlugin(),
            "TextAnalysisPlugin");

        var kernel = Kernel.builder()
            .withAIService(ChatCompletionService.class, chat)
            .withPlugin(plugin)
            .build();

        SemanticKernelTelemetry telemetry = new SemanticKernelTelemetry();

        Span span = GlobalOpenTelemetry.getTracer("Test")
            .spanBuilder("testNestedCalls span")
            .setSpanKind(SpanKind.SERVER)
            .startSpan();

        try (Scope scope = span.makeCurrent()) {
            String analysed = kernel
                .invokePromptAsync(
                    """
                        Analyse the following text:
                        Hello There
                        """,
                    KernelArguments.builder().build(),
                    InvocationContext.builder()
                        .withToolCallBehavior(ToolCallBehavior.allowAllKernelFunctions(true))
                        .withReturnMode(InvocationReturnMode.NEW_MESSAGES_ONLY)
                        .withTelemetry(telemetry)
                        .build())
                .withResultType(String.class)
                .map(result -> {
                    return result.getResult();
                })
                .block();
            System.out.println(analysed);
        } finally {
            span.end();
        }

    }

    public static class TextAnalysisPlugin {

        @DefineKernelFunction(description = "Change all string chars to uppercase.", name = "Uppercase")
        public String uppercase(
            @KernelFunctionParameter(description = "Text to uppercase", name = "input") String text) {
            return text.toUpperCase(Locale.ROOT);
        }

        @DefineKernelFunction(name = "sha256sum", description = "Calculates a sha256 of the input", returnType = "string")
        public Mono<String> sha256sum(
            @KernelFunctionParameter(name = "input", description = "The input to checksum", type = String.class) String input,
            Kernel kernel,
            SemanticKernelTelemetry telemetry) throws NoSuchAlgorithmException {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            String hashStr = new BigInteger(1, hash).toString(16);

            return kernel
                .invokePromptAsync(
                    """
                        Uppercase the following text:
                        === BEGIN TEXT ===
                        %s
                        === END TEXT ===
                        """.formatted(hashStr)
                        .stripIndent(),
                    null,
                    InvocationContext.builder()
                        .withToolCallBehavior(ToolCallBehavior.allowAllKernelFunctions(true))
                        .withReturnMode(InvocationReturnMode.NEW_MESSAGES_ONLY)
                        .withTelemetry(telemetry)
                        .build())
                .withResultType(String.class)
                .map(result -> {
                    return result.getResult();
                });
        }

        @DefineKernelFunction(name = "formatAnswer", description = "Formats an answer", returnType = "string")
        public Mono<String> formatAnswer(
            @KernelFunctionParameter(name = "input", description = "The input to format", type = String.class) String input,
            Kernel kernel,
            SemanticKernelTelemetry telemetry) throws ServiceNotFoundException {

            return kernel
                .invokePromptAsync(
                    """
                        Translate the following text into Italian:
                        === BEGIN TEXT ===
                        %s
                        === END TEXT ===
                        """.formatted(input)
                        .stripIndent())
                .withResultType(String.class)
                .map(result -> {
                    return result.getResult();
                });
        }

        @DefineKernelFunction(name = "analyseInput", description = "Gives a text analysis of the input", returnType = "string")
        public Mono<String> analyseInput(
            @KernelFunctionParameter(name = "input", description = "The input to analyse", type = String.class) String input,
            Kernel kernel,
            SemanticKernelTelemetry telemetry) throws ServiceNotFoundException {

            return kernel
                .invokePromptAsync(
                    """
                        Calculating sha256sum of the following text:
                        === BEGIN TEXT ===
                        %s
                        === END TEXT ===
                        """.formatted(input)
                        .stripIndent(),
                    null,
                    InvocationContext.builder()
                        .withToolCallBehavior(ToolCallBehavior.allowAllKernelFunctions(true))
                        .withReturnMode(InvocationReturnMode.NEW_MESSAGES_ONLY)
                        .withTelemetry(telemetry)
                        .build())
                .withResultType(String.class)
                .map(result -> {
                    return result.getResult();
                })
                .flatMap(answer -> {
                    return kernel
                        .invokePromptAsync(
                            """
                                Format the following text:
                                === BEGIN TEXT ===
                                %s
                                === END TEXT ===
                                """.formatted(answer)
                                .stripIndent())
                        .withInvocationContext(
                            InvocationContext.builder()
                                .withToolCallBehavior(
                                    ToolCallBehavior.allowAllKernelFunctions(true))
                                .withReturnMode(InvocationReturnMode.NEW_MESSAGES_ONLY)
                                .withTelemetry(telemetry)
                                .build())
                        .withArguments(null)
                        .withTelemetry(telemetry)
                        .withResultType(String.class);
                })
                .map(it -> {
                    return it.getResult();
                });
        }

    }

}
