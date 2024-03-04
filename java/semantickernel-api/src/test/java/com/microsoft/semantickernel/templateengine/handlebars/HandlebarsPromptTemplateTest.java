/*
 * The MIT License
 *
 * Copyright 2024 davidgrieve.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.microsoft.semantickernel.templateengine.handlebars;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import static java.util.stream.Collectors.joining;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.plugin.KernelPlugin;
import com.microsoft.semantickernel.plugin.KernelPluginFactory;
import com.microsoft.semantickernel.semanticfunctions.KernelFunctionArguments;
import com.microsoft.semantickernel.semanticfunctions.PromptTemplateConfig;
import com.microsoft.semantickernel.semanticfunctions.annotations.DefineKernelFunction;
import com.microsoft.semantickernel.semanticfunctions.annotations.KernelFunctionParameter;
import com.microsoft.semantickernel.services.chatcompletion.AuthorRole;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;

/**
 *
 * @author davidgrieve
 */
public class HandlebarsPromptTemplateTest {

    public HandlebarsPromptTemplateTest() {
    }

    public static void main(String[] args) { new HandlebarsPromptTemplateTest().testRenderAsync(); }

    public static class StringFunctions {

        @DefineKernelFunction(name = "upper", description = "Converts a string to upper case.")
        public String upper(
            @KernelFunctionParameter(name = "input", required = true, description = "The string to convert to upper case", type = String.class) 
            String input) {
                return input.toUpperCase();
        }

        @DefineKernelFunction(name = "concat", description = "Concatenate the second string to the first string.")
        public String concat(
            @KernelFunctionParameter(name = "input", required = true, description = "The string to which the second string is concatenated.", type = String.class) 
            String first, 
            @KernelFunctionParameter(name = "suffix", required = true, description = "The string which is concatenated to the first string.", type = String.class) 
            String suffix) {
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
                    new ChatMessageContent<String>(AuthorRole.SYSTEM, "a"),
                    new ChatMessageContent<String>(AuthorRole.USER, "b")
                )
            ), 
            new ChatHistory(
                Arrays.asList(
                    new ChatMessageContent<String>(AuthorRole.SYSTEM, "c"),
                    new ChatMessageContent<String>(AuthorRole.USER, "d")
                )
            )
        );

        KernelPlugin kernelPlugin = KernelPluginFactory.createFromObject(
            new StringFunctions(),
            "string"
        );

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
        String expResult = "CHOICE-A [CHOICE-A, CHOICE-B] <messages> A B </messages> <messages> C D </messages> Hello World";

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
}