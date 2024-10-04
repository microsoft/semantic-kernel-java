// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantic.kernel.rag.splitting.overlap;

import com.microsoft.semantic.kernel.rag.splitting.OverlapCondition;
import com.microsoft.semantic.kernel.rag.splitting.TextSplitter;
import com.microsoft.semantic.kernel.rag.splitting.splitconditions.SplitPoints;
import java.util.List;
import org.slf4j.Logger;

/**
 * Overlap condition based on percentage of the characters in the chunk. It will return the full
 * split that gives atleast the percentage of the characters in the chunk, i.e if you are splitting
 * based on sentence it will return the full sentence.
 */
public class PercentageOverlapCondition implements OverlapCondition {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(
        PercentageOverlapCondition.class);

    private final float percentage;
    private final TextSplitter splitter;

    public PercentageOverlapCondition(float percentage, TextSplitter splitter) {
        if (percentage < 0 || percentage > 100) {
            LOGGER.warn("Percentage must be between 0 and 100, clamping value to this range 100");
            percentage = Math.min(100, Math.max(0, percentage));
        }
        this.percentage = percentage;
        this.splitter = splitter;
    }

    @Override
    public int getOverlapIndex(String chunk) {
        List<SplitPoints> splitPoints = splitter.getSplitPoints(chunk);

        float index = chunk.length() * (100.0f - percentage) / 100.0f;

        for (SplitPoints splitPoint : splitPoints) {
            if (splitPoint.getEnd() > index) {
                return splitPoint.getStart();
            }
        }

        return 0;
    }
}
