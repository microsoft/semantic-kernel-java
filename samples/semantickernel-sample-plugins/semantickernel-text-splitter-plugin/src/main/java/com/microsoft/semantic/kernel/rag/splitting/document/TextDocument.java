// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantic.kernel.rag.splitting.document;

import com.microsoft.semantic.kernel.rag.splitting.Document;
import reactor.core.publisher.Flux;

/**
 * A document that contains a plain text string.
 */
public class TextDocument implements Document {

    private final String document;

    public TextDocument(String document) {
        this.document = document;
    }

    @Override
    public Flux<String> getContent() {
        return Flux.just(document);
    }
}
