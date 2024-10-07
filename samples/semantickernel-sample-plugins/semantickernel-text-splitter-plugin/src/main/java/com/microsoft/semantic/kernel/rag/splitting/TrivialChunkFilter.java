// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantic.kernel.rag.splitting;

/**
 * A filter that determines if a chunk is trivial and should be merged with the previous chunk.
 */
public interface TrivialChunkFilter {

    /**
     * Returns true if the chunk is trivial and should be merged with the previous chunk.
     *
     * @param doc the chunk to be checked
     * @return true if the chunk is trivial
     */
    public boolean isTrivialChunk(String doc);

}