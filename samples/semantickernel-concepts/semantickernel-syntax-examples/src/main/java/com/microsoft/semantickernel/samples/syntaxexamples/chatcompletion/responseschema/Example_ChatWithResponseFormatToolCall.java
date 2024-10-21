// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.samples.syntaxexamples.chatcompletion.responseschema;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.KeyCredential;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.aiservices.openai.chatcompletion.OpenAIChatCompletion;
import com.microsoft.semantickernel.orchestration.FunctionResult;
import com.microsoft.semantickernel.orchestration.PromptExecutionSettings;
import com.microsoft.semantickernel.orchestration.ToolCallBehavior;
import com.microsoft.semantickernel.plugin.KernelPluginFactory;
import com.microsoft.semantickernel.samples.syntaxexamples.chatcompletion.responseschema.Pet.AnimalType;
import com.microsoft.semantickernel.samples.syntaxexamples.chatcompletion.responseschema.Pet.Weight;
import com.microsoft.semantickernel.samples.syntaxexamples.chatcompletion.responseschema.Pet.WeightUnit;
import com.microsoft.semantickernel.semanticfunctions.annotations.DefineKernelFunction;
import com.microsoft.semantickernel.semanticfunctions.annotations.KernelFunctionParameter;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;

public class Example_ChatWithResponseFormatToolCall {

    private static final String CLIENT_KEY = System.getenv("CLIENT_KEY");
    private static final String AZURE_CLIENT_KEY = System.getenv("AZURE_CLIENT_KEY");

    // Only required if AZURE_CLIENT_KEY is set
    private static final String CLIENT_ENDPOINT = System.getenv("CLIENT_ENDPOINT");
    private static final String MODEL_ID = System.getenv()
        .getOrDefault("MODEL_ID", "gpt-4o");

    public static void main(String[] args) throws InterruptedException, JsonProcessingException {

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

        ChatCompletionService openAIChatCompletion = OpenAIChatCompletion.builder()
            .withOpenAIAsyncClient(client)
            .withModelId(MODEL_ID)
            .build();

        Kernel kernel = Kernel.builder()
            .withAIService(ChatCompletionService.class, openAIChatCompletion)
            .withPlugin(KernelPluginFactory.createFromObject(new PetStore(), "PetStore"))
            .build();

        PromptExecutionSettings promptExecutionSettings = PromptExecutionSettings.builder()
            .withJsonSchemaResponseFormat(Pet.class)
            .build();

        FunctionResult<Pet> response = kernel.invokePromptAsync("Get pet with id 1234")
            .withResultTypeAutoConversion(Pet.class)
            .withPromptExecutionSettings(promptExecutionSettings)
            .withToolCallBehavior(ToolCallBehavior.allowAllKernelFunctions(true))
            .block();

        System.out.println(new ObjectMapper()
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(response.getResult()));
    }

    public static class PetStore {

        @DefineKernelFunction(name = "getPetById", returnType = "com.microsoft.semantickernel.samples.syntaxexamples.chatcompletion.responseschema.Pet")
        public Pet getPetById(
            @KernelFunctionParameter(name = "id") String id) {
            if (id.equals("1234")) {
                return new Pet(
                    "Test name",
                    AnimalType.CAT,
                    5,
                    new Weight(5.0, WeightUnit.KG));
            }
            return null;
        }
    }

}
