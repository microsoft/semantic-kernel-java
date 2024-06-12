// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.plugin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.semantickernel.contextvariables.CaseInsensitiveMap;
import com.microsoft.semantickernel.exceptions.SKException;
import com.microsoft.semantickernel.implementation.EmbeddedResourceLoader;
import com.microsoft.semantickernel.implementation.EmbeddedResourceLoader.ResourceLocation;
import com.microsoft.semantickernel.semanticfunctions.InputVariable;
import com.microsoft.semantickernel.semanticfunctions.KernelFunction;
import com.microsoft.semantickernel.semanticfunctions.KernelFunctionFromMethod;
import com.microsoft.semantickernel.semanticfunctions.KernelFunctionFromPrompt;
import com.microsoft.semantickernel.semanticfunctions.KernelPromptTemplateFactory;
import com.microsoft.semantickernel.semanticfunctions.OutputVariable;
import com.microsoft.semantickernel.semanticfunctions.PromptTemplate;
import com.microsoft.semantickernel.semanticfunctions.PromptTemplateConfig;
import com.microsoft.semantickernel.semanticfunctions.PromptTemplateFactory;
import com.microsoft.semantickernel.semanticfunctions.annotations.DefineKernelFunction;
import com.microsoft.semantickernel.semanticfunctions.annotations.KernelFunctionParameter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for creating {@link KernelPlugin} instances. {@code KernelPlugin}s can be created from a
 * Java object, from loading a directory of plugins, or from loading plugins from a resource.
 */
