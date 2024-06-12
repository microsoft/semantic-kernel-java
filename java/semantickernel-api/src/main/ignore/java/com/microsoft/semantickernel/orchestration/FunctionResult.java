// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.orchestration;

import java.util.Map;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @since 1.0.0
 */
public interface FunctionResult {

    /**
     * Get the name of executed function.
     *
     * @return The name of executed function.
     */
    String functionName();

    /**
     * Get the name of the plugin containing the function.
     *
     * @return The name of the plugin containing the function.
     */
    String pluginName();

    /**
     * Get the metadata for storing additional information about function execution result.
     *
     * @return The metadata for storing additional information about function execution result.
     */
    Map<String, Object> metadata();

    /**
     * Get the function result.
     *
     * @return The function result.
     */
    Mono<String> getValueAsync();

    /**
     * Get the function result.
     *
     * @return The function result.
     */
    Flux<String> getStreamingValueAsync();

    /**
     * Get the function result.
     *
     * @return The function result.
     */
    <T> T getValue();
}
