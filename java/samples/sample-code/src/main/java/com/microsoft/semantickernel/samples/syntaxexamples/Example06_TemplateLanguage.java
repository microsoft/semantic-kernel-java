// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.samples.syntaxexamples;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.SKBuilders;
import com.microsoft.semantickernel.SamplesConfig;
import com.microsoft.semantickernel.coreskills.TimeSkill;
import com.microsoft.semantickernel.exceptions.ConfigurationException;
import com.microsoft.semantickernel.orchestration.SKContext;
import com.microsoft.semantickernel.semanticfunctions.PromptTemplateConfig;

/**
 * Show how to invoke a Native Function written in Java
 * from a Semantic Function written in natural language
 * <p>
 * Refer to the <a href=
 * "https://github.com/microsoft/semantic-kernel/blob/experimental-java/java/samples/sample-code/README.md">
 * README</a> for configuring your environment to run the examples.
 */
public class Example06_TemplateLanguage {

    public static void main(String[] args) throws ConfigurationException {
        System.out.println("======== TemplateLanguage ========");

        OpenAIAsyncClient client = SamplesConfig.getClient();

        Kernel kernel = SKBuilders.kernel()
                .withDefaultAIService(SKBuilders.textCompletion()
                        .withModelId("text-davinci-003")
                        .withOpenAIClient(client)
                        .build())
                .build();

        // Load native skill into the kernel skill collection, sharing its functions
        // with prompt templates
        // Functions loaded here are available as "time.*"
        kernel.importSkill(new TimeSkill(), "time");

        // Semantic Function invoking time.Date and time.Time native functions
        String functionDefinition = """
                Today is: {{time.Date}}
                Current time is: {{time.Time}}

                Answer to the following questions using JSON syntax, including the data used.
                Is it morning, afternoon, evening, or night (morning/afternoon/evening/night)?
                Is it weekend time (weekend/not weekend)?
                """;

        // This allows to see the prompt before it's sent to OpenAI
        System.out.println("--- Rendered Prompt");

        var promptRenderer = SKBuilders.promptTemplate()
                .withPromptTemplateConfig(new PromptTemplateConfig())
                .withPromptTemplate(functionDefinition)
                .withPromptTemplateEngine(kernel.getPromptTemplateEngine())
                .build();

        SKContext skContext = SKBuilders
                .context()
                .withSkills(kernel.getSkills())
                .build();

        var renderedPrompt = promptRenderer.renderAsync(skContext);
        System.out.println(renderedPrompt.block());

        // Run the prompt / semantic function
        var kindOfDay = kernel
                .getSemanticFunctionBuilder()
                .withPromptTemplate(functionDefinition)
                .withCompletionConfig(
                        new PromptTemplateConfig.CompletionConfig(
                                0, 0, 0, 0, 256))
                .build();

        // Show the result
        System.out.println("--- Semantic Function result");
        var result = kindOfDay.invokeAsync("").block().getResult();
        System.out.println(result);
        /*
         * OUTPUT:
         *
         * --- Rendered Prompt
         *
         * Today is: Friday, April 28, 2023
         * Current time is: 11:04:30 PM
         *
         * Answer to the following questions using JSON syntax, including the data used.
         * Is it morning, afternoon, evening, or night
         * (morning/afternoon/evening/night)?
         * Is it weekend time (weekend/not weekend)?
         *
         * --- Semantic Function result
         *
         * {
         * "date": "Friday, April 28, 2023",
         * "time": "11:04:30 PM",
         * "period": "night",
         * "weekend": "weekend"
         * }
         */
    }
}
