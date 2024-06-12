///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS com.microsoft.semantic-kernel:semantickernel-core:0.2.6-alpha
//DEPS com.microsoft.semantic-kernel:semantickernel-core-skills:0.2.6-alpha
//DEPS com.microsoft.semantic-kernel.connectors:semantickernel-connectors:0.2.6-alpha
//DEPS org.slf4j:slf4j-jdk14:2.0.7
//SOURCES syntaxexamples/SampleSkillsUtil.java,Config.java
package com.microsoft.semantickernel.samples;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.SKBuilders;
import com.microsoft.semantickernel.connectors.ai.openai.textcompletion.OpenAITextCompletion;
import com.microsoft.semantickernel.connectors.ai.openai.util.OpenAIClientProvider;
import com.microsoft.semantickernel.exceptions.ConfigurationException;
import com.microsoft.semantickernel.orchestration.SKContext;
import com.microsoft.semantickernel.orchestration.SKFunction;
import com.microsoft.semantickernel.samples.syntaxexamples.SampleSkillsUtil;
import com.microsoft.semantickernel.skilldefinition.ReadOnlyFunctionCollection;
import com.microsoft.semantickernel.textcompletion.CompletionSKFunction;
import java.io.IOException;
import reactor.core.publisher.Mono;

/**
 * Getting started
 * <p>
 * Create a conf.properties file based on the examples files at the root of this module.
 * <p>
 * <a href= "https://learn.microsoft.com/en-us/azure/cognitive-services/openai/quickstart">Get
 * started with Azure OpenAI</a>
 * <a href="https://openai.com/product">Get started with OpenAI</a>
 */
public class Example11_UseMultipleModels {

  /**
   * Returns a Semantic Kernel with Text Completion.
   *
   * @param client Client that will handle requests to AzureOpenAI or OpenAI.
   * @return Kernel.
   */
  public static Kernel getKernel(OpenAIAsyncClient client) {
    OpenAITextCompletion davinciTextCompletion = new OpenAITextCompletion(client,
        "text-davinci-003");
    OpenAITextCompletion adaTextCompletion = new OpenAITextCompletion(client, "text-ada-001");

    Kernel kernel = SKBuilders.kernel()
        // Add one of this serviceId to config.json in FunSkill to test difference models
        .withAIService("text-davinci-003", davinciTextCompletion, false, OpenAITextCompletion.class)
        .withAIService("text-ada-001", adaTextCompletion, false, OpenAITextCompletion.class)
        .build();

    return kernel;
  }

  /**
   * Imports 'FunSkill' from directory examples and runs the 'Joke' function within it.
   *
   * @param kernel Kernel with Text Completion.
   */
  public static void runWithServiceDefinedAtInvocation(Kernel kernel) {
    ReadOnlyFunctionCollection skill = kernel
        .importSkillFromDirectory("FunSkill", SampleSkillsUtil.detectSkillDirLocation(),
            "FunSkill");

    CompletionSKFunction function = skill.getFunction("Joke",
        CompletionSKFunction.class);

    Mono<SKContext> result = function.invokeAsync("time travel to dinosaur age",
        SKBuilders
            .completionRequestSettings()
            .serviceId("text-davinci-003")
            .build());

    String davinci = result.block().getResult();

    result = function.invokeAsync("time travel to dinosaur age",
        SKBuilders
            .completionRequestSettings()
            .serviceId("text-ada-001")
            .build());

    String ada = result.block().getResult();

    System.out.println("=== RESULT FROM text-davinci-003 ===");
    System.out.println(davinci);
    System.out.println("====================================");

    System.out.println("===== RESULT FROM text-ada-001 =====");
    System.out.println(ada);
    System.out.println("====================================");
  }


  public static void runWithServiceDefinedByInlineFunction(Kernel kernel) {
    // Create function via kernel
    var function = kernel.
        getSemanticFunctionBuilder()
        .withPromptTemplate("tell a joke about {{$input}}")
        .withRequestSettings(
            SKBuilders.completionRequestSettings()
                .temperature(0.4)
                .topP(1)
                .maxTokens(100)
                .serviceId("text-davinci-003")
                .build())
        .build();

    String davinci = function.invokeAsync("time travel to dinosaur age").block().getResult();

    System.out.println("=== RESULT FROM text-davinci-003 ===");
    System.out.println(davinci);
    System.out.println("====================================");
  }


  public static void runWithServiceDefinedInFile(Kernel kernel) {
    ReadOnlyFunctionCollection skills = kernel.importSkillFromResources(
        "Plugins",
        "ExamplePlugins",
        "ExampleFunctionWithService"
    );

    SKFunction<?> function = skills.getFunction("ExampleFunctionWithService");

    System.out.println("=== RESULT FROM text-davinci-003 ===");
    System.out.println(function.invokeAsync().block().getResult());
    System.out.println("====================================");
  }


  public static void run(OpenAIAsyncClient client) {
    Kernel kernel = getKernel(client);
    runWithServiceDefinedInFile(kernel);
    runWithServiceDefinedAtInvocation(kernel);
    runWithServiceDefinedByInlineFunction(kernel);
  }

  public static void main(String args[]) throws ConfigurationException, IOException {
    run(OpenAIClientProvider.getClient());
  }
}
