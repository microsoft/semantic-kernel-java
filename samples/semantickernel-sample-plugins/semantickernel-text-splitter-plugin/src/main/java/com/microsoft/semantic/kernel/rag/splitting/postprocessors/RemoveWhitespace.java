// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantic.kernel.rag.splitting.postprocessors;

import com.microsoft.semantic.kernel.rag.splitting.Chunk;
import com.microsoft.semantic.kernel.rag.splitting.ChunkPostProcessor;

/**
 * A post processor that removes leading and trailing whitespace from a chunk.
 */
public class RemoveWhitespace implements ChunkPostProcessor {

    @Override
    public Chunk process(Chunk chunk) {
        return new Chunk(chunk.getContents()
            .replaceAll("^\\s+", "")
            .replaceAll("^[\n\r]+", "")
            .replaceAll("\\s+$", "")
            .replaceAll("[\n\r]+$", ""));
    }

}
