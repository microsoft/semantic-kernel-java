// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.builders;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.chatcompletion.AzureOpenAIChatCompletion;
import com.microsoft.semantickernel.chatcompletion.OpenAIChatCompletion;
import com.microsoft.semantickernel.orchestration.KernelFunctionYaml;
import com.microsoft.semantickernel.orchestration.contextvariables.KernelArguments;

/**
 * Enum singleton that service loads builder implementations
 */
@SuppressWarnings("ImmutableEnumChecker")
public enum BuildersSingleton {
    INST;

    // Fallback classes in case the META-INF/services directory is missing
    // Keep this list in alphabetical order by fallback variable name

    private static final String FALLBACK_OPENAI_CHAT_COMPLETION_BUILDER_CLASS = 
        "com.microsoft.semantickernel.aiservices.openai.chatcompletion.OpenAIChatCompletion$Builder";
    private static final String FALLBACK_AZURE_OPENAI_CHAT_COMPLETION_BUILDER_CLASS = 
        "com.microsoft.semantickernel.aiservices.azureopenai.chatcompletion.AzureOpenAIChatCompletion$Builder";
    private static final String FALLBACK_COMPLETION_CONFIG_BUILDER_CLASS =
        "com.microsoft.semantickernel.semanticfunctions.PromptTemplateConfig$CompletionConfigBuilder";
    private static final String FALLBACK_COMPLETION_FUNCTION_BUILDER_CLASS =
        "com.microsoft.semantickernel.orchestration.DefaultCompletionSKFunction$Builder";
    private static final String FALLBACK_CONTEXT_BUILDER_CLASS =
        "com.microsoft.semantickernel.orchestration.DefaultSKContext$Builder";
    private static final String FALLBACK_TEXT_EMBEDDING_GENERATION_BUILDER_CLASS =
        "com.microsoft.semantickernel.connectors.ai.openai.textembeddings.OpenAITextEmbeddingGeneration$Builder";
    private static final String FALLBACK_KERNEL_BUILDER_CLASS =
        "com.microsoft.semantickernel.DefaultKernel$Builder";
    private static final String FALLBACK_KERNEL_CONFIG_BUILDER_CLASS =
        "com.microsoft.semantickernel.KernelConfig$Builder";
    private static final String FALLBACK_PROMPT_TEMPLATE_BUILDER_CLASS =
        "com.microsoft.semantickernel.semanticfunctions.DefaultPromptTemplate$Builder";
    private static final String FALLBACK_PROMPT_TEMPLATE_ENGINE_BUILDER_CLASS =
        "com.microsoft.semantickernel.templateengine.DefaultPromptTemplateEngine$Builder";
    private static final String FALLBACK_MEMORY_STORE_BUILDER_CLASS =
        "com.microsoft.semantickernel.memory.VolatileMemoryStore$Builder";
    private static final String FALLBACK_SEMANTIC_TEXT_MEMORY_CLASS =
        "com.microsoft.semantickernel.memory.DefaultSemanticTextMemory$Builder";
    private static final String FALLBACK_SKILL_COLLECTION_BUILDER_CLASS =
        "com.microsoft.semantickernel.skilldefinition.DefaultSkillCollection$Builder";
    private static final String FALLBACK_TEXT_COMPLETION_BUILDER_CLASS =
        "com.microsoft.semantickernel.connectors.ai.openai.textcompletion.OpenAITextCompletion$Builder";
    private static final String FALLBACK_VARIABLE_BUILDER_CLASS =
        "com.microsoft.semantickernel.orchestration.DefaultContextVariables$Builder";

    private static final String FALLBACK_KERNEL_ARGUMENTS_BUILDER_CLASS =
        "com.microsoft.semantickernel.orchestration.contextvariables.DefaultKernelArguments$Builder";


    private static final String FALLBACK_KERNEL_FUNCTION_YAML_BUILDER_CLASS =
        "com.microsoft.semantickernel.semanticfunctions.KernelFunctionYamlBuilder";
    private final Map<Class<?>, Supplier<?>>
        builders = new HashMap<>();

