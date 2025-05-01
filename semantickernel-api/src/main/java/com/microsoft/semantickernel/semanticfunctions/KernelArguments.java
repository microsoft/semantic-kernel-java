// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.semanticfunctions;

import com.microsoft.semantickernel.builders.SemanticKernelBuilder;
import com.microsoft.semantickernel.contextvariables.CaseInsensitiveMap;
import com.microsoft.semantickernel.contextvariables.ContextVariable;
import com.microsoft.semantickernel.contextvariables.ContextVariableType;
import com.microsoft.semantickernel.contextvariables.ContextVariableTypeConverter;
import com.microsoft.semantickernel.contextvariables.ContextVariableTypes;
import com.microsoft.semantickernel.exceptions.SKException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.microsoft.semantickernel.orchestration.PromptExecutionSettings;
import reactor.util.annotation.NonNull;

/**
 * Arguments to a kernel function.
 */
public class KernelArguments implements Map<String, ContextVariable<?>> {

    /**
     * Default key for the main input.
     */
    public static final String MAIN_KEY = "input";

    protected final CaseInsensitiveMap<ContextVariable<?>> variables;
    protected final Map<String, PromptExecutionSettings> executionSettings;

    /**
     * Create a new instance of KernelArguments.
     *
     * @param variables The variables to use for the function invocation.
     */
    protected KernelArguments(
        @Nullable Map<String, ContextVariable<?>> variables,
        @Nullable Map<String, PromptExecutionSettings> executionSettings) {
        if (variables == null) {
            this.variables = new CaseInsensitiveMap<>();
        } else {
            this.variables = new CaseInsensitiveMap<>(variables);
        }

        if (executionSettings == null) {
            this.executionSettings = new HashMap<>();
        } else {
            this.executionSettings = new HashMap<>(executionSettings);
        }
    }

    /**
     * Create a new instance of KernelArguments.
     *
     * @param content The content to use for the function invocation.
     */
    protected KernelArguments(@NonNull ContextVariable<?> content) {
        this();
        this.variables.put(MAIN_KEY, content);
    }

    /**
     * Create a new instance of KernelArguments.
     */
    protected KernelArguments() {
        this.variables = new CaseInsensitiveMap<>();
        this.executionSettings = new HashMap<>();
    }

    /**
     * Create a new instance of KernelArguments.
     *
     * @param arguments The arguments to copy.
     */
    protected KernelArguments(@NonNull KernelArguments arguments) {
        this.variables = new CaseInsensitiveMap<>(arguments.variables);
        this.executionSettings = new HashMap<>(arguments.executionSettings);
    }

    /**
     * Get the prompt execution settings
     *
     * @return prompt execution settings
     */
    @Nonnull
    public Map<String, PromptExecutionSettings> getExecutionSettings() {
        return Collections.unmodifiableMap(executionSettings);
    }

    /**
     * Get the input (entry in the MAIN_KEY slot)
     *
     * @return input
     */
    @Nullable
    public ContextVariable<?> getInput() {
        return get(MAIN_KEY);
    }

    /**
     * Create formatted string of the variables
     *
     * @return formatted string
     */
    public String prettyPrint() {
        return variables.entrySet().stream()
            .reduce(
                "",
                (str, entry) -> str
                    + System.lineSeparator()
                    + entry.getKey()
                    + ": "
                    + entry.getValue().toPromptString(ContextVariableTypes.getGlobalTypes()),
                (a, b) -> a + b);
    }

    /**
     * Return the variable with the given name
     *
     * @param key variable name
     * @return content of the variable
     */
    @Nullable
    public ContextVariable<?> get(String key) {
        return variables.get(key);
    }

    /**
     * Return the variable with the given name
     *
     * @param key variable name
     * @return content of the variable
     */
    @Nullable
    <T> ContextVariable<T> get(String key, Class<T> clazz) {
        ContextVariable<?> value = variables.get(key);
        if (value == null) {
            return null;
        } else if (clazz.isAssignableFrom(value.getType().getClazz())) {
            return (ContextVariable<T>) value;
        }

        throw new SKException(
            String.format(
                "Variable %s is of type %s, but requested type is %s",
                key, value.getType().getClazz(), clazz));
    }

    /**
     * Return whether the variable with the given name is {@code null} or empty.
     *
     * @param key the key for the variable
     * @return {@code true} if the variable is {@code null} or empty, {@code false} otherwise
     */
    public boolean isNullOrEmpty(String key) {
        return get(key) == null || get(key).isEmpty();
    }

    @Override
    public int size() {
        return variables.size();
    }

    @Override
    public boolean isEmpty() {
        return variables.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return variables.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return variables.containsValue(value);
    }

    @Override
    @Nullable
    public ContextVariable<?> get(Object key) {
        return variables.get(key);
    }

    @Override
    public ContextVariable<?> put(String key, ContextVariable<?> value) {
        return variables.put(key, value);
    }

    @Override
    public ContextVariable<?> remove(Object key) {
        return variables.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends ContextVariable<?>> m) {
        variables.putAll(m);
    }

    @Override
    public void clear() {
        variables.clear();
    }

    @Override
    public Set<String> keySet() {
        return variables.keySet();
    }

    @Override
    public Collection<ContextVariable<?>> values() {
        return variables.values();
    }

    @Override
    public Set<Entry<String, ContextVariable<?>>> entrySet() {
        return variables.entrySet();
    }

    /**
     * Create a copy of the current instance
     *
     * @return copy of the current instance
     */
    public KernelArguments copy() {
        return new KernelArguments(variables, executionSettings);
    }

