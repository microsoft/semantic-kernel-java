package com.microsoft.semantickernel.samples.syntaxexamples.agents;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.KeyCredential;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.agents.AgentInvokeOptions;
import com.microsoft.semantickernel.agents.chatcompletion.ChatCompletionAgent;
import com.microsoft.semantickernel.agents.chatcompletion.ChatHistoryAgentThread;
import com.microsoft.semantickernel.aiservices.openai.chatcompletion.OpenAIChatCompletion;
import com.microsoft.semantickernel.contextvariables.ContextVariableTypeConverter;
import com.microsoft.semantickernel.contextvariables.ContextVariableTypes;
import com.microsoft.semantickernel.implementation.templateengine.tokenizer.DefaultPromptTemplate;
import com.microsoft.semantickernel.orchestration.InvocationContext;
import com.microsoft.semantickernel.orchestration.PromptExecutionSettings;
import com.microsoft.semantickernel.orchestration.ToolCallBehavior;
import com.microsoft.semantickernel.plugin.KernelPluginFactory;
import com.microsoft.semantickernel.samples.plugins.github.GitHubModel;
import com.microsoft.semantickernel.samples.plugins.github.GitHubPlugin;
import com.microsoft.semantickernel.semanticfunctions.KernelArguments;
import com.microsoft.semantickernel.semanticfunctions.PromptTemplateConfig;
import com.microsoft.semantickernel.services.chatcompletion.AuthorRole;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;

import java.util.List;
import java.util.Scanner;

public class CompletionAgent {
    private static final String CLIENT_KEY = System.getenv("CLIENT_KEY");
    private static final String AZURE_CLIENT_KEY = System.getenv("AZURE_CLIENT_KEY");

    // Only required if AZURE_CLIENT_KEY is set
    private static final String CLIENT_ENDPOINT = System.getenv("CLIENT_ENDPOINT");
    private static final String MODEL_ID = System.getenv()
            .getOrDefault("MODEL_ID", "gpt-4o");

    private static final String GITHUB_PAT = System.getenv("GITHUB_PAT");
    public static void main(String[] args) {
        System.out.println("======== ChatCompletion Agent ========");

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

        System.out.println("------------------------");

        ChatCompletionService chatCompletion = OpenAIChatCompletion.builder()
                .withModelId(MODEL_ID)
                .withOpenAIAsyncClient(client)
                .build();

        Kernel kernel = Kernel.builder()
                .withAIService(ChatCompletionService.class, chatCompletion)
                .withPlugin(KernelPluginFactory.createFromObject(new GitHubPlugin(GITHUB_PAT),
                        "GitHubPlugin"))
                .build();

        InvocationContext invocationContext = InvocationContext.builder()
                .withToolCallBehavior(ToolCallBehavior.allowAllKernelFunctions(true))
                .withContextVariableConverter(new ContextVariableTypeConverter<>(
                        GitHubModel.Issue.class,
                        o -> (GitHubModel.Issue) o,
                        o -> o.toString(),
                        s -> null
                ))
                .build();

        ChatCompletionAgent agent = ChatCompletionAgent.builder()
                .withKernel(kernel)
                .withKernelArguments(
                    KernelArguments.builder()
                        .withVariable("repository", "microsoft/semantic-kernel-java")
                        .withExecutionSettings(PromptExecutionSettings.builder()
                                .build())
                        .build()
                )
                .withInvocationContext(invocationContext)
                .withTemplate(
                    DefaultPromptTemplate.build(
                        PromptTemplateConfig.builder()
                            .withTemplate(
                                """
                                You are an agent designed to query and retrieve information from a single GitHub repository in a read-only manner.
                                You are also able to access the profile of the active user.
                
                                Use the current date and time to provide up-to-date details or time-sensitive responses.
                
                                The repository you are querying is a public repository with the following name: {{$repository}}
                
                                The current date and time is: {{$now}}.
                                """
                            )
                            .build()
                    )
                ).build();

        ChatHistoryAgentThread agentThread = new ChatHistoryAgentThread();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("> ");

            String input = scanner.nextLine();
            if (input.equalsIgnoreCase("exit")) {
                break;
            }

            var message = new ChatMessageContent<>(AuthorRole.USER, input);
            KernelArguments arguments = KernelArguments.builder()
                    .withVariable("now", System.currentTimeMillis())
                    .build();

            var response = agent.invokeAsync(
                    List.of(message),
                    agentThread,
                    AgentInvokeOptions.builder()
                            .withKernel(kernel)
                            .withKernelArguments(arguments)
                            .build()
            ).block();

            var lastResponse = response.get(response.size() - 1);

            System.out.println("> " + lastResponse.getMessage());
            agentThread = (ChatHistoryAgentThread) lastResponse.getThread();
        }
    }
}
