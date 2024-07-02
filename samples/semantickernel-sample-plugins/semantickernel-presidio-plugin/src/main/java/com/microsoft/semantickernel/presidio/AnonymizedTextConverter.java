// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.presidio;

import com.microsoft.semantickernel.contextvariables.ContextVariableTypeConverter;

public class AnonymizedTextConverter extends ContextVariableTypeConverter<AnonymizedText> {

    public AnonymizedTextConverter() {
        super(
            AnonymizedText.class,
            it -> (AnonymizedText) it,
            AnonymizedText::getRedacted,
            text -> {
                throw new UnsupportedOperationException("AnonymizedText is write-only");
            });
    }
}
