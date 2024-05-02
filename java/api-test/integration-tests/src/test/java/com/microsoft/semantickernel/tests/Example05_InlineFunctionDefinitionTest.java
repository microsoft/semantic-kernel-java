// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.tests;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.orchestration.FunctionResult;
import com.microsoft.semantickernel.orchestration.PromptExecutionSettings;
import com.microsoft.semantickernel.semanticfunctions.KernelFunction;
import com.microsoft.semantickernel.semanticfunctions.KernelFunctionArguments;
import com.microsoft.semantickernel.semanticfunctions.KernelFunctionFromPrompt;
import com.microsoft.semantickernel.services.textcompletion.TextGenerationService;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@WireMockTest
public class Example05_InlineFunctionDefinitionTest {

    @Test
    public void main(WireMockRuntimeInfo wmRuntimeInfo) {
        final OpenAIAsyncClient client = new OpenAIClientBuilder()
            .endpoint("http://localhost:" + wmRuntimeInfo.getHttpPort())
            .buildAsyncClient();

        TextGenerationService textGenerationService = TextGenerationService.builder()
            .withOpenAIAsyncClient(client)
            .withModelId("text-davinci-003")
            .build();

        Kernel kernel = Kernel.builder()
            .withAIService(TextGenerationService.class, textGenerationService)
            .build();

        System.out.println("======== Inline Function Definition ========");

        // Function defined using few-shot design pattern
        String promptTemplate = """
                Generate a creative reason or excuse for the given event.
                Be creative and be funny. Let your imagination run wild.

                Event: I am running late.
                Excuse: I was being held ransom by giraffe gangsters.

                Event: I haven't been to the gym for a year
                Excuse: I've been too busy training my pet dragon.

                Event: {{$input}}
            """.stripIndent();

        var excuseFunction = new KernelFunctionFromPrompt.Builder<String>()
            .withName("Excuse")
            .withTemplate(promptTemplate)
            .withDefaultExecutionSettings(
                new PromptExecutionSettings.Builder()
                    .withTemperature(0.4)
                    .withTopP(1)
                    .withMaxTokens(100)
                    .build())
            .build();

        WireMockUtil.mockCompletionResponse("I missed the F1 final race", "a-response");

        var result = kernel.invokeAsync(excuseFunction)
            .withArguments(
                KernelFunctionArguments.builder()
                    .withInput("I missed the F1 final race")
                    .build())
            .block();

        Assertions.assertEquals("a-response", result.getResult());

        WireMockUtil.mockCompletionResponse("sorry I forgot your birthday", "a-response-2");

        result = kernel.invokeAsync(excuseFunction)
            .withArguments(
                KernelFunctionArguments.builder()
                    .withInput("sorry I forgot your birthday")
                    .build())
            .block();

        Assertions.assertEquals("a-response-2", result.getResult());

        WireMockUtil.mockCompletionResponse("Translate this date ", "a-response-3");

        var date = DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneOffset.UTC)
            .format(Instant.ofEpochSecond(1));
        var message = "Translate this date " + date + " to French format";
        var fixedFunction = KernelFunction.<String>createFromPrompt(message)
            .withDefaultExecutionSettings(
                PromptExecutionSettings.builder()
                    .withMaxTokens(100)
                    .build())
            .build();

        FunctionResult<String> fixedFunctionResult = kernel
            .invokeAsync(fixedFunction)
            .block();

        Assertions.assertEquals("a-response-3", fixedFunctionResult.getResult());

    }
}
