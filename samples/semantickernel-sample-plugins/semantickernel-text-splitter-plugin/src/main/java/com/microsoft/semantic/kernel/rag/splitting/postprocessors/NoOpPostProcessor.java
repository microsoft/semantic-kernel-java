// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantic.kernel.rag.splitting.postprocessors;

import com.microsoft.semantic.kernel.rag.splitting.Chunk;
import com.microsoft.semantic.kernel.rag.splitting.ChunkPostProcessor;

/**
 * A post processor that does nothing.
 */
public class NoOpPostProcessor implements ChunkPostProcessor {

    public NoOpPostProcessor() {
    }

    @Override
    public Chunk process(Chunk chunk) {
        return chunk;
    }

}
