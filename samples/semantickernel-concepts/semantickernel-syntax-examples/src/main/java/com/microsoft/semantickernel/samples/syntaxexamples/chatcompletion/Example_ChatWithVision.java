// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.samples.syntaxexamples.chatcompletion;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.KeyCredential;
import com.microsoft.semantickernel.aiservices.openai.chatcompletion.OpenAIChatCompletion;
import com.microsoft.semantickernel.implementation.CollectionUtil;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;
import com.microsoft.semantickernel.services.chatcompletion.message.ChatMessageImageContent;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class Example_ChatWithVision {

    private static final String CLIENT_KEY = System.getenv("CLIENT_KEY");
    private static final String AZURE_CLIENT_KEY = System.getenv("AZURE_CLIENT_KEY");

    // Only required if AZURE_CLIENT_KEY is set
    private static final String CLIENT_ENDPOINT = System.getenv("CLIENT_ENDPOINT");

    // NOTE THAT vision is GPT4 only
    private static final String MODEL_ID = System.getenv()
        .getOrDefault("MODEL_ID", "gpt-4o");

    public static void main(String[] args) throws MalformedURLException {
        System.out.println("======== Open AI - Chat with Vision ========");

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

        ChatCompletionService chatGPT = OpenAIChatCompletion.builder()
            .withModelId(MODEL_ID)
            .withOpenAIAsyncClient(client)
            .build();

        describeUrl(chatGPT);
        describeImage(chatGPT);
    }

    private static void describeImage(ChatCompletionService chatGPT) throws MalformedURLException {

        try (InputStream duke = Example_ChatWithVision.class.getResourceAsStream("duke.png")) {
            byte[] image = duke.readAllBytes();

            ChatHistory chatHistory = new ChatHistory(
                "You look at images and answer questions about them");

            // First user message
            chatHistory.addUserMessage(
                "This image is a cartoon drawing of the Java Duke character riding a dinosaur. What type of dinosaur is it?");
            chatHistory.addMessage(
                ChatMessageImageContent.builder()
                    .withImage("png", image)
                    .build());

            var reply = chatGPT.getChatMessageContentsAsync(chatHistory, null, null);

            String message = reply
                .mapNotNull(CollectionUtil::getLastOrNull)
                .map(ChatMessageContent::getContent)
                .block();

            System.out.println("\n------------------------");
            System.out.print(message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void describeUrl(ChatCompletionService chatGPT) throws MalformedURLException {
        ChatHistory chatHistory = new ChatHistory("You look at images and describe them");

        // First user message
        chatHistory.addUserMessage("Describe the following image");
        chatHistory.addMessage(
            ChatMessageImageContent.builder()
                .withImageUrl(new URL("https://cr.openjdk.org/~jeff/Duke/jpg/Welcome.jpg"))
                .build());

        var reply = chatGPT.getChatMessageContentsAsync(chatHistory, null, null);

        String message = reply
            .mapNotNull(CollectionUtil::getLastOrNull)
            .map(ChatMessageContent::getContent)
            .block();

        System.out.println("\n------------------------");
        System.out.print(message);
    }

}
