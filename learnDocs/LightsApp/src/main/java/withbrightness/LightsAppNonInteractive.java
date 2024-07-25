package withbrightness;// Copyright (c) Microsoft. All rights reserved.

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.google.gson.Gson;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.aiservices.openai.chatcompletion.OpenAIChatCompletion;
import com.microsoft.semantickernel.contextvariables.ContextVariableTypeConverter;
import com.microsoft.semantickernel.contextvariables.ContextVariableTypes;
import com.microsoft.semantickernel.orchestration.InvocationContext;
import com.microsoft.semantickernel.orchestration.InvocationReturnMode;
import com.microsoft.semantickernel.orchestration.ToolCallBehavior;
import com.microsoft.semantickernel.plugin.KernelPlugin;
import com.microsoft.semantickernel.plugin.KernelPluginFactory;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;

import java.util.List;

public class LightsAppNonInteractive {

    private static final String AZURE_CLIENT_KEY = System.getenv("AZURE_CLIENT_KEY");
    private static final String CLIENT_ENDPOINT = System.getenv("CLIENT_ENDPOINT");
    private static final String MODEL_ID = System.getenv().getOrDefault("MODEL_ID", "gpt-4o");

    public static void main(String[] args) {

        // <LightAppExample>
        OpenAIAsyncClient client = new OpenAIClientBuilder()
                .credential(new AzureKeyCredential(AZURE_CLIENT_KEY))
                .endpoint(CLIENT_ENDPOINT)
                .buildAsyncClient();

        // <importplugin>
        // Import the LightsPlugin
        KernelPlugin lightPlugin = KernelPluginFactory.createFromObject(new LightsPlugin(),
                "LightsPlugin");
        // </importplugin>

        // <createservice>
        // Create your AI service client
        ChatCompletionService chatCompletionService = OpenAIChatCompletion.builder()
                .withModelId(MODEL_ID)
                .withOpenAIAsyncClient(client)
                .build();

        // <buildkernel>
        // Create a kernel with Azure OpenAI chat completion and plugin
        Kernel kernel = Kernel.builder()
                .withAIService(ChatCompletionService.class, chatCompletionService)
                .withPlugin(lightPlugin)
                .build();
        // </buildkernel>
        // </createservice>


        // Add a converter to the kernel to show it how to serialise LightModel objects into a prompt
        ContextVariableTypes
                .addGlobalConverter(
                        ContextVariableTypeConverter.builder(LightModel.class)
                                .toPromptString(new Gson()::toJson)
                                .fromObject(it -> {
                                    if (it instanceof String) {
                                        try {
                                            return new Gson().fromJson((String) it, LightModel.class);
                                        } catch (Exception e) {
                                            throw new RuntimeException("Cannot convert to LightModel");
                                        }
                                    }
                                    return null;
                                })
                                .fromPromptString(string -> {
                                    return new Gson().fromJson(string, LightModel.class);
                                })
                                .build());

        // <invoke>
        // <enableplanning>
        // Enable planning
        InvocationContext invocationContext = new InvocationContext.Builder()
                .withReturnMode(InvocationReturnMode.FULL_HISTORY)
                .withToolCallBehavior(ToolCallBehavior.allowAllKernelFunctions(true))
                .build();
        // </enableplanning>

        // Create a history to store the conversation
        ChatHistory history = new ChatHistory();
        history.addUserMessage("Turn on light 2");

        List<ChatMessageContent<?>> results = chatCompletionService
                .getChatMessageContentsAsync(history, kernel, invocationContext)
                .block();

        System.out.println("Assistant > " + results.get(results.size() - 1));

        history.addUserMessage("Decrease brightness of light 1");

        results = chatCompletionService
                .getChatMessageContentsAsync(history, kernel, invocationContext)
                .block();

        System.out.println("Assistant > " + results.get(results.size() - 1));


        history.addUserMessage("What is the state of all the lights?");

        results = chatCompletionService
                .getChatMessageContentsAsync(history, kernel, invocationContext)
                .block();

        System.out.println("Assistant > " + results.get(results.size() - 1));
        // </invoke>
    }
}
