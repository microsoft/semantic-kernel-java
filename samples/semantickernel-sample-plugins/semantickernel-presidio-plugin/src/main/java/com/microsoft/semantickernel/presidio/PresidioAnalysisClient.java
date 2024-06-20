// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.presidio;

import com.azure.core.http.ContentType;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.microsoft.semantickernel.presidio.models.AnalyzerRequest;
import com.microsoft.semantickernel.presidio.models.AnalyzerResult;
import java.net.URL;
import java.util.List;
import reactor.core.publisher.Mono;

public class PresidioAnalysisClient extends PresidioApiClient {

    public PresidioAnalysisClient(
        HttpClient client,
        URL endpoint) {
        super(client, endpoint);
    }

    public Mono<List<AnalyzerResult>> analyze(AnalyzerRequest analyzerRequest) {
        try {
            HttpRequest request = new HttpRequest(HttpMethod.POST,
                new URL(super.endpoint, "/analyze"))
                .setHeader(HttpHeaderName.CONTENT_TYPE, ContentType.APPLICATION_JSON)
                .setBody(super.mapper.writeValueAsBytes(analyzerRequest));

            return super.client.send(request)
                .flatMap(httpResponse -> {
                    if (httpResponse.getStatusCode() != 200) {
                        return Mono.error(
                            new RuntimeException(
                                "Request failed: " + httpResponse.getStatusCode()));
                    } else {
                        return httpResponse.getBodyAsString();
                    }
                })
                .flatMap(body -> {
                    try {
                        TypeReference<List<AnalyzerResult>> type = new TypeReference<>() {
                        };

                        return Mono.just(mapper.readValue(body, type));
                    } catch (JsonProcessingException e) {
                        return Mono.error(
                            new RuntimeException("Failed to parse response", e));
                    }
                });
        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    public Mono<List<AnalyzerResult>> analyze(String text, String language) {
        return analyze(new AnalyzerRequest(text, language));
    }

}
