// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantic.kernel.rag.splitting;

/**
 * Defines how much overlap is allowed between two pages.
 */
public interface OverlapCondition {

    /**
     * Returns the index of the first character that should be considered as the beginning of the
     * overlap.
     *
     * @param chunk the chunk to be checked
     * @return the index of the first character that should be considered as the beginning of the
     * overlap
     */
    public int getOverlapIndex(String chunk);

}
