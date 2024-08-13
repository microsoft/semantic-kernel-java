// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.agents.internal;

import com.microsoft.semantickernel.agents.AgentChannel;

/**
 * Tracks channel along with its hashed key.
 */
public class ChannelReference {

    private final AgentChannel channel;
    private final String hash;

    public ChannelReference(AgentChannel channel, String hash) {
        this.channel = channel;
        this.hash = hash;
    }

    /**
     * The referenced channel.
     * @return The referenced channel.
     */
    public AgentChannel getChannel() {
        return channel;
    }

    /**
     * The channel hash.
     * @return The channel hash.
     */
    public String getHash() {
        return hash;
    }
}
