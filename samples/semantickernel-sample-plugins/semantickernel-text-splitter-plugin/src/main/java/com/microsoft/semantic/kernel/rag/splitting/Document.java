// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantic.kernel.rag.splitting;

import reactor.core.publisher.Flux;

/**
 * A document to be read and split into chunks.
 */
public interface Document {
    Flux<String> getContent();
}
