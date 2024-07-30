// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.services.openai;

import com.microsoft.semantickernel.services.AIService;
import com.microsoft.semantickernel.builders.SemanticKernelBuilder;
import javax.annotation.Nullable;

/**
 * Builder for an OpenAI service.
 * @param <C> The client type
 * @param <T> The service type
 * @param <U> The builder type
*/
public abstract class OpenAiServiceBuilder<C, T extends AIService, U extends OpenAiServiceBuilder<C, T, U>> implements
 
    SemanticKernelBuilder<T> {

    @Nullable
    protected String modelId;
    @Nullable
    protected C client;
    @Nullable
    protected String serviceId;
    @Nullable
    protected String deploymentName;

    /**
     * Sets the model ID for the service.
     * <p>
     * If no deployment name is provided, it will be assumed that this model ID is also the
     * deployment name.
     *
     * @param modelId The model ID
     * @return The builder
     */
    public U withModelId(String modelId) {
        this.modelId = modelId;
        return (U) this;
    }

    /**
     * Sets the deployment name for the service if required.
     *
     * @param deploymentName The deployment name
     * @return The builder
     */
    public U withDeploymentName(String deploymentName) {
        this.deploymentName = deploymentName;
        return (U) this;
    }

    /**
     * Sets the OpenAI client for the service
     *
     * @param client The OpenAI client
     * @return The builder
     */
    public U withOpenAIAsyncClient(C client) {
        this.client = client;
        return (U) this;
    }

    /**
     * Sets the service ID for the service
     *
     * @param serviceId The service ID
     * @return The builder
     */
    public U withServiceId(String serviceId) {
        this.serviceId = serviceId;
        return (U) this;
    }

    /**
     * Builds the service.
     * @return The service
     */
    @Override
    public abstract T build();

}