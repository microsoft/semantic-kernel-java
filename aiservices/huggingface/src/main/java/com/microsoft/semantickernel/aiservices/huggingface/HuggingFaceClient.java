// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.aiservices.huggingface;

import com.azure.core.credential.KeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.semantickernel.aiservices.huggingface.models.GeneratedTextItem;
import com.microsoft.semantickernel.aiservices.huggingface.models.TextGenerationRequest;
import com.microsoft.semantickernel.exceptions.SKException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import reactor.core.publisher.Mono;
import javax.annotation.Nullable;

/**
 * A client for the Hugging Face API.
 */
public class HuggingFaceClient {

    private final KeyCredential key;
    private final String endpoint;
    private final HttpClient httpClient;

    /**
     * Creates a new Hugging Face client.
     * @param key The key credential for endpoint authentication.
     * @param endpoint The endpoint for the Hugging Face API.
     * @param httpClient The HTTP client to use for requests.
     */
    public HuggingFaceClient(
        KeyCredential key,
        String endpoint,
        HttpClient httpClient) {
        this.key = key;
        this.endpoint = endpoint;
        this.httpClient = httpClient;
    }

    /*
     * TODO: TGI
     * public Mono<String> getChatMessageContentsAsync(
     * String modelId,
     * ChatCompletionRequest chatCompletionRequest
     * ) {
     * try {
     * String body = new ObjectMapper().writeValueAsString(chatCompletionRequest);
     * return performRequest(modelId, body)
     * .handle((response, sink) -> {
     * ObjectMapper mapper = new ObjectMapper();
     * JavaType type = mapper.getTypeFactory().
     * constructCollectionType(List.class, GeneratedTextItem.class);
     * try {
     * sink.next(mapper.readValue(response, type));
     * } catch (JsonProcessingException e) {
     * sink.error(
     * new SKException("Failed to deserialize response from Hugging Face",
     * e));
     * }
     * });
     * } catch (JsonProcessingException e) {
     * return Mono.error(new SKException("Failed to serialize request body", e));
     * }
     * }
     *
     */

    private static class GeneratedTextItemList {

        private final List<List<GeneratedTextItem>> generatedTextItems;

        @JsonCreator
        public GeneratedTextItemList(
            List<List<GeneratedTextItem>> generatedTextItems) {
            this.generatedTextItems = generatedTextItems;
        }

    }

    /**
     * Gets the text contents from the Hugging Face API.
     * @param modelId The model ID.
     * @param textGenerationRequest The text generation request.
     * @return The generated text items.
     */
    public Mono<List<GeneratedTextItem>> getTextContentsAsync(
        String modelId,
        TextGenerationRequest textGenerationRequest) {
        try {
            String body = new ObjectMapper().writeValueAsString(textGenerationRequest);
            return performRequest(modelId, body)
                .handle((response, sink) -> {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        JavaType type = mapper.getTypeFactory().constructCollectionType(List.class,
                            GeneratedTextItemList.class);
                        GeneratedTextItemList data = mapper.readValue(response,
                            GeneratedTextItemList.class);
                        sink.next(data.generatedTextItems.get(0));
                    } catch (Exception e) {
                        sink.error(
                            new SKException("Failed to deserialize response from Hugging Face",
                                e));
                    }
                });
        } catch (JsonProcessingException e) {
            return Mono.error(new SKException("Failed to serialize request body", e));
        }
    }

    private Mono<String> performRequest(String modelId,
        String body) {
        HttpRequest request = new HttpRequest(HttpMethod.POST, endpoint)
            .setHeader(HttpHeaderName.AUTHORIZATION, "Bearer " + key.getKey())
            .setHeader(HttpHeaderName.CONTENT_TYPE, "application/json")
            .setHeader(HttpHeaderName.fromString("azureml-model-deployment"), modelId);

        request.setBody(body.getBytes(StandardCharsets.UTF_8));

        Mono<String> responseBody = httpClient
            .send(request)
            .onErrorResume(
                e -> {
                    return Mono.error(
                        new SKException("Failed to send request to Hugging Face", e));
                })
            .flatMap(httpResponse -> {
                if (httpResponse.getStatusCode() >= 400) {
                    return httpResponse.getBodyAsString()
                        .flatMap(errorBody -> {
                            return Mono.error(new SKException(
                                "Failed to get text content from Hugging Face. Status code: "
                                    + httpResponse.getStatusCode() + " " + errorBody));
                        });
                } else {
                    return Mono.just(httpResponse);
                }
            })
            .flatMap(HttpResponse::getBodyAsString);
        return responseBody;
    }

    /**
     * Creates a new builder for a Hugging Face client.
     * @return The builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for a Hugging Face client.
     */
     public static class Builder {

        @Nullable
        private KeyCredential key = null;
        @Nullable
        private String endpoint = null;
        @Nullable
        private HttpClient httpClient = null;

        /**
         * Builds the Hugging Face client.
         * @return The client
         */
        public HuggingFaceClient build() {
            if (httpClient == null) {
                httpClient = HttpClient.createDefault();
            }
            if (key == null) {
                throw new SKException("Key credential is required");
            }
            if (endpoint == null) {
                throw new SKException("Endpoint is required");
            }
            return new HuggingFaceClient(
                key,
                endpoint,
                httpClient);
        }

        /**
         * Sets the key credential for the client.
         * @param key The key credential
         * @return The builder
         */
        public Builder credential(KeyCredential key) {
            this.key = key;
            return this;
        }

        /**
         * Sets the endpoint for the client.
         * @param endpoint The endpoint
         * @return The builder
         */
        public Builder endpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        /**
         * Sets the HTTP client for the client.
         * @param httpClient The HTTP client
         * @return The builder
         */
        public Builder httpClient(HttpClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }
    }
}
