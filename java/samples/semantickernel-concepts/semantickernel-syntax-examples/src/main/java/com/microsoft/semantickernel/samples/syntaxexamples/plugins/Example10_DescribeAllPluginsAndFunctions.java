// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.samples.syntaxexamples.plugins;

import java.nio.file.Path;
import java.util.Locale;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.KeyCredential;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.Kernel.Builder;
import com.microsoft.semantickernel.aiservices.openai.textcompletion.OpenAITextGenerationService;
import com.microsoft.semantickernel.orchestration.PromptExecutionSettings;
import com.microsoft.semantickernel.plugin.KernelPluginFactory;
import com.microsoft.semantickernel.samples.plugins.text.TextPlugin;
import com.microsoft.semantickernel.semanticfunctions.KernelFunction;
import com.microsoft.semantickernel.semanticfunctions.KernelFunctionFromPrompt;
import com.microsoft.semantickernel.semanticfunctions.KernelFunctionMetadata;
import com.microsoft.semantickernel.semanticfunctions.annotations.DefineKernelFunction;
import com.microsoft.semantickernel.semanticfunctions.annotations.KernelFunctionParameter;
import com.microsoft.semantickernel.services.textcompletion.TextGenerationService;

public class Example10_DescribeAllPluginsAndFunctions {

    private static final String PLUGIN_DIR = System.getenv("PLUGIN_DIR") == null ? "."
        : System.getenv("PLUGIN_DIR");
    private static final String CLIENT_KEY = System.getenv("CLIENT_KEY");
    private static final String AZURE_CLIENT_KEY = System.getenv("AZURE_CLIENT_KEY");

    // Only required if AZURE_CLIENT_KEY is set
    private static final String CLIENT_ENDPOINT = System.getenv("CLIENT_ENDPOINT");
    private static final String MODEL_ID = System.getenv()
        .getOrDefault("MODEL_ID", "text-davinci-003");

    /// <summary>
    /// Print a list of all the functions imported into the kernel, including function descriptions,
    /// list of parameters, parameters descriptions, etc.
    /// See the end of the file for a sample of what the output looks like.
    /// </summary>
    public static void main(String[] args) {
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

        TextGenerationService textGenerationService = OpenAITextGenerationService.builder()
            .withOpenAIAsyncClient(client)
            .withModelId(MODEL_ID)
            .build();

        Builder kernelBuilder = Kernel.builder()
            .withAIService(TextGenerationService.class, textGenerationService);

        kernelBuilder.withPlugin(
            KernelPluginFactory.createFromObject(
                new StaticTextPlugin(), "StaticTextPlugin"));

        // Import another native plugin
        kernelBuilder.withPlugin(
            KernelPluginFactory.createFromObject(
                new TextPlugin(), "AnotherTextPlugin"));

        kernelBuilder.withPlugin(
            KernelPluginFactory
                .importPluginFromDirectory(
                    Path.of(PLUGIN_DIR,
                        "java/samples/semantickernel-concepts/semantickernel-syntax-examples/src/main/resources/Plugins"),
                    "SummarizePlugin",
                    null));

        // Not added to kernel so should not be printed
        var jokeFunction = KernelFunctionFromPrompt.builder()
            .withTemplate("tell a joke about {{$input}}")
            .withDefaultExecutionSettings(
                PromptExecutionSettings.builder()
                    .withMaxTokens(150)
                    .build())
            .build();

        // Not added to kernel so should not be printed
        var writeNovel = KernelFunctionFromPrompt.builder()
            .withTemplate("write a novel about {{$input}} in {{$language}} language")
            .withName("Novel")
            .withDescription("Write a bedtime story")
            .withDefaultExecutionSettings(
                PromptExecutionSettings.builder()
                    .withMaxTokens(150)
                    .build())
            .build();

        System.out.println("**********************************************");
        System.out.println("****** Registered plugins and functions ******");
        System.out.println("**********************************************");
        System.out.println();

        Kernel kernel = kernelBuilder.build();
        kernel.getPlugins()
            .forEach(plugin -> {
                System.out.println("Plugin: " + plugin.getName());
                plugin
                    .getFunctions()
                    .values()
                    .stream()
                    .map(KernelFunction::getMetadata)
                    .forEach(Example10_DescribeAllPluginsAndFunctions::printFunction);
            });
    }

    private static void printFunction(KernelFunctionMetadata<?> func) {
        System.out.println("   " + func.getName() + ": " + func.getDescription());

        if (!func.getParameters().isEmpty()) {
            System.out.println("      Params:");

            func.getParameters()
                .forEach(p -> {
                    System.out.println("      - " + p.getName() + ": " + p.getDescription());
                    System.out.println("        default: '" + p.getDefaultValue() + "'");
                });
        }

        System.out.println();
    }

    public static class StaticTextPlugin {

        @DefineKernelFunction(description = "Change all string chars to uppercase", name = "uppercase")
        public static String uppercase(
            @KernelFunctionParameter(description = "Text to uppercase", name = "input") String input) {
            return input.toUpperCase(Locale.ROOT);
        }

        @DefineKernelFunction(description = "Append the day variable", name = "appendDay")
        public String appendDay(
            @KernelFunctionParameter(description = "Append the day variable", name = "appendDay") String input,
            @KernelFunctionParameter(description = "Value of the day to append", name = "day") String day) {
            return input + day;
        }
    }
}
