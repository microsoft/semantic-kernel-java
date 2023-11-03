// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.syntaxexamples;

import static com.microsoft.semantickernel.DefaultKernelTest.mockCompletionOpenAIAsyncClient;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.SKBuilders;
import com.microsoft.semantickernel.coreskills.TimeSkill;
import com.microsoft.semantickernel.orchestration.SKContext;
import com.microsoft.semantickernel.semanticfunctions.PromptTemplate;
import com.microsoft.semantickernel.semanticfunctions.PromptTemplateConfig;
import com.microsoft.semantickernel.textcompletion.CompletionSKFunction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

public class Example06TemplateLanguageTest {

    @Test
    public void run() {
        OpenAIAsyncClient client =
                mockCompletionOpenAIAsyncClient(Tuples.of("Today is", "A-RESULT"));

        Kernel kernel =
                SKBuilders.kernel()
                        .withDefaultAIService(
                                SKBuilders.textCompletion()
                                        .withModelId("text-davinci-003")
                                        .withOpenAIClient(client)
                                        .build())
                        .build();

        // Load native skill into the kernel skill collection, sharing its functions
        // with prompt
        // templates
        // Functions loaded here are available as "time.*"
        kernel.importSkill(new TimeSkill(), "time");

        // Semantic Function invoking time.Date and time.Time native functions
        String functionDefinition =
                "\n"
                        + "Today is: {{time.Date}}\n"
                        + "Current time is: {{time.Time}}\n"
                        + "\n"
                        + "Answer to the following questions using JSON syntax, including the data"
                        + " used.\n"
                        + "Is it morning, afternoon, evening, or night"
                        + " (morning/afternoon/evening/night)?\n"
                        + "Is it weekend time (weekend/not weekend)?";

        PromptTemplate promptRenderer =
                SKBuilders.promptTemplate()
                        .withPromptTemplateConfig(new PromptTemplateConfig())
                        .withPromptTemplate(functionDefinition)
                        .withPromptTemplateEngine(kernel.getPromptTemplateEngine())
                        .build();

        SKContext skContext = SKBuilders.context().withSkills(kernel.getSkills()).build();

        Mono<String> renderedPrompt = promptRenderer.renderAsync(skContext);

        String renderedText = renderedPrompt.block();

        // Check that it has been rendered
        Assertions.assertTrue(!renderedText.contains("time.Date"));

        // Run the prompt / semantic function
        CompletionSKFunction kindOfDay =
                kernel.getSemanticFunctionBuilder()
                        .withKernel(kernel)
                        .withPromptTemplate(functionDefinition)
                        .withCompletionConfig(
                                new PromptTemplateConfig.CompletionConfig(0, 0, 0, 0, 256))
                        .build();

        Assertions.assertEquals("A-RESULT", kindOfDay.invokeAsync("").block().getResult());
    }
}
