// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.aiservices.voyageai.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.microsoft.semantickernel.exceptions.AIException;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * HTTP client for VoyageAI API.
 */
public class VoyageAIClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(VoyageAIClient.class);
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final String DEFAULT_ENDPOINT = "https://api.voyageai.com/v1";

    private final OkHttpClient httpClient;
    private final String apiKey;
    private final String endpoint;
    private final ObjectMapper objectMapper;

    /**
     * Creates a new VoyageAI client.
     *
     * @param apiKey   VoyageAI API key
     * @param endpoint Optional API endpoint (defaults to https://api.voyageai.com/v1)
     * @param httpClient Optional HTTP client
     */
    public VoyageAIClient(
        String apiKey,
        @Nullable String endpoint,
        @Nullable OkHttpClient httpClient) {

        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("API key cannot be null or empty");
        }

        this.apiKey = apiKey;
        this.endpoint = endpoint != null ? endpoint : DEFAULT_ENDPOINT;
        this.httpClient = httpClient != null ? httpClient : createDefaultHttpClient();
        this.objectMapper = createObjectMapper();
    }

    /**
     * Creates a new VoyageAI client with default HTTP client and endpoint.
     *
     * @param apiKey VoyageAI API key
     */
    public VoyageAIClient(String apiKey) {
        this(apiKey, null, null);
    }

    private static OkHttpClient createDefaultHttpClient() {
        return new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();
    }

    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        return mapper;
    }

    /**
     * Sends a request to the VoyageAI API.
     *
     * @param path         API path (e.g., "embeddings", "rerank")
     * @param requestBody  Request body object
     * @param responseType Response type class
     * @param <T>          Response type
     * @return Mono containing the response
     */
    public <T> Mono<T> sendRequestAsync(
        String path,
        Object requestBody,
        Class<T> responseType) {

        return Mono.fromCallable(() -> {
            String requestUri = endpoint + "/" + path;

            LOGGER.debug("Sending VoyageAI request to {}", requestUri);

            String json = objectMapper.writeValueAsString(requestBody);
            LOGGER.trace("Request body: {}", json);

            RequestBody body = RequestBody.create(json, JSON);

            Request request = new Request.Builder()
                .url(requestUri)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Accept", "application/json")
                .post(body)
                .build();

            try (Response response = httpClient.newCall(request).execute()) {
                String responseBody = response.body() != null ? response.body().string() : "";

                if (!response.isSuccessful()) {
                    LOGGER.error("VoyageAI API request failed with status {}: {}",
                        response.code(), responseBody);
                    throw new AIException(AIException.ErrorCodes.SERVICE_ERROR,
                        String.format("VoyageAI API request failed with status %d: %s",
                            response.code(), responseBody));
                }

                LOGGER.trace("Response body: {}", responseBody);

                T result = objectMapper.readValue(responseBody, responseType);
                if (result == null) {
                    throw new AIException(AIException.ErrorCodes.SERVICE_ERROR,
                        "Failed to deserialize VoyageAI response: " + responseBody);
                }

                return result;
            }
        });
    }
}