    BuildersSingleton() {
        try {
            registerBuilder(Kernel.Builder.class, FALLBACK_KERNEL_BUILDER_CLASS);
            registerBuilder(KernelArguments.Builder.class, FALLBACK_KERNEL_ARGUMENTS_BUILDER_CLASS);
            registerBuilder(KernelFunctionYaml.Builder.class, FALLBACK_KERNEL_FUNCTION_YAML_BUILDER_CLASS);
            registerBuilder(OpenAIChatCompletion.Builder.class, FALLBACK_OPENAI_CHAT_COMPLETION_BUILDER_CLASS);
            registerBuilder(AzureOpenAIChatCompletion.Builder.class, FALLBACK_AZURE_OPENAI_CHAT_COMPLETION_BUILDER_CLASS);
            /*

            // Keep this list in alphabetical order by fallback variable name
            registerBuilder(ChatCompletionService.Builder.class,
                FALLBACK_CHAT_COMPLETION_BUILDER_CLASS);

            registerBuilder(
                CompletionKernelFunction.Builder.class, FALLBACK_COMPLETION_FUNCTION_BUILDER_CLASS);


            registerBuilder(
                PromptConfig.CompletionConfigBuilder.class,
                FALLBACK_COMPLETION_CONFIG_BUILDER_CLASS);
            registerBuilder(SKContext.Builder.class, FALLBACK_CONTEXT_BUILDER_CLASS);
            registerBuilder(
                    TextEmbeddingGeneration.Builder.class,
                    FALLBACK_TEXT_EMBEDDING_GENERATION_BUILDER_CLASS);
            registerBuilder(KernelConfig.Builder.class, FALLBACK_KERNEL_CONFIG_BUILDER_CLASS);
            registerBuilder(MemoryStore.Builder.class, FALLBACK_MEMORY_STORE_BUILDER_CLASS);
            registerBuilder(PromptTemplate.Builder.class, FALLBACK_PROMPT_TEMPLATE_BUILDER_CLASS);
            registerBuilder(
                    PromptTemplateEngine.Builder.class,
                    FALLBACK_PROMPT_TEMPLATE_ENGINE_BUILDER_CLASS);
            registerBuilder(SemanticTextMemory.Builder.class, FALLBACK_SEMANTIC_TEXT_MEMORY_CLASS);
            registerBuilder(
                    ReadOnlySkillCollection.Builder.class, FALLBACK_SKILL_COLLECTION_BUILDER_CLASS);
            registerBuilder(TextCompletion.Builder.class, FALLBACK_TEXT_COMPLETION_BUILDER_CLASS);

 */
        } catch (Throwable e) {
            Logger LOGGER = LoggerFactory.getLogger(BuildersSingleton.class);
            LOGGER.error("Failed to discover Semantic Kernel Builders", e);
            LOGGER.error(
                "This is likely due to:\n\n"
                    + "- The Semantic Kernel implementation (typically provided by"
                    + " semantickernel-core) is not present on the classpath at runtime, ensure"
                    + " that this dependency is available at runtime. In maven this would be"
                    + " achieved by adding:\n"
                    + "\n"
                    + "        <dependency>\n"
                    + "            <groupId>com.microsoft.semantickernel</groupId>\n"
                    + "            <artifactId>semantickernel-core</artifactId>\n"
                    + "            <version>${skversion}</version>\n"
                    + "            <scope>runtime</scope>\n"
                    + "        </dependency>\n\n"
                    + "- The META-INF/services files that define the service loading have been"
                    + " filtered out and are not present within the running application\n\n"
                    + "- The class names have been changed (for instance shaded) preventing"
                    + " discovering the classes");

            throw e;
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void registerBuilder(
        Class<T> clazz, String fallbackClassName) {
        builders.put(
            clazz,
            (Supplier<? extends Buildable>)
                ServiceLoadUtil.findServiceLoader(clazz, fallbackClassName));
    }

    @SuppressWarnings("unchecked")
    public <T> T getInstance(Class<T> clazz) {
        return (T) builders.get(clazz).get();
    }
}
