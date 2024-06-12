// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.implementation.templateengine.tokenizer;

import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.contextvariables.ContextVariableTypes;
import com.microsoft.semantickernel.exceptions.SKException;
import com.microsoft.semantickernel.implementation.Verify;
import com.microsoft.semantickernel.implementation.templateengine.tokenizer.blocks.Block;
import com.microsoft.semantickernel.implementation.templateengine.tokenizer.blocks.BlockTypes;
import com.microsoft.semantickernel.implementation.templateengine.tokenizer.blocks.CodeRendering;
import com.microsoft.semantickernel.implementation.templateengine.tokenizer.blocks.NamedArgBlock;
import com.microsoft.semantickernel.implementation.templateengine.tokenizer.blocks.TextRendering;
import com.microsoft.semantickernel.implementation.templateengine.tokenizer.blocks.VarBlock;
import com.microsoft.semantickernel.orchestration.InvocationContext;
import com.microsoft.semantickernel.semanticfunctions.InputVariable;
import com.microsoft.semantickernel.semanticfunctions.KernelFunctionArguments;
import com.microsoft.semantickernel.semanticfunctions.PromptTemplate;
import com.microsoft.semantickernel.semanticfunctions.PromptTemplateConfig;
import com.microsoft.semantickernel.templateengine.semantickernel.TemplateException;
import com.microsoft.semantickernel.templateengine.semantickernel.TemplateException.ErrorCodes;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * The default prompt template.
 */
public class DefaultPromptTemplate implements PromptTemplate {

    private final PromptTemplateConfig promptTemplateConfig;
    private final List<Block> blocks;

    /**
     * Create a new prompt template.
     *
     * @param promptTemplateConfig The prompt template configuration.
     */
    private DefaultPromptTemplate(
        @Nonnull PromptTemplateConfig promptTemplateConfig,
        @Nonnull List<Block> blocks) {
        this.promptTemplateConfig = promptTemplateConfig;
        this.blocks = Collections.unmodifiableList(blocks);
    }

    /**
     * Build a new prompt template from the given prompt template configuration.
     *
     * @param promptTemplateConfig The prompt template configuration.
     * @return The new prompt template.
     */
    public static DefaultPromptTemplate build(@Nonnull PromptTemplateConfig promptTemplateConfig) {

        List<Block> blocks = extractBlocks(promptTemplateConfig);
        promptTemplateConfig = addMissingInputVariables(promptTemplateConfig, blocks);

        return new DefaultPromptTemplate(promptTemplateConfig, blocks);
    }

    /*
     * Given a prompt template string, extract all the blocks (text, variables, function calls)
     *
     * @return A list of all the blocks, ie the template tokenized in text, variables and function
     * calls
     */
    private static List<Block> extractBlocks(PromptTemplateConfig promptTemplateConfig) {
        String templateText = promptTemplateConfig.getTemplate();

        if (templateText == null) {
            throw new SKException(
                String.format("No prompt template was provided for the prompt %s.",
                    promptTemplateConfig.getName()));
        }

        List<Block> blocks = new TemplateTokenizer().tokenize(templateText);

        Optional<Block> invalid = blocks
            .stream()
            .filter(block -> !block.isValid())
            .findFirst();

        if (invalid.isPresent()) {
            throw new TemplateException(ErrorCodes.SYNTAX_ERROR,
                "Invalid block: " + invalid.get().getContent());
        }

        return blocks;
    }

    /**
     * Augments the prompt template with any variables not already contained there but that are
     * referenced in the prompt template.
     *
     * @param blocks The blocks to search for input variables.
     * @return The augmented prompt template.
     */
    @SuppressWarnings("NullAway")
    private static PromptTemplateConfig addMissingInputVariables(
        PromptTemplateConfig promptTemplateConfig, List<Block> blocks) {
        // Add all of the existing input variables to our known set. We'll avoid adding any
        // dynamically discovered input variables with the same name.
        Set<String> seen = new HashSet<>();

        seen.addAll(
            promptTemplateConfig
                .getInputVariables()
                .stream()
                .map(InputVariable::getName)
                .collect(Collectors.toList()));

        PromptTemplateConfig.Builder promptTemplateConfigBuilder = promptTemplateConfig.copy();

        blocks.forEach(block -> {
            String name = null;
            if (block.getType() == BlockTypes.VARIABLE) {
                name = ((VarBlock) block).getName();
            } else if (block.getType() == BlockTypes.NAMED_ARG) {
                VarBlock blockName = ((NamedArgBlock) block).getVarBlock();
                name = blockName == null ? null : blockName.getName();
            }

            if (!Verify.isNullOrEmpty(name) && !seen.contains(name)) {
                seen.add(name);
                promptTemplateConfigBuilder.addInputVariable(new InputVariable(name));
            }
        });

        return promptTemplateConfigBuilder.build();
    }

    @Override
    public Mono<String> renderAsync(
        Kernel kernel,
        @Nullable KernelFunctionArguments arguments,
        @Nullable InvocationContext context) {

        ContextVariableTypes types;

        if (context != null) {
            types = context.getContextVariableTypes();
        } else {
            types = new ContextVariableTypes();
        }
        return Flux
            .fromIterable(blocks)
            .concatMap(block -> {
                if (block instanceof TextRendering) {
                    return Mono.just(
                        ((TextRendering) block).render(types, arguments));
                } else if (block instanceof CodeRendering) {
                    return ((CodeRendering) block).renderCodeAsync(kernel, arguments, context);
                } else {
                    return Mono.error(new TemplateException(ErrorCodes.UNEXPECTED_BLOCK_TYPE));
                }
            })
            .reduce("", (a, b) -> {
                return a + b;
            });
    }
}
