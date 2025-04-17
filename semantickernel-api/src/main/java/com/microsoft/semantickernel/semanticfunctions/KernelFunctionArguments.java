// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.semanticfunctions;

import com.microsoft.semantickernel.builders.SemanticKernelBuilder;
import com.microsoft.semantickernel.contextvariables.CaseInsensitiveMap;
import com.microsoft.semantickernel.contextvariables.ContextVariable;
import com.microsoft.semantickernel.contextvariables.ContextVariableType;
import com.microsoft.semantickernel.contextvariables.ContextVariableTypeConverter;
import com.microsoft.semantickernel.contextvariables.ContextVariableTypes;
import com.microsoft.semantickernel.exceptions.SKException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import reactor.util.annotation.NonNull;

/**
 * Arguments to a kernel function.
 *
 * @deprecated Use {@link KernelArguments} instead.
 */
@Deprecated
public class KernelFunctionArguments extends KernelArguments {

    /**
     * Default key for the main input.
     */
    public static final String MAIN_KEY = "input";

    /**
     * Create a new instance of KernelFunctionArguments.
     *
     * @param variables The variables to use for the function invocation.
     */
    protected KernelFunctionArguments(
        @Nullable Map<String, ContextVariable<?>> variables) {
        super(variables, null);
    }

    /**
     * Create a new instance of KernelFunctionArguments.
     *
     * @param content The content to use for the function invocation.
     */
    protected KernelFunctionArguments(@NonNull ContextVariable<?> content) {
        super(content);
    }

    /**
     * Create a new instance of KernelArguments.
     *
     * @param arguments The arguments to copy.
     */
    protected KernelFunctionArguments(@NonNull KernelArguments arguments) {
        super(arguments);
    }

    /**
     * Create a new instance of KernelFunctionArguments.
     */
    protected KernelFunctionArguments() {
        super();
    }

    /**
     * Create a new instance of Builder.
     *
     * @return Builder
     */
    public static Builder builder() {
        return new KernelFunctionArguments.Builder();
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
    public KernelFunctionArguments copy() {
        return new KernelFunctionArguments(variables);
    }

    /**
     * Builder for ContextVariables
     *
     * @deprecated Use {@link KernelArguments} builder instead.
     */
    @Deprecated
    public static class Builder extends KernelArguments.Builder<KernelFunctionArguments> {

        /**
         * Create a new instance of Builder.
         */
        @Deprecated
        public Builder() {
            super(KernelFunctionArguments::new);
        }
    }
}
