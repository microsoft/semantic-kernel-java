// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.aiservices.openai.chatcompletion;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.microsoft.semantickernel.orchestration.responseformat.JsonSchemaResponseFormat;
import com.microsoft.semantickernel.plugin.KernelPlugin;
import com.microsoft.semantickernel.plugin.KernelPluginFactory;
import com.microsoft.semantickernel.semanticfunctions.KernelFunction;
import com.microsoft.semantickernel.semanticfunctions.annotations.DefineKernelFunction;
import com.microsoft.semantickernel.semanticfunctions.annotations.KernelFunctionParameter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

public class JsonSchemaTest {

    @Test
    public void jacksonGenerationTest() throws JsonProcessingException {
        JsonSchemaResponseFormat format = JsonSchemaResponseFormat.builder()
            .setResponseFormat(Foo.class)
            .setName("foo")
            .build();

        Assertions.assertEquals("foo", format.getJsonSchema().getName());

        Assertions.assertTrue(format.getJsonSchema().getSchema()
            .replaceAll("\\r\\n|\\r|\\n", "")
            .replaceAll(" +", "")
            .contains(
                "\"type\":\"object\",\"properties\":{\"bar\":{}}"));
    }

    @Test
    public void openAIFunctionTest() {
        KernelPlugin plugin = KernelPluginFactory.createFromObject(
            new TestPlugin(),
            "test");

        Assertions.assertNotNull(plugin);
        Assertions.assertEquals(plugin.getName(), "test");
        Assertions.assertEquals(plugin.getFunctions().size(), 3);

        KernelFunction<?> testFunction = plugin.getFunctions()
            .get("asyncPersonFunction");
        OpenAIFunction openAIFunction = OpenAIFunction.build(
            testFunction.getMetadata(),
            plugin.getName());
        System.out.println(openAIFunction.getFunctionDefinition());

    }


    public static class TestPlugin {

        @DefineKernelFunction
        public String testFunction(
            @KernelFunctionParameter(name = "input", description = "input string") String input) {
            return "test" + input;
        }

        @DefineKernelFunction(returnType = "int")
        public Mono<Integer> asyncTestFunction(
            @KernelFunctionParameter(name = "input") String input) {
            return Mono.just(1);
        }

        @DefineKernelFunction(returnType = "int", description = "test function description",
            name = "asyncPersonFunction", returnDescription = "test return description")
        public Mono<Integer> asyncPersonFunction(
            @KernelFunctionParameter(name = "person",description = "input person", type = Person.class) Person person,
            @KernelFunctionParameter(name = "input", description = "input string") String input) {
            return Mono.just(1);
        }
    }

    private static enum Title {
        MS,
        MRS,
        MR
    }

    public static class Person {
        @JsonPropertyDescription("The name of the person.")
        private String name;
        @JsonPropertyDescription("The age of the person.")
        private int age;
        @JsonPropertyDescription("The title of the person.")
        private Title title;


        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }

        public Title getTitle() {
            return title;
        }

        public void setTitle(Title title) {
            this.title = title;
        }
    }

}
