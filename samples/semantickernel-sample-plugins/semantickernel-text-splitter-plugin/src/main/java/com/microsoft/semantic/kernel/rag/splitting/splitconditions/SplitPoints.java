// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantic.kernel.rag.splitting.splitconditions;

/**
 * A class that represents the start and end points of a split. I.e if splitting by word, these
 * would be the indeces of the first and last char in the word within the chunk.
 */
public class SplitPoints {

    private final int start;
    private final int end;

    public SplitPoints(int start, int end) {
        this.start = start;
        this.end = end;
    }

    /**
     * The index of the first character in the split.
     *
     * @return the index of the first character in the split
     */
    public int getStart() {
        return start;
    }

    /**
     * The index of the last character in the split.
     *
     * @return the index of the last character in the split
     */
    public int getEnd() {
        return end;
    }
}