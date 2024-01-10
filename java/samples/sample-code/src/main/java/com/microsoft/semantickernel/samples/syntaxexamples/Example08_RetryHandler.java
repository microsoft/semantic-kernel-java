package com.microsoft.semantickernel.samples.syntaxexamples;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.policy.ExponentialBackoffOptions;
import com.azure.core.http.policy.RetryOptions;
import com.microsoft.semantickernel.DefaultKernel;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.aiservices.openai.textcompletion.OpenAITextGenerationService;
import com.microsoft.semantickernel.exceptions.ConfigurationException;
import com.microsoft.semantickernel.orchestration.KernelFunction;
import com.microsoft.semantickernel.semanticfunctions.KernelFunctionFromPrompt;
import com.microsoft.semantickernel.textcompletion.TextGenerationService;
import java.time.Duration;

public class Example08_RetryHandler {

    public static void main(String[] args) throws ConfigurationException {
        // Create a Kernel with the HttpClient
        RetryOptions retryOptions = new RetryOptions(new ExponentialBackoffOptions()
            .setMaxDelay(Duration.ofSeconds(10))
            .setBaseDelay(Duration.ofSeconds(2))
            .setMaxRetries(3));

        OpenAIAsyncClient client = new OpenAIClientBuilder()
            .retryOptions(retryOptions)
            .endpoint("https://localhost:5000")
            .credential(new AzureKeyCredential("BAD KEY"))
            .buildAsyncClient();

        OpenAITextGenerationService textGenerationService = OpenAITextGenerationService.builder()
            .withOpenAIAsyncClient(client)
            .withModelId("text-davinci-003")
            .build();

        Kernel kernel = new DefaultKernel.Builder()
            .withDefaultAIService(TextGenerationService.class, textGenerationService)
            .build();

        String question = "How popular is the Polly library?";

        KernelFunction fuction = KernelFunctionFromPrompt.create(question);

        try {
            // Will retry 3 times with exponential backoff
            kernel.invokeAsync(fuction, null, String.class).block();
        } catch (Exception e) {
            System.out.println("Hit max retries");
        }
    }
}
