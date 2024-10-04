// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantic.kernel.rag.splitting;

/**
 * Defines the condition that should be met for a chunk to be considered full.
 */
public interface ChunkEndCondition {

    /**
     * Accepts a string and returns the number of character that should be considered as the end of
     * the FIRST chunk within the string. This method will be subsiquently called until all pages
     * are found.
     * <p>
     * Return -1 if the value does not contain enough characters to be considered as a full chunk.
     *
     * @param value the value to be checked
     * @return the index of the character that should be considered as the end of the first chunk in
     * the string
     */
    public int getEndOfNextChunk(String value);

}
