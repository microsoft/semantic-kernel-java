package com.microsoft.semantickernel.tests;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.models.ChatCompletions;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.json.JsonOptions;
import com.azure.json.JsonWriter;
import com.azure.json.implementation.DefaultJsonReader;
import com.azure.json.implementation.DefaultJsonWriter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.aiservices.openai.chatcompletion.OpenAIChatCompletion;
import com.microsoft.semantickernel.contextvariables.converters.ContextVariableJacksonConverter;
import com.microsoft.semantickernel.implementation.EmbeddedResourceLoader;
import com.microsoft.semantickernel.implementation.EmbeddedResourceLoader.ResourceLocation;
import com.microsoft.semantickernel.orchestration.FunctionResult;
import com.microsoft.semantickernel.orchestration.PromptExecutionSettings;
import com.microsoft.semantickernel.orchestration.responseformat.JsonSchemaResponseFormat;
import com.microsoft.semantickernel.semanticfunctions.HandlebarsPromptTemplateFactory;
import com.microsoft.semantickernel.semanticfunctions.KernelFunctionYaml;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import java.io.IOException;
import java.io.StringWriter;
import org.apache.commons.text.StringEscapeUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;

public class ResponseSchemaTest {

    public static class TestClass {

        private final String name;

        @JsonCreator
        public TestClass(
            @JsonProperty("name") String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    @Test
    public void sendsResponseSchemaFromTemplate() throws IOException {
        OpenAIAsyncClient client = getOpenAIAsyncClient(
            """
                {
                    "name": "Test name"
                }
                """
                .stripIndent());
        Kernel kernel = buildKernel(client);

        var getIntent = KernelFunctionYaml.fromPromptYaml(
            EmbeddedResourceLoader
                .readFile("responseSchema.prompt.yaml", ResponseSchemaTest.class,
                    ResourceLocation.CLASSPATH_ROOT),
            new HandlebarsPromptTemplateFactory());

        FunctionResult<TestClass> response = getIntent.invokeAsync(kernel)
            .withResultTypeAutoConversion(TestClass.class)
            .block();

        verifyCalled(client,
            """
                {
                    "type":"json_schema",
                    "json_schema":{
                        "strict":true,
                        "name":"Test",
                        "schema":{
                            "type" : "object",
                            "properties" : {
                               "name" : {
                                  "type" : "string"
                               }
                            },
                            "required" : [
                               "name"
                            ],
                            "additionalProperties" : false
                        }
                    }
                }
                """
        );
    }

    @Disabled
    @Test
    public void sendsResponseSchema() {
        OpenAIAsyncClient client = getOpenAIAsyncClient(
            """
                {
                    "name": "Test name"
                }
                """
                .stripIndent());

        Kernel kernel = buildKernel(client);

        PromptExecutionSettings promptExecutionSettings = PromptExecutionSettings.builder()
            .withResponseFormat(
                JsonSchemaResponseFormat.builder()
                    .setResponseFormat(TestClass.class)
                    .setName("Test")
                    .build()
            )
            .build();

        FunctionResult<TestClass> response = kernel.invokePromptAsync(
                "Generate TestClass")
            .withTypeConverter(ContextVariableJacksonConverter.create(TestClass.class))
            .withResultType(TestClass.class)
            .withPromptExecutionSettings(promptExecutionSettings)
            .block();

        verifyCalled(client,
            """
                {
                    "type":"json_schema",
                    "json_schema":{
                        "strict":true,
                        "name":"Test",
                        "schema":{
                          "type" : "object",
                          "properties" : {
                            "name" : {
                              "type" : "string"
                            }
                          },
                          "required" : [ "name" ],
                          "additionalProperties" : false
                        }
                    }
                }
                """
        );
    }

    private static void verifyCalled(OpenAIAsyncClient client, String expected) {
        Mockito.verify(client, Mockito.atLeastOnce())
            .getChatCompletionsWithResponse(
                Mockito.any(),
                Mockito.<ChatCompletionsOptions>argThat(
                    (ChatCompletionsOptions chatCompletionsOptions) -> {
                        StringWriter writer = new StringWriter();
                        try {
                            JsonWriter jsonWriter = DefaultJsonWriter.toWriter(
                                writer,
                                new JsonOptions()
                            );
                            JsonWriter format = chatCompletionsOptions.getResponseFormat()
                                    .toJson(jsonWriter);
                            jsonWriter.flush();
                            writer.flush();

                            String json = String.valueOf(writer.getBuffer())
                                .replaceAll("\n", "")
                                .replaceAll("\r", "")
                                .replaceAll(" +", "");
                            String expectedClean = expected
                                .stripIndent()
                                .replaceAll("\n", "")
                                .replaceAll("\r", "")
                                .replaceAll(" +", "");

                            return json.equals(expectedClean);

                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }),
                Mockito.<RequestOptions>any());
    }

    private @NotNull OpenAIAsyncClient getOpenAIAsyncClient(String response) {
        OpenAIAsyncClient client = Mockito.mock(OpenAIAsyncClient.class);

        Mockito.when(
                client.getChatCompletionsWithResponse(
                    Mockito.<String>any(),
                    Mockito.<ChatCompletionsOptions>any(),
                    Mockito.<RequestOptions>any()))
            .thenReturn(
                Mono.just(
                    new Response<ChatCompletions>() {
                        @Override
                        public int getStatusCode() {
                            return 200;
                        }

                        @Override
                        public HttpHeaders getHeaders() {
                            return new HttpHeaders();
                        }

                        @Override
                        public HttpRequest getRequest() {
                            return null;
                        }

                        @Override
                        public ChatCompletions getValue() {
                            return buildResponse(response);
                        }
                    }));
        return client;
    }


    private ChatCompletions buildResponse(String response) {
        String str = String.format("""
            {
               "choices" : [
                  {
                     "content_filter_results" : {
                        "hate" : {
                           "filtered" : false,
                           "severity" : "safe"
                        },
                        "self_harm" : {
                           "filtered" : false,
                           "severity" : "safe"
                        },
                        "sexual" : {
                           "filtered" : false,
                           "severity" : "safe"
                        },
                        "violence" : {
                           "filtered" : false,
                           "severity" : "safe"
                        }
                     },
                     "finish_reason" : "stop",
                     "index" : 0,
                     "message" : {
                        "content" : "%s",
                        "role" : "assistant"
                     }
                  }
               ],
               "created" : 1707253039,
               "id" : "chatcmpl-xxx",
               "prompt_filter_results" : [
                  {
                     "content_filter_results" : {
                        "hate" : {
                           "filtered" : false,
                           "severity" : "safe"
                        },
                        "self_harm" : {
                           "filtered" : false,
                           "severity" : "safe"
                        },
                        "sexual" : {
                           "filtered" : false,
                           "severity" : "safe"
                        },
                        "violence" : {
                           "filtered" : false,
                           "severity" : "safe"
                        }
                     },
                     "prompt_index" : 0
                  }
               ],
               "usage" : {
                  "completion_tokens" : 131,
                  "prompt_tokens" : 26,
                  "total_tokens" : 157
               }
            }
            """, StringEscapeUtils.escapeJson(response));

        try {
            return ChatCompletions.fromJson(
                DefaultJsonReader.fromString(
                    str, new JsonOptions())
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private Kernel buildKernel(OpenAIAsyncClient client) {

        ChatCompletionService openAIChatCompletion = OpenAIChatCompletion.builder()
            .withOpenAIAsyncClient(client)
            .withModelId("a-model")
            .build();

        Kernel kernel = Kernel.builder()
            .withAIService(ChatCompletionService.class, openAIChatCompletion)
            .build();

        return kernel;

    }
}
