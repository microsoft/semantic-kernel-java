// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantic.kernel.rag.splitting;

import com.microsoft.semantic.kernel.rag.splitting.splitconditions.SplitPoint;
import java.util.List;

/**
 * Interface for splitting text into chunks.
 */
public interface TextSplitter {

    /**
     * Get all the split points for the given document.
     *
     * @param doc the document to split
     * @return the split points
     */
    default List<SplitPoint> getSplitPoints(String doc) {
        return getNSplitPoints(doc, Integer.MAX_VALUE);
    }

    /**
     * Get the first n split points for the given document.
     *
     * @param doc the document to split
     * @param n   the number of split points to get
     * @return the split points
     */
    List<SplitPoint> getNSplitPoints(String doc, int n);
}