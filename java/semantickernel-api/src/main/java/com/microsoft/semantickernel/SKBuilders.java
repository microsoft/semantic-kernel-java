// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel;

import com.microsoft.semantickernel.builders.BuildersSingleton;

public class SKBuilders {

    // Prevent creating object
    private SKBuilders() {
    }

    /*
    public static TextCompletion.Builder textCompletion() {
        return BuildersSingleton.INST.getInstance(TextCompletion.Builder.class);
    }

    public static TextEmbeddingGeneration.Builder textEmbeddingGeneration() {
        return BuildersSingleton.INST.getInstance(TextEmbeddingGeneration.Builder.class);
    }
    */

    public static Kernel.Builder kernel() {
        return BuildersSingleton.INST.getInstance(Kernel.Builder.class);
    }
/*
    public static KernelConfig.Builder kernelConfig() {
        return BuildersSingleton.INST.getInstance(KernelConfig.Builder.class);
    }

    public static SemanticTextMemory.Builder semanticTextMemory() {
        return BuildersSingleton.INST.getInstance(SemanticTextMemory.Builder.class);
    }

    public static ReadOnlySkillCollection.Builder skillCollection() {
        return BuildersSingleton.INST.getInstance(ReadOnlySkillCollection.Builder.class);
    }

    public static PromptTemplate.Builder promptTemplate() {
        return BuildersSingleton.INST.getInstance(PromptTemplate.Builder.class);
    }

    public static PromptTemplateEngine.Builder promptTemplateEngine() {
        return BuildersSingleton.INST.getInstance(PromptTemplateEngine.Builder.class);
    }

    public static ContextVariables.Builder variables() {
        return BuildersSingleton.INST.getInstance(ContextVariables.Builder.class);
    }

    public static SKContext.Builder context() {
        return BuildersSingleton.INST.getInstance(SKContext.Builder.class);
    }

    public static PromptConfig.CompletionConfigBuilder completionConfig() {
        return BuildersSingleton.INST.getInstance(
                PromptConfig.CompletionConfigBuilder.class);
    }

    @SuppressWarnings("unchecked")
    public static <ChatHistoryType extends ChatHistory>
            ChatCompletion.Builder<ChatHistoryType> chatCompletion() {
        return (ChatCompletion.Builder<ChatHistoryType>)
                BuildersSingleton.INST.getInstance(ChatCompletion.Builder.class);
    }

    public static MemoryStore.Builder memoryStore() {
        return BuildersSingleton.INST.getInstance(MemoryStore.Builder.class);
    }

    public static CompletionKernelFunction.Builder completionFunctions() {
        return BuildersSingleton.INST.getInstance(CompletionKernelFunction.Builder.class);
    }

     */
}
