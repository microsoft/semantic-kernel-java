// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.aiservices.google;

import com.google.cloud.vertexai.VertexAI;
import com.microsoft.semantickernel.services.AIService;

import javax.annotation.Nullable;


/**
 * Makes a Gemini service available to the Semantic Kernel.
 */
public class GeminiService implements AIService {
    private final VertexAI client;
    private final String modelId;

    /**
     * Creates a new Gemini service.
     * @param client The VertexAI client
     * @param modelId The Gemini model ID
     */
    protected GeminiService(VertexAI client, String modelId) {
        this.client = client;
        this.modelId = modelId;
    }

    @Nullable
    @Override
    public String getModelId() {
        return modelId;
    }

    @Nullable
    @Override
    public String getServiceId() {
        return null;
    }

    /**
     * Gets the VertexAI client.
     * @return  The VertexAI client
     */
    protected VertexAI getClient() {
        return client;
    }
}
