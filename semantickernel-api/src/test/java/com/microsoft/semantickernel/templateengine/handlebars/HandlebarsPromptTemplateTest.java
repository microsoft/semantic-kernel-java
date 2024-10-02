// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.templateengine.handlebars;

import static java.util.stream.Collectors.joining;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.contextvariables.ContextVariable;
import com.microsoft.semantickernel.contextvariables.ContextVariableTypeConverter;
import com.microsoft.semantickernel.contextvariables.converters.ContextVariableJacksonConverter;
import com.microsoft.semantickernel.orchestration.InvocationContext;
import com.microsoft.semantickernel.plugin.KernelPlugin;
import com.microsoft.semantickernel.plugin.KernelPluginFactory;
import com.microsoft.semantickernel.semanticfunctions.KernelFunctionArguments;
import com.microsoft.semantickernel.semanticfunctions.PromptTemplateConfig;
import com.microsoft.semantickernel.semanticfunctions.annotations.DefineKernelFunction;
import com.microsoft.semantickernel.semanticfunctions.annotations.KernelFunctionParameter;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import com.microsoft.semantickernel.services.chatcompletion.message.ChatMessageTextContent;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.apache.commons.text.StringEscapeUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author davidgrieve
 */
public class HandlebarsPromptTemplateTest {

    public HandlebarsPromptTemplateTest() {
    }

    public static void main(String[] args) {
        new HandlebarsPromptTemplateTest().testRenderAsync();
    }

    public static class StringFunctions {

        @DefineKernelFunction(name = "upper", description = "Converts a string to upper case.")
        public String upper(
            @KernelFunctionParameter(name = "input", required = true, description = "The string to convert to upper case", type = String.class) String input) {
            return input.toUpperCase(Locale.ROOT);
        }

        @DefineKernelFunction(name = "concat", description = "Concatenate the second string to the first string.")
        public String concat(
            @KernelFunctionParameter(name = "input", required = true, description = "The string to which the second string is concatenated.", type = String.class) String first,
            @KernelFunctionParameter(name = "suffix", required = true, description = "The string which is concatenated to the first string.", type = String.class) String suffix) {
            return first.concat(suffix);
        }

    }

    /**
     * Test of renderAsync method, of class HandlebarsPromptTemplate.
     */
    @Test
    void testRenderAsync() {

        List<String> choices = Arrays.asList("CHOICE-A", "CHOICE-B");

        List<ChatHistory> history = Arrays.asList(
            new ChatHistory(
                Arrays.asList(
                    ChatMessageTextContent.systemMessage("a"),
                    ChatMessageTextContent.userMessage("b"))),
            new ChatHistory(
                Arrays.asList(
                    ChatMessageTextContent.systemMessage("c"),
                    ChatMessageTextContent.userMessage("d"))));

        KernelPlugin kernelPlugin = KernelPluginFactory.createFromObject(
            new StringFunctions(),
            "string");

        Kernel kernel = Kernel.builder()
            .withPlugin(kernelPlugin)
            .build();

        PromptTemplateConfig promptTemplate = PromptTemplateConfig.builder()
            .withTemplate(
                "{{choices.[0]}}\n" +
                    "{{choices}}\n" +
                    "{{#each history}}\n" +
                    "    {{#each this}}\n" +
                    "        {{string-upper content}}\n" +
                    "    {{/each}}\n" +
                    "{{/each}}\n" +
                    "Hello World")
            // "{{string-concat input suffix}}") TODO - this is not working
            .withTemplateFormat("handlebars")
            .build();

        HandlebarsPromptTemplate instance = new HandlebarsPromptTemplate(promptTemplate);

        KernelFunctionArguments arguments = KernelFunctionArguments.builder()
            .withVariable("input", "Hello ")
            .withVariable("suffix", "World")
            .withVariable("choices", choices)
            .withVariable("history", history)
            .withVariable("kernelPlugins", Arrays.asList(kernelPlugin))
            .build();

        // Return from renderAsync is normalized to remove empty lines and leading/trailing whitespace
        String expResult = "CHOICE-A [CHOICE-A, CHOICE-B] A B C D Hello World";

        String result = instance.renderAsync(kernel, arguments, null).block();
        assertNotNull(result);

        String normalizedResult =
            // split result into lines
            Arrays.stream(result.split("\\r?\\n|\\r"))
                // remove leading and trailing whitespace
                .map(String::trim)
                // remove empty lines
                .filter(s -> !s.isEmpty())
                // put it back together
                .collect(joining(" "));
        assertEquals(expResult, normalizedResult);
    }

    public static class Foo {

        @JsonProperty("val")
        private final String val;

        public Foo(String val) {
            this.val = val;
        }

        public String getVal() {
            return val;
        }
    }

