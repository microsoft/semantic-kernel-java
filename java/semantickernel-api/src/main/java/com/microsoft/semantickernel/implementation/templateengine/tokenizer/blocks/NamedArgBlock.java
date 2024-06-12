// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.implementation.templateengine.tokenizer.blocks;

import static com.microsoft.semantickernel.implementation.templateengine.tokenizer.blocks.BlockTypes.NAMED_ARG;

import com.microsoft.semantickernel.contextvariables.ContextVariableTypes;
import com.microsoft.semantickernel.exceptions.SKException;
import com.microsoft.semantickernel.implementation.Verify;
import com.microsoft.semantickernel.semanticfunctions.KernelFunctionArguments;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NamedArgBlock extends Block implements TextRendering {

    private static final Logger LOGGER = LoggerFactory.getLogger(NamedArgBlock.class);

    private final String name;
    @Nullable
    private final String value;
    private final VarBlock argNameAsVarBlock;
    @Nullable
    private final VarBlock varBlock;
    @Nullable
    private final ValBlock valBlock;

    public NamedArgBlock(String content, String name, String value) {
        super(content.trim(), NAMED_ARG);
        this.name = name.trim();
        this.value = value.trim();

        this.argNameAsVarBlock = new VarBlock(Symbols.VarPrefix + name);

        if (value.startsWith(String.valueOf(Symbols.VarPrefix))) {
            this.varBlock = new VarBlock(value);
            valBlock = null;
        } else {
            this.valBlock = new ValBlock(value);
            varBlock = null;
        }
    }

    protected NamedArgBlock(
        String content,
        String name,
        @Nullable String value,
        VarBlock argNameAsVarBlock,
        @Nullable VarBlock varBlock,
        @Nullable ValBlock valBlock) {
        super(content, NAMED_ARG);
        this.name = name;
        this.value = value;
        this.argNameAsVarBlock = argNameAsVarBlock;
        this.varBlock = varBlock;
        this.valBlock = valBlock;
    }

    public static NamedArgBlock from(String content) {
        String name = tryGetName(content);
        if (name == null) {
            throw new SKException("Unable to extract name from: " + content);
        }
        String value = tryGetValue(content);
        VarBlock argNameAsVarBlock = new VarBlock(Symbols.VarPrefix + name);

        if (value == null) {
            throw new SKException("Unable to extract value from: " + content);
        }

        VarBlock varBlock;
        ValBlock valBlock;

        if (value.startsWith(String.valueOf(Symbols.VarPrefix))) {
            varBlock = new VarBlock(value);
            valBlock = null;
        } else {
            valBlock = new ValBlock(value);
            varBlock = null;
        }

        return new NamedArgBlock(content, name, value, argNameAsVarBlock, varBlock, valBlock);
    }

    @Nullable
    public static String tryGetName(String text) {
        return splitAndGetPart(text, 0);
    }

    @Nullable
    public static String tryGetValue(String text) {
        return splitAndGetPart(text, 1);
    }

    @SuppressWarnings("StringSplitter")
    @Nullable
    private static String splitAndGetPart(String text, int x) {
        if (Verify.isNullOrEmpty(text)) {
            return null;
        }

        String[] argBlockParts = text.split(String.valueOf(Symbols.NamedArgBlockSeparator));

        if (argBlockParts.length == 2) {
            return argBlockParts[x].trim();
        }
        return null;
    }

    @Override
    public boolean isValid() {
        if (Verify.isNullOrEmpty(this.name)) {
            LOGGER.error("A named argument must have a name");
            return false;
        }

        if (this.valBlock != null && !this.valBlock.isValid()) {
            LOGGER.error("There was an issue with the named argument value for '" + name);
            return false;
        } else if (this.varBlock != null && !this.varBlock.isValid()) {
            LOGGER.error("There was an issue with the named argument value for '" + name);
            return false;
        } else if (this.valBlock == null && this.varBlock == null) {
            LOGGER.error("A named argument must have a value");
            return false;
        }

        // Argument names share the same validation as variables
        return this.argNameAsVarBlock.isValid();
    }

    @Override
    public String render(ContextVariableTypes types, @Nullable KernelFunctionArguments variables) {
        return getContent();
    }

    /// <summary>
    /// Attempts to extract the name and value of a named argument block from a string
    /// </summary>
    /// <param name="text">String from which to extract a name and value</param>
    /// <param name="name">Name extracted from argument block, when successful. Empty string otherwise.</param>
    /// <param name="value">Value extracted from argument block, when successful. Empty string otherwise.</param>
    /// <returns>true when a name and value are successfully extracted from the given text, false otherwise</returns>

    @Nullable
    public VarBlock getVarBlock() {
        return varBlock;

    }

    public String getName() {
        return name;
    }

    @SuppressWarnings("NullAway")
    public String getValue(ContextVariableTypes types, KernelFunctionArguments arguments) {
        boolean valueIsValidValBlock = this.valBlock != null && this.valBlock.isValid();
        if (valueIsValidValBlock) {
            return this.valBlock.render(types, arguments);
        }

        boolean valueIsValidVarBlock = this.varBlock != null && this.varBlock.isValid();
        if (valueIsValidVarBlock) {
            return this.varBlock.render(types, arguments);
        }

        return "";
    }
}