public class KernelPluginFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(KernelPluginFactory.class);
    private static final String CONFIG_FILE = "config.json";
    private static final String PROMPT_FILE = "skprompt.txt";
    private static final CaseInsensitiveMap<Class<?>> PRIMATIVE_CLASS_NAMES = new CaseInsensitiveMap<>();
    private static final CaseInsensitiveMap<Class<?>> COMMON_CLASS_NAMES = new CaseInsensitiveMap<>();
    private static final Map<Class<?>, Class<?>> BOXED_FROM_PRIMATIVE = new HashMap<>();

    static {
        PRIMATIVE_CLASS_NAMES.put("void", void.class);
        PRIMATIVE_CLASS_NAMES.put("int", int.class);
        PRIMATIVE_CLASS_NAMES.put("double", double.class);
        PRIMATIVE_CLASS_NAMES.put("boolean", boolean.class);
        PRIMATIVE_CLASS_NAMES.put("float", float.class);
        PRIMATIVE_CLASS_NAMES.put("long", long.class);
        PRIMATIVE_CLASS_NAMES.put("short", short.class);
        PRIMATIVE_CLASS_NAMES.put("byte", byte.class);
        PRIMATIVE_CLASS_NAMES.put("char", char.class);

        COMMON_CLASS_NAMES.put("integer", int.class);
        COMMON_CLASS_NAMES.put("string", String.class);
        COMMON_CLASS_NAMES.put("list", ArrayList.class);
        COMMON_CLASS_NAMES.put("map", HashMap.class);
        COMMON_CLASS_NAMES.put("set", HashSet.class);

        BOXED_FROM_PRIMATIVE.put(void.class, Void.class);
        BOXED_FROM_PRIMATIVE.put(int.class, Integer.class);
        BOXED_FROM_PRIMATIVE.put(double.class, Double.class);
        BOXED_FROM_PRIMATIVE.put(boolean.class, Boolean.class);
        BOXED_FROM_PRIMATIVE.put(float.class, Float.class);
        BOXED_FROM_PRIMATIVE.put(long.class, Long.class);
        BOXED_FROM_PRIMATIVE.put(short.class, Short.class);
        BOXED_FROM_PRIMATIVE.put(byte.class, Byte.class);
        BOXED_FROM_PRIMATIVE.put(char.class, Character.class);

    }

    /**
     * Creates a plugin that wraps the specified target object. Methods decorated with
     * {@code {@literal @}DefineSKFunction} will be included in the plugin.
     *
     * @param target     The instance of the class to be wrapped.
     * @param pluginName Name of the plugin for function collection and prompt templates. If the
     *                   value is {@code null}, a plugin name is derived from the type of the
     *                   target.
     * @return The new plugin.
     */
    public static KernelPlugin createFromObject(Object target, String pluginName) {
        List<KernelFunction<?>> methods = Arrays.stream(target.getClass().getMethods())
            .filter(method -> method.isAnnotationPresent(DefineKernelFunction.class))
            .map(method -> {
                DefineKernelFunction annotation = method.getAnnotation(DefineKernelFunction.class);
                Class<?> returnType = getReturnType(annotation, method);
                OutputVariable<?> kernelReturnParameterMetadata = new OutputVariable<>(
                    annotation.returnDescription(),
                    returnType);

                KernelFunctionFromMethod.Builder<Object> builder = KernelFunction
                    .createFromMethod(method, target)
                    .withParameters(getParameters(method))
                    .withReturnParameter(kernelReturnParameterMetadata);

                if (pluginName != null && !pluginName.isEmpty()) {
                    builder = builder.withPluginName(pluginName);
                }

                if (annotation.name() != null && !annotation.name().isEmpty()) {
                    builder = builder.withFunctionName(annotation.name());
                }

                if (annotation.description() != null && !annotation.description().isEmpty()) {
                    builder = builder.withDescription(annotation.description());
                }

                return builder.build();

            }).collect(ArrayList::new, (list, it) -> list.add(it), (a, b) -> a.addAll(b));

        return createFromFunctions(pluginName, methods);
    }

    private static Class<?> getReturnType(DefineKernelFunction annotation, Method method) {
        Class<?> returnType = null;
        if (annotation.returnType().isEmpty()) {
            returnType = method.getReturnType();

            if (Publisher.class.isAssignableFrom(returnType)) {
                throw new SKException(
                    "Method: " + method.getDeclaringClass().getName() + "." + method.getName()
                        + ", this is an async method, It is required to add an annotation to specify the return type");
            }
        } else {
            String className = annotation.returnType();
            if (method.getReturnType().getName().equals(className)) {
                // primarily meant to handle void
                return method.getReturnType();
            }

            try {
                returnType = Thread.currentThread().getContextClassLoader()
                    .loadClass(annotation.returnType());
            } catch (ClassNotFoundException e) {
                // ignore
            }

            if (returnType == null) {
                returnType = getCommonTypeAlias(method, className);
            }

            if (returnType == null) {
                throw new SKException("Could not find return type " + annotation.returnType()
                    + "  is not found on method " + method.getDeclaringClass().getName() + "."
                    + method.getName());
            }

            if (!Publisher.class.isAssignableFrom(method.getReturnType())
                && !returnType.isAssignableFrom(method.getReturnType())) {
                throw new SKException(
                    "Return type " + returnType.getName() + " is not assignable from "
                        + method.getReturnType());
            }
        }

        return returnType;
    }

    /**
     * Returns a class found via an inexact match on the class name, i.e does not require the user
     * to provide a fully qualified class name for common types.
     *
     * @param method    The method to get the return type for.
     * @param className The class name to search for.
     * @return The class if found, otherwise null.
     */
    @Nullable
    private static Class<?> getCommonTypeAlias(Method method, String className) {
        Class<?> returnType = PRIMATIVE_CLASS_NAMES.get(className);

        if (returnType == null) {
            returnType = COMMON_CLASS_NAMES.get(className);
        }

        if (returnType != null && Publisher.class.isAssignableFrom(method.getReturnType())) {
            return returnType;
        }

        if (returnType != null && !returnType.isAssignableFrom(method.getReturnType())) {
            returnType = BOXED_FROM_PRIMATIVE.get(returnType);
        }

        return returnType;
    }

    /**
     * Returns the class for the provided type name.
     *
     * @param className The type name.
     * @return The class for the type name.
     */
    public static Class<?> getTypeForName(String className) {
        Class<?> clazz = PRIMATIVE_CLASS_NAMES.get(className);

        if (clazz == null) {
            clazz = COMMON_CLASS_NAMES.get(className);
        }

        if (clazz != null) {
            return clazz;
        }

        try {
            clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
        } catch (ClassNotFoundException e) {
            // ignore
        }

        if (clazz == null) {
            try {
                // Seems that in tests specifically we need to use the class loader of the class itself
                clazz = KernelPluginFactory.class.getClassLoader().loadClass(className);
            } catch (ClassNotFoundException e) {
                // ignore
            }
        }

        if (clazz == null) {
            throw new SKException("Requested type could not be found: " + className
                + ". This needs to be a fully qualified class name, e.g. 'java.lang.String'.");
        }
        return clazz;
    }

    /**
     * Creates a plugin from the provided name and function collection.
     *
     * @param pluginName The name for the plugin.
     * @param functions  The initial functions to be available as part of the plugin.
     * @return The new plugin.
     */
    public static KernelPlugin createFromFunctions(
        String pluginName,
        @Nullable List<KernelFunction<?>> functions) {
        return createFromFunctions(pluginName, null, functions);
    }

    /**
     * Initializes the new plugin from the provided name, description, and function collection.
     *
     * @param pluginName  The name for the plugin.
     * @param description A description of the plugin.
     * @param functions   The initial functions to be available as part of the plugin.
     * @return The new plugin.
     */
    public static KernelPlugin createFromFunctions(String pluginName, @Nullable String description,
        @Nullable List<KernelFunction<?>> functions) {
        Map<String, KernelFunction<?>> funcs = new HashMap<>();
        if (functions != null) {
            funcs = functions.stream().collect(Collectors.toMap(KernelFunction::getName, f -> f));
        }
        return new KernelPlugin(pluginName, description, funcs);
    }

    private static List<InputVariable> getParameters(Method method) {
        return Arrays.stream(method.getParameters())
            .filter(parameter -> parameter.isAnnotationPresent(KernelFunctionParameter.class))
            .map(parameter -> {
                KernelFunctionParameter annotation = parameter.getAnnotation(
                    KernelFunctionParameter.class);

                return InputVariable.build(
                    annotation.name(),
                    annotation.type(),
                    annotation.description(),
                    annotation.defaultValue(),
                    annotation.required());
            }).collect(Collectors.toList());
    }

    /**
     * Imports a plugin from a directory. The directory should contain subdirectories, each of which
     * contains a prompt template and a configuration file. The configuration file should be named
     * "config.json" and the prompt template should be named "skprompt.txt".
     *
     * @param parentDirectory       The parent directory containing the plugin directories.
     * @param pluginDirectoryName   The name of the plugin directory.
     * @param promptTemplateFactory The factory to use for creating prompt templates.
     * @return The imported plugin.
     */
    public static KernelPlugin importPluginFromDirectory(
        Path parentDirectory,
        String pluginDirectoryName,
        @Nullable PromptTemplateFactory promptTemplateFactory) {

        // Verify.ValidSkillName(pluginDirectoryName);
        File pluginDir = new File(parentDirectory.toFile(), pluginDirectoryName);
        // Verify.DirectoryExists(pluginDir);
        if (!pluginDir.exists() || !pluginDir.isDirectory()) {
            throw new SKException("Could not find directory " + pluginDir.getAbsolutePath());
        }
        File[] files = pluginDir.listFiles(File::isDirectory);
        if (files == null) {
            throw new SKException("No Plugins found in directory " + pluginDir.getAbsolutePath());
        }

        Map<String, KernelFunction<?>> plugins = new CaseInsensitiveMap<>();

        for (File dir : files) {
            try {
                // Continue only if prompt template exists
                File promptPath = new File(dir, PROMPT_FILE);
                if (!promptPath.exists()) {
                    continue;
                }

                File configPath = new File(dir, CONFIG_FILE);
                if (!configPath.exists()) {
                    continue;
                    // Verify.NotNull(config, $"Invalid prompt template
                    // configuration, unable to parse {configPath}");
                }

                KernelFunction<?> plugin = getKernelFunction(promptTemplateFactory, configPath,
                    promptPath);

                plugins.put(dir.getName(), plugin);
            } catch (IOException e) {
                LOGGER.error("Failed to read file", e);
            }
        }

        return new KernelPlugin(
            pluginDirectoryName,
            null,
            plugins);
    }

    private static KernelFunction<?> getKernelFunction(
        @Nullable PromptTemplateFactory promptTemplateFactory,
        File configPath,
        File promptPath)
        throws IOException {
        try {
            PromptTemplateConfig config = new ObjectMapper().readValue(configPath,
                PromptTemplateConfig.class);

            // Load prompt template
            String template = new String(Files.readAllBytes(promptPath.toPath()),
                Charset.defaultCharset());

            return getKernelFunction(promptTemplateFactory, config, template);
        } catch (Exception e) {
            LOGGER.error("Failed to read file " + configPath.getAbsolutePath(), e);

            throw new SKException("Failed to read function " + configPath.getAbsolutePath(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> KernelFunction<T> getKernelFunction(
        @Nullable PromptTemplateFactory promptTemplateFactory,
        PromptTemplateConfig config,
        String template) {
        PromptTemplate promptTemplate;

        config = config.copy().withTemplate(template).build();

        if (promptTemplateFactory != null) {
            promptTemplate = promptTemplateFactory.tryCreate(config);
        } else {
            promptTemplate = new KernelPromptTemplateFactory().tryCreate(config);
        }

        return (KernelFunction<T>) new KernelFunctionFromPrompt.Builder<>()
            .withName(config.getName())
            .withDescription(config.getDescription())
            .withExecutionSettings(config.getExecutionSettings())
            .withInputParameters(config.getInputVariables())
            .withPromptTemplate(promptTemplate)
            .withTemplate(template)
            .withTemplateFormat(config.getTemplateFormat())
            .withOutputVariable(config.getOutputVariable())
            .withPromptTemplateFactory(promptTemplateFactory)
            .build();
    }

    /**
     * @param parentDirectory       The parent directory containing the plugin directories.
     * @param pluginDirectoryName   The name of the plugin directory.
     * @param functionName          The name of the function to import.
     * @param promptTemplateFactory The factory to use for creating prompt templates.
     * @return The imported plugin.
     * @see KernelPluginFactory#importPluginFromResourcesDirectory(String, String, String,
     * PromptTemplateFactory, Class)
     */
    @Nullable
    public static KernelPlugin importPluginFromResourcesDirectory(
        String parentDirectory,
        String pluginDirectoryName,
        String functionName,
        @Nullable PromptTemplateFactory promptTemplateFactory) {
        return importPluginFromResourcesDirectory(parentDirectory, pluginDirectoryName,
            functionName,
            promptTemplateFactory, null);
    }

    /**
     * Imports a plugin from a resource directory, which may be on the classpath or filesystem. The
     * directory should contain subdirectories, each of which contains a prompt template and a
     * configuration file. The configuration file should be named "config.json" and the prompt
     * template should be named "skprompt.txt".
     *
     * @param parentDirectory       The parent directory containing the plugin directories.
     * @param pluginDirectoryName   The name of the plugin directory.
     * @param functionName          The name of the function to import.
     * @param promptTemplateFactory The factory to use for creating prompt templates.
     * @param clazz                 The class to use for loading resources. If null, the classloader
     *                              will be used.
     * @return The imported plugin.
     */
    @Nullable
    public static KernelPlugin importPluginFromResourcesDirectory(
        String parentDirectory,
        String pluginDirectoryName,
        String functionName,
        @Nullable PromptTemplateFactory promptTemplateFactory,
        @Nullable Class<?> clazz) {

        String template = getTemplatePrompt(parentDirectory, pluginDirectoryName, functionName,
            clazz);

        PromptTemplateConfig promptTemplateConfig = getPromptTemplateConfig(parentDirectory,
            pluginDirectoryName, functionName, clazz);

        if (promptTemplateConfig == null) {
            LOGGER.warn("Unable to load prompt template config for " + functionName + " in "
                + pluginDirectoryName);
            return null;
        }
        KernelFunction<?> function = getKernelFunction(promptTemplateFactory,
            promptTemplateConfig, template);

        HashMap<String, KernelFunction<?>> plugins = new HashMap<>();

        plugins.put(functionName, function);

        return new KernelPlugin(
            pluginDirectoryName,
            promptTemplateConfig.getDescription(),
            plugins);
    }

    private static String getTemplatePrompt(
        String pluginDirectory,
        String pluginName,
        String functionName,
        @Nullable Class<?> clazz) {
        String promptFileName = pluginDirectory + File.separator + pluginName + File.separator
            + functionName
            + File.separator + PROMPT_FILE;

        try {
            return getFileContents(promptFileName, clazz);
        } catch (IOException e) {
            LOGGER.error("Failed to read file " + promptFileName, e);

            throw new SKException("No plugins found in directory " + promptFileName);
        }
    }

    private static String getFileContents(String file, @Nullable Class<?> clazz)
        throws FileNotFoundException {
        if (clazz == null) {
            return EmbeddedResourceLoader.readFile(file, null, ResourceLocation.CLASSPATH_ROOT,
                ResourceLocation.FILESYSTEM);
        }

        return EmbeddedResourceLoader.readFile(file, clazz, ResourceLocation.CLASSPATH_ROOT,
            ResourceLocation.CLASSPATH, ResourceLocation.FILESYSTEM);
    }

    @Nullable
    private static PromptTemplateConfig getPromptTemplateConfig(
        String pluginDirectory,
        String pluginName, String functionName,
        @Nullable Class<?> clazz) {
        String configFileName = pluginDirectory + File.separator + pluginName + File.separator
            + functionName
            + File.separator + CONFIG_FILE;

        try {
            String config = getFileContents(configFileName, clazz);

            return PromptTemplateConfig.parseFromJson(config);
        } catch (Exception e) {
            if (e instanceof SKException) {
                LOGGER.error("Failed to parse config file " + configFileName, e);

                throw new SKException("Failed to parse config file " + configFileName, e);
            } else {
                LOGGER.debug("No config for " + functionName + " in " + pluginName);
            }
            return null;
        }
    }
}
