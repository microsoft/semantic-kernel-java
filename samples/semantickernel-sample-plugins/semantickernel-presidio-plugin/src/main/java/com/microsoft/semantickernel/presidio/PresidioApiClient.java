// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.presidio;

import com.azure.core.http.HttpClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URL;

public abstract class PresidioApiClient {

    protected final HttpClient client;
    protected final URL endpoint;
    protected final ObjectMapper mapper;

    public PresidioApiClient(
        HttpClient client,
        URL endpoint) {
        this.client = client;
        this.endpoint = endpoint;
        this.mapper = new ObjectMapper();
    }
}
