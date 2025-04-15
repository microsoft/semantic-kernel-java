// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.samples.syntaxexamples.java;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.KeyCredential;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.aiservices.openai.chatcompletion.OpenAIChatCompletion;
import com.microsoft.semantickernel.contextvariables.ContextVariableTypeConverter;
import com.microsoft.semantickernel.contextvariables.ContextVariableTypes;
import com.microsoft.semantickernel.contextvariables.converters.ContextVariableJacksonConverter;
import com.microsoft.semantickernel.exceptions.ConfigurationException;
import com.microsoft.semantickernel.semanticfunctions.KernelArguments;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CustomTypes_Example {

    private static final String CLIENT_KEY = System.getenv("CLIENT_KEY");
    private static final String AZURE_CLIENT_KEY = System.getenv("AZURE_CLIENT_KEY");

    // Only required if AZURE_CLIENT_KEY is set
    private static final String CLIENT_ENDPOINT = System.getenv("CLIENT_ENDPOINT");
    private static final String MODEL_ID = System.getenv()
        .getOrDefault("MODEL_ID", "gpt-35-turbo-2");

    public static void main(String[] args) throws ConfigurationException, IOException {

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

        ChatCompletionService chatCompletionService = OpenAIChatCompletion.builder()
            .withOpenAIAsyncClient(client)
            .withModelId(MODEL_ID)
            .build();

        exampleBuildingCustomConverter(chatCompletionService);
        exampleUsingJackson(chatCompletionService);
        exampleUsingGlobalTypes(chatCompletionService);
    }

    public record Pet(String name, int age, String species) {

        @JsonCreator
        public Pet(
            @JsonProperty("name") String name,
            @JsonProperty("age") int age,
            @JsonProperty("species") String species) {
            this.name = name;
            this.age = age;
            this.species = species;
        }

        @Override
        public String toString() {
            return name + " " + species + " " + age;
        }
    }

    private static void exampleBuildingCustomConverter(
        ChatCompletionService chatCompletionService) {
        Pet sandy = new Pet("Sandy", 3, "Dog");

        Kernel kernel = Kernel.builder()
            .withAIService(ChatCompletionService.class, chatCompletionService)
            .build();

        // Format:
        //   name: Sandy
        //   age: 3
        //   species: Dog

        // Custom serializer
        Function<Pet, String> petToString = pet -> "name: " + pet.name() + "\n" +
            "age: " + pet.age() + "\n" +
            "species: " + pet.species() + "\n";

        // Custom deserializer
        Function<String, Pet> stringToPet = prompt -> {
            Map<String, String> properties = Arrays.stream(prompt.split("\n"))
                .collect(Collectors.toMap(
                    line -> line.split(":")[0].trim(),
                    line -> line.split(":")[1].trim()));

            return new Pet(
                properties.get("name"),
                Integer.parseInt(properties.get("age")),
                properties.get("species"));
        };

        // create custom converter
        ContextVariableTypeConverter<Pet> typeConverter = ContextVariableTypeConverter.builder(
            Pet.class)
            .toPromptString(petToString)
            .fromPromptString(stringToPet)
            .build();

        Pet updated = kernel.invokePromptAsync(
            "Change Sandy's name to Daisy:\n{{$Sandy}}",
            KernelArguments.builder()
                .withVariable("Sandy", sandy, typeConverter)
                .build())
            .withTypeConverter(typeConverter)
            .withResultType(Pet.class)
            .block()
            .getResult();

        System.out.println("Sandy's updated record: " + updated);
    }

    public static void exampleUsingJackson(ChatCompletionService chatCompletionService) {
        Pet sandy = new Pet("Sandy", 3, "Dog");

        Kernel kernel = Kernel.builder()
            .withAIService(ChatCompletionService.class, chatCompletionService)
            .build();

        // Create a converter that defaults to using jackson for serialization
        ContextVariableTypeConverter<Pet> typeConverter = ContextVariableJacksonConverter.create(
            Pet.class);

        // Invoke the prompt with the custom converter
        Pet updated = kernel.invokePromptAsync(
            "Increase Sandy's age by a year:\n{{$Sandy}}",
            KernelArguments.builder()
                .withVariable("Sandy", sandy, typeConverter)
                .build())
            .withTypeConverter(typeConverter)
            .withResultType(Pet.class)
            .block()
            .getResult();

        System.out.println("Sandy's updated record: " + updated);
    }

    public static void exampleUsingGlobalTypes(ChatCompletionService chatCompletionService) {
        Pet sandy = new Pet("Sandy", 3, "Dog");

        Kernel kernel = Kernel.builder()
            .withAIService(ChatCompletionService.class, chatCompletionService)
            .build();

        // Create a converter that defaults to using jackson for serialization
        ContextVariableTypeConverter<Pet> typeConverter = ContextVariableJacksonConverter.create(
            Pet.class);

        // Add converter to global types
        ContextVariableTypes.addGlobalConverter(typeConverter);

        // No need to explicitly tell the invocation how to convert the type
        Pet updated = kernel.invokePromptAsync(
            "Sandy's is actually a cat correct this:\n{{$Sandy}}",
            KernelArguments.builder()
                .withVariable("Sandy", sandy)
                .build())
            .withResultType(Pet.class)
            .block()
            .getResult();

        System.out.println("Sandy's updated record: " + updated);
    }

}
