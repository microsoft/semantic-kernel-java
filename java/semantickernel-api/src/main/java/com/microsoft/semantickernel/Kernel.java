// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel;

import com.microsoft.semantickernel.builders.Buildable;
import com.microsoft.semantickernel.builders.SemanticKernelBuilder;
import com.microsoft.semantickernel.hooks.KernelHooks;
import com.microsoft.semantickernel.orchestration.FunctionInvocation;
import com.microsoft.semantickernel.orchestration.KernelFunction;
import com.microsoft.semantickernel.plugin.KernelPlugin;
import com.microsoft.semantickernel.services.AIService;
import com.microsoft.semantickernel.services.AIServiceSelection;
import com.microsoft.semantickernel.services.AIServiceSelector;
import com.microsoft.semantickernel.services.AiServiceCollection;
import com.microsoft.semantickernel.services.OrderedAIServiceSelector;
import com.microsoft.semantickernel.services.ServiceNotFoundException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

import javax.annotation.Nullable;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Provides state for use throughout a Semantic Kernel workload.
 * <p>
 * An instance of {@code Kernel} is passed through to every function invocation and service call
 * throughout the system, providing to each the ability to access shared state and services.
 */
public class Kernel implements Buildable {

    private final AIServiceSelector serviceSelector;
    private final KernelPluginCollection plugins;
    private final KernelHooks globalKernelHooks;

    /**
     * Initializes a new instance of {@code Kernel}.
     * @param serviceSelector The {@code AIServiceSelector} used to query for services available through the kernel.
     * @param plugins The collection of plugins available through the kernel. If {@code null}, an empty collection will be used.
     * @param globalKernelHooks The global hooks to be used throughout the kernel. If {@code null}, an empty collection will be used.
     */
    public Kernel(
        AIServiceSelector serviceSelector,
        @Nullable List<KernelPlugin> plugins,
        @Nullable KernelHooks globalKernelHooks) {
        this.serviceSelector = serviceSelector;
        if (plugins != null) {
            this.plugins = new KernelPluginCollection(plugins);
        } else {
            this.plugins = new KernelPluginCollection();
        }

        this.globalKernelHooks = new KernelHooks(globalKernelHooks);
    }

    /**
     * Invokes a {@code KernelFunction} function by name.
     * @param <T> The return type of the function.
     * @param pluginName The name of the plugin containing the function.
     * @param functionName The name of the function to invoke.
     * @return The result of the function invocation.
     * @throws IllegalArgumentException if the plugin or function is not found.
     * @see KernelFunction#invokeAsync(Kernel)
     * @see KernelPluginCollection#getFunction(String, String)
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public <T> FunctionInvocation<T> invokeAsync(
        String pluginName,
        String functionName) {
        KernelFunction function = getFunction(pluginName, functionName);
        return invokeAsync(function);
    }

    /**
     * Invokes a {@code KernelFunction}.
     * @param <T> The return type of the function.
     * @param function The function to invoke.
     * @return The result of the function invocation.
     * @see KernelFunction#invokeAsync(Kernel)
     */
    public <T> FunctionInvocation<T> invokeAsync(KernelFunction<T> function) {
        return function.invokeAsync(this);
    }

    /**
     * This method is used to add plugins to the kernel after it has been created.
     * If a plugin with the same name already exists, it will be replaced.
     * @param plugin The plugin to add.
     * @return {@code this} kernel with the plugin added.
     */
    public Kernel addPlugin(KernelPlugin plugin) {
        plugins.add(plugin);
        return this;
    }

    /**
     * Gets the plugin with the specified name.
     * @param pluginName The name of the plugin to get.
     * @return The plugin with the specified name, or {@code null} if no such plugin exists.
     */    
    @Nullable
    public KernelPlugin getPlugin(String pluginName) {
        return plugins.getPlugin(pluginName);
    }

    /**
     * Gets the plugins that were added to the kernel.
     * @return The plugins available through the kernel (unmodifiable list).
     * @see Kernel#getPlugins()
     */
    public Collection<KernelPlugin> getPlugins() {
        return Collections.unmodifiableCollection(plugins.getPlugins());
    }

