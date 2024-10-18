// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.aiservices.openai;

import com.microsoft.semantickernel.services.AIService;
import javax.annotation.Nullable;

/**
 * Provides OpenAI service.
 * @param <Client> the client type
 */
public abstract class OpenAiService<Client> implements AIService {

    private final Client client;
    @Nullable
    private final String serviceId;
    private final String modelId;
    private final String deploymentName;

    protected OpenAiService(
        Client client,
        @Nullable String serviceId,
        String modelId,
        String deploymentName) {
        this.client = client;
        this.serviceId = serviceId;
        this.modelId = modelId;
        this.deploymentName = deploymentName;
    }

    @Nullable
    @Override
    public String getModelId() {
        return modelId;
    }

    @Override
    @Nullable
    public String getServiceId() {
        return serviceId;
    }

    /**
     * Gets the client.
     * @return the client
     */
    protected Client getClient() {
        return client;
    }

    /**
     * Gets the deployment name.
     * @return the deployment name
     */
    public String getDeploymentName() {
        return deploymentName;
    }
}
