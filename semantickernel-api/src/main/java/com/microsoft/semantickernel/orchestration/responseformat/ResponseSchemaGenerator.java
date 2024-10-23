// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.orchestration.responseformat;

import com.microsoft.semantickernel.exceptions.SKException;
import com.microsoft.semantickernel.implementation.ServiceLoadUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interface for generating json response schemas for a given class.
 */
public interface ResponseSchemaGenerator {

    Logger LOGGER = LoggerFactory.getLogger(ResponseSchemaGenerator.class);

    /**
     * Generate a json schema for the given class.
     *
     * @param clazz The class to generate a schema for.
     * @return The json schema.
     */
    public String generateSchema(Class<?> clazz);

    /**
     * Load a response schema generator based on the Jackson library, requires that
     * com.github.victools:jsonschema-generator has been added to the class path.
     *
     * @return The response schema generator.
     */
    public static ResponseSchemaGenerator jacksonGenerator() {
        try {
            return loadGenerator(
                "com.microsoft.semantickernel.aiservices.openai.chatcompletion.responseformat.JacksonResponseFormatGenerator");
        } catch (NoClassDefFoundError e) {
            LOGGER.error(
                "The Jackson response schema generator relies on the optional dependencies 'com.github.victools:jsonschema-generator', and 'com.github.victools:jsonschema-module-jackson'. To use this feature, please add this dependency to your project.");
            throw new SKException(
                "The Jackson response schema generator relies on the optional dependency 'com.github.victools:jsonschema-generator', and 'com.github.victools:jsonschema-module-jackson'. To use this feature, please add this dependency to your project.");
        }
    }

    /**
     * Load a response schema generator based on the given class name.
     * The class must implement the {@link ResponseSchemaGenerator} interface.
     *
     * @param className The class name of the generator.
     * @return The response schema generator.
     */
    public static ResponseSchemaGenerator loadGenerator(String className) {
        return ServiceLoadUtil
            .findServiceLoader(ResponseSchemaGenerator.class,
                className)
            .get();
    }
}
