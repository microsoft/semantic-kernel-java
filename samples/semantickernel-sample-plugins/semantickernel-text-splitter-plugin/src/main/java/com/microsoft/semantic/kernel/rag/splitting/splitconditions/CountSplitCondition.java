// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantic.kernel.rag.splitting.splitconditions;

import com.microsoft.semantic.kernel.rag.splitting.ChunkEndCondition;
import com.microsoft.semantic.kernel.rag.splitting.TextSplitter;
import java.util.List;

/**
 * Overlap condition based on counting the number of "splits" i.e if splitting by words, would allow
 * you to define a chunk as n words, or if by sentences, then n sentences.
 */
public class CountSplitCondition implements ChunkEndCondition {

    private final int count;
    private final TextSplitter splitter;

    public CountSplitCondition(int count, TextSplitter splitter) {
        this.count = count;
        this.splitter = splitter;
    }

    @Override
    public int getEndOfNextChunk(String doc) {
        List<SplitPoints> splitPoints = splitter.getNSplitPoints(doc, count);

        if (splitPoints.size() < count) {
            return splitPoints.get(splitPoints.size() - 1).getEnd();
        }

        return splitPoints.get(count - 1).getEnd();
    }
}
