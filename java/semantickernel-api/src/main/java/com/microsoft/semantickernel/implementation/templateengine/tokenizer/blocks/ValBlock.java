// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.implementation.templateengine.tokenizer.blocks;

import com.microsoft.semantickernel.contextvariables.ContextVariableTypes;
import com.microsoft.semantickernel.semanticfunctions.KernelFunctionArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.util.annotation.Nullable;

public final class ValBlock extends Block implements TextRendering {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValBlock.class);

    // Cache the first and last char
    private char first = '\0';
    private char last = '\0';

    // Content, excluding start/end quote chars
    private String value = "";

    public ValBlock(String quotedValue) {
        super(quotedValue.trim(), BlockTypes.VALUE);

        if (this.getContent().length() < 2) {
            LOGGER.error("A value must have single quotes or double quotes on both sides");
            return;
        }

        this.first = this.getContent().charAt(0);
        this.last = this.getContent().charAt(getContent().length() - 1);
        this.value = this.getContent().substring(1, this.getContent().length() - 1);
    }

    public static boolean hasValPrefix(@Nullable String text) {
        return text != null
            && !text.isEmpty()
            && (text.charAt(0) == Symbols.DblQuote || text.charAt(0) == Symbols.SglQuote);
    }

    @Override
    @Nullable
    public String render(ContextVariableTypes types, @Nullable KernelFunctionArguments variables) {
        return value;
    }

    @Override
    public boolean isValid() {
        // Content includes the quotes, so it must be at least 2 chars long
        if (this.getContent().length() < 2) {
            LOGGER.error("A value must have single quotes or double quotes on both sides");
            return false;
        }

        // Check if delimiting chars are consistent
        if (first != last) {
            LOGGER.error(
                "A value must be defined using either single quotes or double quotes, not"
                    + " both");
            return false;
        }

        return true;
    }
}