    /**
     * Create a new instance of Builder.
     *
     * @return Builder
     */
    public static Builder<?> builder() {
        return new Builder<>(KernelArguments::new);
    }

    /**
     * Builder for ContextVariables
     */
    public static class Builder<U extends KernelArguments> implements SemanticKernelBuilder<U> {

        private final Function<KernelArguments, U> constructor;
        private final Map<String, ContextVariable<?>> variables;
        private final Map<String, PromptExecutionSettings> executionSettings;

        protected Builder(Function<KernelArguments, U> constructor) {
            this.constructor = constructor;
            this.variables = new HashMap<>();
            this.executionSettings = new HashMap<>();
        }

        /**
         * Builds an instance with the given content in the default main key
         *
         * @param content Entry to place in the "input" slot
         * @param <T>     Type of the value
         * @return {$code this} Builder for fluent coding
         */
        public <T> Builder<U> withInput(ContextVariable<T> content) {
            return withVariable(MAIN_KEY, content);
        }

        /**
         * Builds an instance with the given content in the default main key
         *
         * @param content Entry to place in the "input" slot
         * @return {$code this} Builder for fluent coding
         * @throws SKException if the content cannot be converted to a ContextVariable
         */
        public Builder<U> withInput(Object content) {
            return withInput(ContextVariable.ofGlobalType(content));
        }

        /**
         * Builds an instance with the given content in the default main key
         *
         * @param content       Entry to place in the "input" slot
         * @param typeConverter Type converter for the content
         * @param <T>           Type of the value
         * @return {$code this} Builder for fluent coding
         * @throws SKException if the content cannot be converted to a ContextVariable
         */
        public <T> Builder<U> withInput(T content, ContextVariableTypeConverter<T> typeConverter) {
            return withInput(new ContextVariable<>(
                new ContextVariableType<>(
                    typeConverter,
                    typeConverter.getType()),
                content));
        }

        /**
         * Builds an instance with the given variables
         *
         * @param map Existing variables
         * @return {$code this} Builder for fluent coding
         */
        public Builder<U> withVariables(@Nullable Map<String, ContextVariable<?>> map) {
            if (map == null) {
                return this;
            }
            variables.putAll(map);
            return this;
        }

        /**
         * Set variable
         *
         * @param key   variable name
         * @param value variable value
         * @param <T>   Type of the value
         * @return {$code this} Builder for fluent coding
         */
        public <T> Builder<U> withVariable(String key, ContextVariable<T> value) {
            variables.put(key, value);
            return this;
        }

        /**
         * Set variable, uses the default type converters
         *
         * @param key   variable name
         * @param value variable value
         * @return {$code this} Builder for fluent coding
         * @throws SKException if the value cannot be converted to a ContextVariable
         */
        public Builder<U> withVariable(String key, Object value) {
            if (value instanceof ContextVariable) {
                return withVariable(key, (ContextVariable<?>) value);
            }
            return withVariable(key, ContextVariable.ofGlobalType(value));
        }

        /**
         * Set variable
         *
         * @param key           variable name
         * @param value         variable value
         * @param typeConverter Type converter for the value
         * @param <T>           Type of the value
         * @return {$code this} Builder for fluent coding
         * @throws SKException if the value cannot be converted to a ContextVariable
         */
        public <T> Builder<U> withVariable(String key, T value,
            ContextVariableTypeConverter<T> typeConverter) {
            return withVariable(key, new ContextVariable<>(
                new ContextVariableType<>(
                    typeConverter,
                    typeConverter.getType()),
                value));
        }

        /**
         * Set prompt execution settings
         *
         *  @param executionSettings Execution settings
         *  @return {$code this} Builder for fluent coding
         */
        public Builder<U> withExecutionSettings(PromptExecutionSettings executionSettings) {
            return withExecutionSettings(Collections.singletonList(executionSettings));
        }

        /**
         * Set prompt execution settings
         *
         *  @param executionSettings Execution settings
         *  @return {$code this} Builder for fluent coding
         */
        public Builder<U> withExecutionSettings(
            Map<String, PromptExecutionSettings> executionSettings) {
            if (executionSettings == null) {
                return this;
            }

            this.executionSettings.putAll(executionSettings);
            return this;
        }

        /**
         * Set prompt execution settings
         *
         * @param executionSettings Execution settings
         * @return {$code this} Builder for fluent coding
         */
        public Builder<U> withExecutionSettings(List<PromptExecutionSettings> executionSettings) {
            if (executionSettings == null) {
                return this;
            }

            for (PromptExecutionSettings settings : executionSettings) {
                String serviceId = settings.getServiceId();

                if (this.executionSettings.containsKey(serviceId)) {
                    if (serviceId.equals(PromptExecutionSettings.DEFAULT_SERVICE_ID)) {
                        throw new SKException(
                            String.format(
                                "Multiple prompt execution settings with the default service id '%s' or no service id have been provided. Specify a single default prompt execution settings and provide a unique service id for all other instances.",
                                PromptExecutionSettings.DEFAULT_SERVICE_ID));
                    }

                    throw new SKException(
                        String.format(
                            "Multiple prompt execution settings with the service id '%s' have been provided. Specify a unique service id for all instances.",
                            serviceId));
                }

                this.executionSettings.put(serviceId, settings);
            }

            return this;
        }

        @Override
        public U build() {
            KernelArguments arguments = new KernelArguments(variables, executionSettings);
            return constructor.apply(arguments);
        }
    }
}
