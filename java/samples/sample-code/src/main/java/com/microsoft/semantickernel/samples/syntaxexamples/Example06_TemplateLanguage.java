// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.samples.syntaxexamples;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.KeyCredential;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.aiservices.openai.textcompletion.OpenAITextGenerationService;
import com.microsoft.semantickernel.exceptions.ConfigurationException;
import com.microsoft.semantickernel.orchestration.PromptExecutionSettings;
import com.microsoft.semantickernel.plugin.KernelPlugin;
import com.microsoft.semantickernel.plugin.KernelPluginFactory;
import com.microsoft.semantickernel.semanticfunctions.KernelFunction;
import com.microsoft.semantickernel.semanticfunctions.KernelPromptTemplateFactory;
import com.microsoft.semantickernel.semanticfunctions.PromptTemplateConfig;
import com.microsoft.semantickernel.semanticfunctions.annotations.DefineKernelFunction;
import com.microsoft.semantickernel.services.textcompletion.TextGenerationService;

/**
 * Show how to invoke a Native Function written in Java
 * from a Semantic Function written in natural language
 * <p>
 * Refer to the <a href=
 * "https://github.com/microsoft/semantic-kernel/blob/experimental-java/java/samples/sample-code/README.md">
 * README</a> for configuring your environment to run the examples.
 */
public class Example06_TemplateLanguage {

    private static final String CLIENT_KEY = System.getenv("CLIENT_KEY");
    private static final String AZURE_CLIENT_KEY = System.getenv("AZURE_CLIENT_KEY");

    // Only required if AZURE_CLIENT_KEY is set
    private static final String CLIENT_ENDPOINT = System.getenv("CLIENT_ENDPOINT");
    private static final String MODEL_ID = System.getenv()
        .getOrDefault("MODEL_ID", "text-davinci-003");

    public static void main(String[] args) throws ConfigurationException {
        System.out.println("======== TemplateLanguage ========");

        OpenAIAsyncClient client = SamplesConfig.getClient();

        Kernel kernel = SKBuilders.kernel()
                .withDefaultAIService(SKBuilders.textCompletion()
                        .withModelId("davinci-002")
                        .withOpenAIClient(client)
                        .build())
                .build();

        // Load native skill into the kernel skill collection, sharing its functions
        // with prompt templates
        // Functions loaded here are available as "time.*"
        kernel.importSkill(new TimeSkill(), "time");

        kernel = kernel.toBuilder()
            .withPlugin(time)
            .build();

        // Prompt Function invoking time.Date and time.Time method functions
        String functionDefinition = """
                Today is: {{time.Date}}
                Current time is: {{time.Time}}

                Answer to the following questions using JSON syntax, including the data used.
                Is it morning, afternoon, evening, or night (morning/afternoon/evening/night)?
                Is it weekend time (weekend/not weekend)?
                """;

        // This allows to see the prompt before it's sent to OpenAI
        System.out.println("--- Rendered Prompt");

        var promptTemplate = new KernelPromptTemplateFactory()
            .tryCreate(PromptTemplateConfig
                .builder()
                .withTemplate(functionDefinition)
                .build());

        SKContext skContext = SKBuilders
            .context()
            .withSkills(kernel.getSkills())
            .build();

        var kindOfDay = KernelFunction.createFromPrompt(functionDefinition)
            .withDefaultExecutionSettings(
                PromptExecutionSettings.builder()
                    .withMaxTokens(100)
                    .build())
            .withTemplateFormat(PromptTemplateConfig.SEMANTIC_KERNEL_TEMPLATE_FORMAT)
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

    public static class Time {

        @DefineKernelFunction(name = "date")
        public String date() {
            return "2021-09-01";
        }

        @DefineKernelFunction(name = "time")
        public String time() {
            return "12:00:00";
        }
    }
}
