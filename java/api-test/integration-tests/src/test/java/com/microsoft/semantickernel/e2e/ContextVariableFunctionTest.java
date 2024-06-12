// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.e2e;

import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.semanticfunctions.PromptTemplateConfig;
import com.microsoft.semantickernel.textcompletion.CompletionSKContext;
import com.microsoft.semantickernel.textcompletion.CompletionSKFunction;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

public class ContextVariableFunctionTest extends AbstractKernelTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContextVariableFunctionTest.class);

    public static void main(String[] args)
            throws IOException, ExecutionException, InterruptedException, TimeoutException {
        new ContextVariableFunctionTest().runContextVariableTest();
    }

    @Test
    @EnabledIf("isAzureTestEnabled")
    public void runContextVariableTest()
            throws IOException, ExecutionException, InterruptedException, TimeoutException {
        Kernel kernel = buildTextCompletionKernel();

        String prompt =
                """
                        ChatBot can have a conversation with you about any topic.
                        It can give explicit instructions or say 'I don't know' if it does not have an answer.

                        {{$history}}
                        User: {{$user_input}}
                        ChatBot:\s""";

        CompletionSKFunction chat =
                kernel.getSemanticFunctionBuilder()
                        .createFunction(
                                prompt,
                                "ChatBot",
                                null,
                                null,
                                new PromptTemplateConfig.CompletionConfig(
                                        0.7, 0.5, 0, 0, 2000, new ArrayList<>()));

        CompletionSKContext readOnlySkContext = chat.buildContext();

        chat("Hi, I'm looking for book suggestions?", chat, readOnlySkContext)
                .flatMap(
                        chat(
                                "I love history and philosophy, I'd like to learn something new"
                                        + " about Greece, any suggestion?",
                                chat))
                .flatMap(chat("that sounds interesting, what is it about?", chat))
                .flatMap(
                        chat(
                                "if I read that book, what exactly will I learn about Greece"
                                        + " history?",
                                chat))
                .flatMap(
                        chat("could you list some more books I could read about this topic?", chat))
                .block();
    }

    private Function<CompletionSKContext, Mono<CompletionSKContext>> chat(
            String input, CompletionSKFunction chat) {
        return (context) -> {
            try {
                return chat(input, chat, context);
            } catch (ExecutionException | InterruptedException | TimeoutException e) {
                return Mono.error(e);
            }
        };
    }

    private Mono<CompletionSKContext> chat(
            String input, CompletionSKFunction chat, CompletionSKContext context)
            throws ExecutionException, InterruptedException, TimeoutException {
        context = context.setVariable("user_input", input);

        LOGGER.info("User:\n" + input);

        CompletionSKContext finalContext = context;
        return chat.invokeAsync(context, null)
                .map(
                        result -> {
                            LOGGER.info("Bot:\n\t\t" + result.getResult());

                            String existingHistoy = finalContext.getVariables().get("history");
                            if (existingHistoy == null) {
                                existingHistoy = "";
                            }
                            existingHistoy +=
                                    "\nUser: " + input + "\nChatBot: " + result.getResult() + "\n";
                            return finalContext.setVariable("history", existingHistoy);
                        });
    }
}
