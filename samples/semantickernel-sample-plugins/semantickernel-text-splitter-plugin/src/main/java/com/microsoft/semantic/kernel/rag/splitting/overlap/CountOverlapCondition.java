// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantic.kernel.rag.splitting.overlap;

import com.microsoft.semantic.kernel.rag.splitting.OverlapCondition;
import com.microsoft.semantic.kernel.rag.splitting.TextSplitter;
import com.microsoft.semantic.kernel.rag.splitting.splitconditions.SplitPoints;
import java.util.List;

/**
 * Overlap condition based on counting the number of "splits" i.e if splitting by words, would allow
 * you to define an overlap as n words, or if by sentences, then n sentences.
 */
public class CountOverlapCondition implements OverlapCondition {

    // The number of splits to count to consider the condition met
    private final int count;

    // The type of splitter to use to get the split points.
    private final TextSplitter splitter;

    public CountOverlapCondition(int count, TextSplitter splitter) {
        this.count = count;
        this.splitter = splitter;
    }

    @Override
    public int getOverlapIndex(String chunk) {
        List<SplitPoints> splitPoints = splitter.getSplitPoints(chunk);

        if (splitPoints.size() == 0) {
            return 0;
        }

        int i = Math.max(splitPoints.size() - count, 0);
        i = Math.min(splitPoints.size() - 1, i);

        return splitPoints.get(i).getStart();
    }
}
