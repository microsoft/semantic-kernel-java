// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantic.kernel.rag.splitting;

import com.microsoft.semantic.kernel.rag.splitting.splitconditions.SplitPoints;

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
    List<SplitPoints> getSplitPoints(String doc);

    /**
     * Get the first n split points for the given document.
     *
     * @param doc the document to split
     * @param n   the number of split points to get
     * @return the split points
     */
    List<SplitPoints> getNSplitPoints(String doc, int n);
}