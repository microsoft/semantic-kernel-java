// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.presidio;

import com.azure.core.http.ContentType;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.microsoft.semantickernel.presidio.models.AnalyzerResult;
import com.microsoft.semantickernel.presidio.models.AnonymizeRequest;
import com.microsoft.semantickernel.presidio.models.AnonymizerResult;
import com.microsoft.semantickernel.presidio.models.anonymizerType.AnonymizerType;
import java.net.URL;
import java.util.List;
import java.util.Map;
import reactor.core.publisher.Mono;

public class PresidioAnonmizerClient extends PresidioApiClient {

    public PresidioAnonmizerClient(
        HttpClient client,
        URL endpoint) {
        super(client, endpoint);
    }

    public Mono<AnonymizerResult> anonymize(String text,
        Map<String, ? extends AnonymizerType> anonymizers,
        List<AnalyzerResult> analyzerResults) {

        return anonymize(new AnonymizeRequest(text, anonymizers, analyzerResults));
    }

    private Mono<AnonymizerResult> anonymize(AnonymizeRequest anonymizeRequest) {

        try {
            HttpRequest request = new HttpRequest(HttpMethod.POST,
                new URL(super.endpoint, "/anonymize"))
                .setHeader(HttpHeaderName.CONTENT_TYPE, ContentType.APPLICATION_JSON)
                .setBody(super.mapper.writeValueAsBytes(anonymizeRequest));

            return super.client.send(request)
                .flatMap(httpResponse -> {
                    if (httpResponse.getStatusCode() >= 400) {
                        return Mono.error(
                            new RuntimeException(
                                "Request failed: " + httpResponse.getStatusCode()));
                    } else {
                        return httpResponse.getBodyAsString();
                    }
                })
                .flatMap(body -> {
                    try {
                        TypeReference<AnonymizerResult> type = new TypeReference<>() {
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
}
