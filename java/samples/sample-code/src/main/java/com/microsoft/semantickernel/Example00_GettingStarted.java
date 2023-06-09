package com.microsoft.semantickernel;

import java.io.IOException;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.microsoft.semantickernel.builders.SKBuilders;
import com.microsoft.semantickernel.extensions.KernelExtensions;
import com.microsoft.semantickernel.skilldefinition.ReadOnlyFunctionCollection;
import com.microsoft.semantickernel.textcompletion.CompletionSKContext;
import com.microsoft.semantickernel.textcompletion.CompletionSKFunction;

import reactor.core.publisher.Mono;

/**
 * Getting started
 *
 * Create a conf.properties file based on the examples files at the root of this
 * module.
 *
 * <a href=
 * "https://learn.microsoft.com/en-us/azure/cognitive-services/openai/quickstart">Get
 * started with Azure OpenAI</a>
 * <a href="https://openai.com/product">Get started with OpenAI</a>
 */
public class Example00_GettingStarted {

  /**
   * Returns a Semantic Kernel with Text Completion.
   *
   * @param client Client that will handle requests to AzureOpenAI or OpenAI.
   * @return
   */
  public static Kernel getKernel(OpenAIAsyncClient client) {
    KernelConfig config = SKBuilders.kernelConfig()
        .addTextCompletionService("davinci",
            kernel -> SKBuilders.textCompletionService().build(client, "text-davinci-003"))
        .build();

    Kernel kernel = SKBuilders.kernel()
        .setKernelConfig(config)
        .build();

    return kernel;
  }

  /**
   * Imports 'FunSkill' from directory examples and runs the 'Joke' function
   * within it.
   *
   * @param kernel Kernel with Text Completion.
   */
  public static void joke(Kernel kernel) {

    ReadOnlyFunctionCollection skill = kernel
        .importSkill("FunSkill", KernelExtensions.importSemanticSkillFromDirectory(
            "samples/skills", "FunSkill"));

    CompletionSKFunction function = skill.getFunction("Joke",
        CompletionSKFunction.class);

    Mono<CompletionSKContext> result = function.invokeAsync("time travel to dinosaur age");

    if (result != null) {
      System.out.println(result.block().getResult());
    }
  }

  public static void run(Config.ClientType clientType) throws IOException {
    Kernel kernel = getKernel(clientType.getClient());
    joke(kernel);
  }

  public static void main(String args[]) throws IOException {
    // Send one of Config.ClientType.OPEN_AI or Config.ClientType.AZURE_OPEN_AI
    run(Config.ClientType.OPEN_AI);
  }
}