    /**
     * Gets the function with the specified name from the plugin with the specified name.
     * @param <T> The return type of the function.
     * @param pluginName The name of the plugin containing the function.
     * @param functionName The name of the function to get.
     * @return The function with the specified name from the plugin with the specified name.
     * @throws IllegalArgumentException if the plugin or function is not found.
     * @see KernelPluginCollection#getFunction(String, String)
     */
    @SuppressWarnings("unchecked")
    public <T> KernelFunction<T> getFunction(String pluginName, String functionName) {
        return (KernelFunction<T>) plugins.getFunction(pluginName, functionName);
    }

    /**
     * Gets the functions available through the kernel. Functions are collected from all plugins
     * available through the kernel. 
     * @return The functions available through the kernel.
     * @see Kernel#getPlugins()
     * @see Kernel.Builder#withPlugin(KernelPlugin)
     */
    public List<KernelFunction<?>> getFunctions() {
        return plugins.getFunctions();
    }

    /**
     * Get the {@code KernelHooks} used throughout the kernel. 
     * These {@code KernelHooks} are used in addition to any hooks provided to a function.
     * @return The {@code KernelHooks} used throughout the kernel.
     * @see KernelFunction#invokeAsync(Kernel, KernelFunctionArguments, ContextVariableType, InvocationContext)
     */
    @SuppressFBWarnings("EI_EXPOSE_REP")
    public KernelHooks getGlobalKernelHooks() {
         return globalKernelHooks;
    }

    /**
     * Get the AIServiceSelector used to query for services available through the kernel.
     * @return The AIServiceSelector used to query for services available through the kernel.
     */
    public AIServiceSelector getServiceSelector() {
        return serviceSelector;
    }

    /**
     * Get the service of the specified type from the kernel.
     * @param <T> The type of the service to get.
     * @param clazz The class of the service to get.
     * @return The service of the specified type from the kernel.
     * @throws ServiceNotFoundException if the service is not found.
     * @see com.microsoft.semantickernel.services.AIServiceSelector#trySelectAIService(Class, KernelFunction, KernelFunctionArguments)
     */
    public <T extends AIService> T getService(Class<T> clazz) throws ServiceNotFoundException {
        AIServiceSelection<T> selector = serviceSelector
            .trySelectAIService(
                clazz,
                null,
                null);

        if (selector == null) {
            throw new ServiceNotFoundException("Unable to find service of type " + clazz.getName());
        }

        return selector.getService();
    }

    /**
     * Get the fluent builder for creating a new instance of {@code Kernel}.
     * @return The fluent builder for creating a new instance of {@code Kernel}.
     */
    public static Kernel.Builder builder() {
        return new Kernel.Builder();
    }


    /**
     * A fluent builder for creating a new instance of {@code Kernel}.
     */
    public static class Builder implements SemanticKernelBuilder<Kernel> {

        private final AiServiceCollection services = new AiServiceCollection();
        private final List<KernelPlugin> plugins = new ArrayList<>();
        @Nullable
        private Function<AiServiceCollection, AIServiceSelector> serviceSelectorProvider;

        /**
         * Adds a service to the kernel.
         * @param <T> The type of the service to add.
         * @param clazz The class of the service to add.
         * @param aiService The service to add.
         * @return {@code this} builder with the service added.
         */
        public <T extends AIService> Builder withAIService(Class<T> clazz, T aiService) {
            services.put(clazz, aiService);
            return this;
        }

        /**
         * Adds a plugin to the kernel.
         * @param plugin The plugin to add.
         * @return {@code this} builder with the plugin added.
         */
        public Kernel.Builder withPlugin(KernelPlugin plugin) {
            plugins.add(plugin);
            return this;
        }

        /**
         * Sets the service selector provider for the kernel.
         * @param serviceSelector The service selector provider for the kernel.
         * @return {@code this} builder with the service selector provider set.
         */
        public Kernel.Builder withServiceSelector(
            Function<AiServiceCollection, AIServiceSelector> serviceSelector) {
            this.serviceSelectorProvider = serviceSelector;
            return this;
        }

        /**
         * Builds a new instance of {@code Kernel} with the services and plugins provided.
         * @return A new instance of {@code Kernel}.
         */
        @Override
        public Kernel build() {

            AIServiceSelector serviceSelector;
            if (serviceSelectorProvider == null) {
                serviceSelector = new OrderedAIServiceSelector(services);
            } else {
                serviceSelector = serviceSelectorProvider.apply(services);
            }

            return new Kernel(
                serviceSelector,
                plugins,
                null);
        }
    }

}
