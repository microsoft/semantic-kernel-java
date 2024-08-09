// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.agents;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import com.microsoft.semantickernel.agents.internal.BroadcastQueue;
import com.microsoft.semantickernel.agents.internal.ChannelReference;
import com.microsoft.semantickernel.agents.internal.KeyEncoder;
import com.microsoft.semantickernel.exceptions.SKException;
import com.microsoft.semantickernel.services.chatcompletion.AuthorRole;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;

import reactor.core.publisher.Mono;

/**
 * Point of interaction for one or more agents.
 * 
 * An AgentChat instance does not support concurrent invocation and
 * are synchronized using {@code java.util.concurrent.locks.Lock}. Any 
 * thread attempting to invoke a public method while another thread is
 * holding the lock will block until the lock is released.
 */
public abstract class AgentChat {

    private final BroadcastQueue broadcastQueue;
    // Map channel hash to channel: one entry per channel.
    private final Map<String, AgentChannel> agentChannels;
    // Map agent to its channel-hash: one entry per agent.
    private final Map<Agent, String> channelMap;
    private final ChatHistory chatHistory;
    private final Lock lock;

    /**
     * Process a series of interactions between the agents participating in this chat.
     * 
     * @return Asynchronous enumeration of messages.
     */
    public abstract List<ChatMessageContent<?>> invokeAsync();

    /**
     * Retrieve the chat history.
     * 
     * @return The message history
     */
    public Mono<List<ChatMessageContent<?>>> getChatMessagesAsync() {
        return getChatMessagesAsync(null);
    }

   /**
     * Retrieve the message history, either the primary history or
     * an agent specific version.
     * 
     * @param agent An optional agent, if requesting an agent history.
     * @return The message history
     * 
     * Any AgentChat instance does not support concurrent invocation and
     * will throw exception if concurrent activity is attempted.
     */
    public Mono<List<ChatMessageContent<?>>> getChatMessagesAsync(Agent agent) {
        lock.lock();
        try {
            if (agent == null) {
                return Mono.just(chatHistory.getMessages());
            } else {
                String channelHash = getAgentHash(agent);
                return synchronizeChannelAsync(channelHash)
                    .flatMap(channel -> channel.getHistoryAsync());    
            }
        } finally {
            lock.unlock();  
        }
    }

    /**
     * Append a message to the conversation. Adding a message while an agent is active is not allowed.
     *
     * @param message A non-system message with which to append to the conversation.
     * @throws KernelException if a system message is present, without taking any other action
     * @throws KernelException chat has current activity.
     */
    public void addChatMessage(ChatMessageContent<?> message) {
        addChatMessages(Arrays.asList(message));
    }

    /**
     * Append messages to the conversation. Adding messages while an agent is active is not allowed.
     *
     * @param messages Set of non-system messages with which to append to the conversation.
     * @throws KernelException if a system message is present, without taking any other action
     * @throws KernelException chat has current activity.
     */
    public void addChatMessages(List<ChatMessageContent<?>> messages) {
        lock.lock();
        try {
           
            if (messages.stream().anyMatch(it -> it.getAuthorRole() == AuthorRole.SYSTEM)) {
                throw new SKException(
                        String.format("History does not support messages with Rople of %s", AuthorRole.SYSTEM));
            }
            
            // Append chat history
            chatHistory.addAll(messages);

            // Broadcast message to other channels (in parallel)
            // Note: Able to queue messages without synchronizing channels.
            List<ChannelReference> channelRefs = 
                agentChannels.entrySet().stream()
                .map(entry -> new ChannelReference(entry.getValue(), entry.getKey()))
                .collect(Collectors.toList());

            broadcastQueue.enqueue(channelRefs, messages);

        } finally {
            lock.unlock();
        }
    }

    /**
     * Construct a new {@link AgentChat} instance.
     */
    protected AgentChat() {
        broadcastQueue = new BroadcastQueue();
        agentChannels = new HashMap<>();
        channelMap = new HashMap<>();
        chatHistory = new ChatHistory();
        lock = new ReentrantLock();
    }

    /**
     * Exposes the internal history to subclasses.
     */
    protected ChatHistory getHistory() {
        return chatHistory;
    }

    /**
     * Process a discrete incremental interaction between a single Agent an a AgentChat.
     *
     * @param agent The agent actively interacting with the chat.
     * @return Asynchronous enumeration of messages.
     */
    protected Mono<List<ChatMessageContent<?>>> invokeAgentAsync(Agent agent) {
        lock.lock();
        try {
            return getOrCreateChannel(agent)
                .flatMap(channel -> channel.invokeAsync(agent)
                        .doOnNext(messages -> {
                            chatHistory.addAll(messages);
                            List<ChannelReference> channelRefs = agentChannels.entrySet().stream()
                                .filter(entry -> !entry.getKey().equals(agent))
                                .map(entry -> new ChannelReference(entry.getValue(), entry.getKey()))
                                .collect(Collectors.toList());
                                broadcastQueue.enqueue(channelRefs, messages);
                            })
                    );
        } finally {
            lock.unlock();
        }
    }

    private String getAgentHash(@Nonnull Agent agent) {
        String hash = channelMap.computeIfAbsent(agent, key -> KeyEncoder.generateHash(key.getChannelKeys()));
        return hash;
    }

    private Mono<AgentChannel> synchronizeChannelAsync(String channelHash) {
        AgentChannel channel = agentChannels.get(channelHash);
        if (channel != null) {
            return broadcastQueue.ensureSynchronizedAsync(
                new ChannelReference(channel, channelHash)
            );
        }
        return Mono.empty();
    }

    private Mono<AgentChannel> getOrCreateChannel(Agent agent) {
        String channelHash = getAgentHash(agent);
        return synchronizeChannelAsync(channelHash)
            .flatMap(channel -> {
                if (channel == null) {
                    return agent.createChannelAsync()
                        .doOnNext(newChannel -> {
                            agentChannels.put(channelHash, newChannel);
                        })
                        .flatMap(newChannel -> {
                            if (chatHistory.getMessages().size() > 0) {
                                return newChannel.receiveAsync(chatHistory.getMessages());
                            }
                            return Mono.empty();
                        })
                        .then(Mono.just(agentChannels.get(channelHash)));
                }
                return Mono.just(channel);
        });
    }
}
