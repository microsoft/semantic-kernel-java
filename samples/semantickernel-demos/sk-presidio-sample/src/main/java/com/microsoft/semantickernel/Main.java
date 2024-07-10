// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.KeyCredential;
import com.microsoft.semantickernel.aiservices.openai.chatcompletion.OpenAIChatCompletion;
import com.microsoft.semantickernel.contextvariables.ContextVariableTypes;
import com.microsoft.semantickernel.orchestration.InvocationContext;
import com.microsoft.semantickernel.orchestration.PromptExecutionSettings;
import com.microsoft.semantickernel.plugin.KernelPluginFactory;
import com.microsoft.semantickernel.presidio.AnonymizedText;
import com.microsoft.semantickernel.presidio.AnonymizedTextConverter;
import com.microsoft.semantickernel.presidio.RedactorPlugin;
import com.microsoft.semantickernel.semanticfunctions.KernelFunctionArguments;
import com.microsoft.semantickernel.services.ServiceNotFoundException;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;
import java.util.List;
import reactor.core.publisher.Mono;

public class Main {

    private static final String USE_AZURE_CLIENT = System.getenv("USE_AZURE_CLIENT");

    private static final String CLIENT_KEY = System.getenv("CLIENT_KEY");
    private static final String AZURE_CLIENT_KEY = System.getenv("AZURE_CLIENT_KEY");

    // Only required if AZURE_CLIENT_KEY is set
    private static final String CLIENT_ENDPOINT = System.getenv("CLIENT_ENDPOINT");
    private static final String MODEL_ID = System.getenv()
        .getOrDefault("MODEL_ID", "gpt-4o");

    public static void main(String[] args) throws InterruptedException {

        Kernel kernel = buildKernel();

        String text = """
            The users name is: Steven.
            Steven has account number 012345612.
            Steven was born in New York and their mother is Sally.
            """.stripIndent();

        System.out.println("==============================");
        System.out.println("Input text is: \n" + text);

        AnonymizedText result = kernel
            .invokeAsync("redactor", "redact")
            .withResultType(AnonymizedText.class)
            .withArguments(
                KernelFunctionArguments.builder()
                    .withVariable("input", text)
                    .build())
            .block()
            .getResult();

        System.out.println("==============================");
        System.out.println("Anonymised text is: \n" + result.getRedacted());

        askQuestion(kernel, result, "Question: Where was the user born?").block();
        askQuestion(kernel, result, "Question: Who is the users mother?").block();

    }

    private static Mono<List<ChatMessageContent<?>>> askQuestion(
        Kernel kernel,
        AnonymizedText anonymizedUserInfo,
        String question) {
        ChatHistory chat = formChatHistory();

        chat.addUserMessage(anonymizedUserInfo.getRedacted());
        chat.addUserMessage(question);

        System.out.println("==============================");
        System.out.println("User Question: \n" + question);

        try {
            return kernel
                .getService(ChatCompletionService.class)
                .getChatMessageContentsAsync(chat, kernel,
                    InvocationContext.builder()
                        .withPromptExecutionSettings(
                            PromptExecutionSettings.builder()
                                .withMaxTokens(2048)
                                .withTemperature(0.5)
                                .build())
                        .build())
                .map(chatHistory -> {
                    String message = chatHistory.get(0).getContent();
                    System.out.println("==============================");
                    System.out.println(
                        "Anonymised response: \n" + message);

                    System.out.println("==============================");
                    System.out.println(
                        "Deanonymised response: \n" + anonymizedUserInfo.unredact(message));
                    return chatHistory;
                });

        } catch (ServiceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static ChatHistory formChatHistory() {
        ChatHistory chat = new ChatHistory();

        chat.addSystemMessage(
            """
                You answer questions about the provided information.
                The following is an example of answering a question about a user.

                Information about the user:
                The users name is PERSON100.
                PERSON100 has long hair.

                Question: What does the users hair look like?
                Answer: The user has long hair.
                """.stripIndent());

        return chat;
    }

    private static Kernel buildKernel() {
        OpenAIAsyncClient client;

        if (Boolean.parseBoolean(USE_AZURE_CLIENT)) {
            client = new OpenAIClientBuilder()
                .credential(new AzureKeyCredential(AZURE_CLIENT_KEY))
                .endpoint(CLIENT_ENDPOINT)
                .buildAsyncClient();

        } else {
            client = new OpenAIClientBuilder()
                .credential(new KeyCredential(CLIENT_KEY))
                .buildAsyncClient();
        }

        ChatCompletionService chat = OpenAIChatCompletion.builder()
            .withModelId(MODEL_ID)
            .withOpenAIAsyncClient(client)
            .build();

        ContextVariableTypes.addGlobalConverter(new AnonymizedTextConverter());

        return Kernel
            .builder()
            .withAIService(ChatCompletionService.class, chat)
            .withPlugin(
                KernelPluginFactory.createFromObject(
                    new RedactorPlugin(
                        "http://presidio-analyzer:3000",
                        "http://presidio-anonymizer:3000"),
                    "redactor"))
            .build();

    }

}
