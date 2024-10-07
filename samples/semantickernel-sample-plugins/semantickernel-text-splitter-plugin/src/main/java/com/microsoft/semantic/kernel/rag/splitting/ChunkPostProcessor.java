// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantic.kernel.rag.splitting;

/**
 * A post processor that processes a chunk after it has been split.
 */
public interface ChunkPostProcessor {
    Chunk process(Chunk chunk);
}
