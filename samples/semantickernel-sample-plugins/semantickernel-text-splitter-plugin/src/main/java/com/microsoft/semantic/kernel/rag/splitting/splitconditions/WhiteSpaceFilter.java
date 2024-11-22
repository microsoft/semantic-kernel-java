// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantic.kernel.rag.splitting.splitconditions;

import com.microsoft.semantic.kernel.rag.splitting.TrivialChunkFilter;

/**
 * A filter that merges chunks that have less than N non-whitespace characters.
 */
public class WhiteSpaceFilter implements TrivialChunkFilter {

    private final int trivialCharacterCount;

    public WhiteSpaceFilter(int trivialCharacterCount) {
        this.trivialCharacterCount = trivialCharacterCount;
    }

    @Override
    public boolean isTrivialChunk(String doc) {
        return doc.replaceAll("\\s+", "").length() < trivialCharacterCount;
    }
}
