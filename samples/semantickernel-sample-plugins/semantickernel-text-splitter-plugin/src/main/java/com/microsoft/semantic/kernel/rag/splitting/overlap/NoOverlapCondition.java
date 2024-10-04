// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantic.kernel.rag.splitting.overlap;

import com.microsoft.semantic.kernel.rag.splitting.OverlapCondition;

/**
 * An overlap condition that does not overlap.
 */
public class NoOverlapCondition implements OverlapCondition {

    public NoOverlapCondition() {
    }

    @Override
    public int getOverlapIndex(String chunk) {
        return chunk.length();
    }
}
