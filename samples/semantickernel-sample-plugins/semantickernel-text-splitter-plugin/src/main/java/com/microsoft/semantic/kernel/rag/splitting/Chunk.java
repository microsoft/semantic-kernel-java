// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantic.kernel.rag.splitting;

public class Chunk {

    private final String chunk;

    public Chunk(String chunk) {
        this.chunk = chunk;
    }

    public String getContents() {
        return chunk;
    }

}