    @Test
    public void testSerializesObject() {
        PromptTemplateConfig promptTemplate = PromptTemplateConfig.builder()
            .withTemplate("{{input}}")
            .withTemplateFormat("handlebars")
            .build();

        HandlebarsPromptTemplate instance = new HandlebarsPromptTemplate(promptTemplate);

        KernelFunctionArguments arguments = KernelFunctionArguments.builder()
            .withVariable("input", new Foo("bar"),
                ContextVariableJacksonConverter.create(Foo.class))
            .build();

        // Return from renderAsync is normalized to remove empty lines and leading/trailing whitespace
        String expResult = StringEscapeUtils.escapeXml11("{  \"val\" : \"bar\"}");

        String result = instance.renderAsync(Kernel.builder().build(), arguments, null)
            .block();
        Assertions.assertEquals(expResult, result.replaceAll("\\r\\n|\\r|\\n", ""));
    }

    @Test
    public void testMessageContent() {
        PromptTemplateConfig promptTemplate = PromptTemplateConfig.builder()
            .withTemplate(
                "{{#each input}}\n" +
                    "<message role=\"{{role}}\">{{content}}</message>\n" +
                    "{{/each}}")
            .withTemplateFormat("handlebars")
            .build();

        HandlebarsPromptTemplate instance = new HandlebarsPromptTemplate(promptTemplate);

        KernelFunctionArguments arguments = KernelFunctionArguments.builder()
            .withVariable("input", new ChatHistory()
                .addAssistantMessage("foo")
                .addUserMessage("bar\"<>&"))
            .build();

        // Return from renderAsync is normalized to remove empty lines and leading/trailing whitespace
        String expResult = "<message role=\"ASSISTANT\">foo</message><message role=\"USER\">bar&quot;&lt;&gt;&amp;</message>";

        String result = instance.renderAsync(Kernel.builder().build(), arguments, null)
            .block();
        Assertions.assertEquals(expResult, result.replaceAll("\\n", ""));
    }

    @Test
    public void testMessageHandler() {
        PromptTemplateConfig promptTemplate = PromptTemplateConfig.builder()
            .withTemplate("{{#message role=\"user\"}}\n" +
                "{{input}}\n" +
                "{{/message}}")
            .withTemplateFormat("handlebars")
            .build();

        HandlebarsPromptTemplate instance = new HandlebarsPromptTemplate(promptTemplate);

        KernelFunctionArguments arguments = KernelFunctionArguments.builder()
            .withVariable("input", "bar\"<>&")
            .build();

        // Return from renderAsync is normalized to remove empty lines and leading/trailing whitespace
        String expResult = "<message role=\"user\">bar&quot;&lt;&gt;&amp;</message>";

        String result = instance.renderAsync(Kernel.builder().build(), arguments, null)
            .block();
        Assertions.assertEquals(expResult, result.replaceAll("\\n", ""));
    }

    @Test
    public void iterableWithContextVariable() {
        PromptTemplateConfig promptTemplate = PromptTemplateConfig.builder()
            .withTemplate(
                "{{#each input}}" +
                    "{{this}}" +
                    "{{/each}}")
            .withTemplateFormat("handlebars")
            .build();

        HandlebarsPromptTemplate instance = new HandlebarsPromptTemplate(promptTemplate);

        KernelFunctionArguments arguments = KernelFunctionArguments.builder()
            .withVariable("input", Arrays.asList(ContextVariable.of("foo\"<>&")))
            .build();

        // Return from renderAsync is normalized to remove empty lines and leading/trailing whitespace
        String expResult = "foo&quot;&lt;&gt;&amp;";

        String result = instance.renderAsync(Kernel.builder().build(), arguments, null)
            .block();
        Assertions.assertEquals(expResult, result.replaceAll("\\n", ""));
    }

    @Test
    public void withCustomConverter() {
        PromptTemplateConfig promptTemplate = PromptTemplateConfig.builder()
            .withTemplate("{{#each input}}{{this}}{{/each}}")
            .withTemplateFormat("handlebars")
            .build();

        HandlebarsPromptTemplate instance = new HandlebarsPromptTemplate(promptTemplate);

        ContextVariableTypeConverter<Foo> converter = ContextVariableTypeConverter.builder(
            Foo.class)
            .toPromptString(Foo::getVal)
            .build();
        KernelFunctionArguments arguments = KernelFunctionArguments.builder()
            .withVariable("input", ContextVariable.of(new Foo("bar\"<>&"), converter))
            .build();

        // Return from renderAsync is normalized to remove empty lines and leading/trailing whitespace
        String expResult = "bar&quot;&lt;&gt;&amp;";

        String result = instance.renderAsync(Kernel.builder().build(), arguments,
            InvocationContext.builder()
                .withContextVariableConverter(converter)
                .build())
            .block();
        Assertions.assertEquals(expResult, result.replaceAll("\\n", ""));
    }
}