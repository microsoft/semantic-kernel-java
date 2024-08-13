// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.agents.internal;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.reactivestreams.Publisher;

import com.microsoft.semantickernel.agents.Agent;
import com.microsoft.semantickernel.agents.AgentChannel;
import com.microsoft.semantickernel.exceptions.SKException;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;

import reactor.core.publisher.Mono;

/**
 * Utility class used by {@link AgentChat} to manage the broadcast of
 * conversation messages via the {@link com.microsoft.semantickernel.agents.AgentChannel#receiveAsync}.
 * Interaction occurs via two methods:
 * <ul>
 * <li>{@link BroadcastQueue#enqueue}: Adds messages to a channel specific queue for processing.</li>
 * <li>{@link BroadcastQueue#ensureSynchronizedAsync}: Blocks until the specified channel's processing queue is empty.</li>
 * </ul>
 * Maintains a set of channel specific queues, each with individual locks.
 * Queue specific locks exist to synchronize access to an individual queue only.
 * Due to the closed "friend" relationship between with {@link AgentChat},
 * {@link BroadcastQueue} is never invoked concurrently, which eliminates
 * race conditions over the queue dictionary.
 */
public class BroadcastQueue {

    /**
     * The queue reference structure.
     */
    private static class QueueReference {

        private final ConcurrentLinkedQueue<List<ChatMessageContent<?>>> queue = new ConcurrentLinkedQueue<>();
        private final Lock queueLock = new ReentrantLock();
        private FutureTask<Void> receiveTask;

        // Any failure that occured during execution of {@link #receiveTask}.
        private Exception receiveFailure;

        /**
         * Convenience logic
         */
        private boolean isEmpty() {
            return this.queue.isEmpty();
        }


        private Lock getQueueLock() {
            return queueLock;
        }

        private Queue<List<ChatMessageContent<?>>> getQueue() {
            return queue;
        }

        /**
         * Capture any failure that may occur during execution of {@link #receiveTask}.
         */
        private void setReceiveFailure(Exception receiveFailure) {
            this.receiveFailure = receiveFailure;
        }

        private Exception getReceiveFailure() {
            return receiveFailure;
        }

        private FutureTask<Void> getReceiveTask() {
            return receiveTask;
        }

        private void setReceiveTask(FutureTask<Void> receiveTask) {
            this.receiveTask = receiveTask;
        }
    }

    private final Map<String, QueueReference> queues = new ConcurrentHashMap<>();

    
    // Defines the yield duration when waiting on a channel-queue to drain.
    // TODO: This should be a configuration setting. See Duration#parse
    private static final Duration blockDuration = Duration.ofMillis(100L);

    private final ExecutorService executorService = 
        Executors.newCachedThreadPool(runnable -> {
                Thread thread = Executors.defaultThreadFactory().newThread(runnable);
                thread.setDaemon(true);
                return thread;
            }
        );
    
    /**
     * Enqueue a set of messages for a given channel.
     *
     * @param channelRefs The target channels for which to broadcast.
     * @param messages    The messages being broadcast.
     */
    public void enqueue(List<ChannelReference> channelRefs, List<ChatMessageContent<?>> messages) {
        for (ChannelReference channelRef : channelRefs) {
            QueueReference queueRef = queues.computeIfAbsent(channelRef.getHash(), key -> new QueueReference());

            queueRef.getQueueLock().lock();
            try {
                queueRef.getQueue().add(messages);
                if (queueRef.getReceiveTask() == null || queueRef.getReceiveTask().isDone()) {
                    queueRef.setReceiveTask(new FutureTask<>(receiveAsync(channelRef, queueRef), null));
                    executorService.submit(queueRef.getReceiveTask());
                }
            } finally {
                queueRef.getQueueLock().unlock();
            }
        }

    }

    /**
     * Blocks until a channel-queue is not in a receive state to ensure that
     * channel history is complete.
     *
     * @param channelRef         A {@link ChannelReference} structure.
     * @return false when channel is no longer receiving.
     * @throws KernelException When channel is out of sync.
     */
    public Mono<AgentChannel> ensureSynchronizedAsync(ChannelReference channelRef) {
        // Either won race with Enqueue or lost race with ReceiveAsync.
        // Missing queue is synchronized by definition.
        QueueReference queueRef = queues.get(channelRef.getHash());
        if (queueRef == null) {
            return Mono.just(channelRef.getChannel());
        }

        FutureTask<Void> receiveTask = queueRef.getReceiveTask();
        if (receiveTask == null || receiveTask.isDone()) {
            return Mono.just(channelRef.getChannel());
        }

        return Mono.fromRunnable(() -> {
            try {
                receiveTask.get(blockDuration.toMillis(), TimeUnit.MILLISECONDS);
            } catch (CancellationException | TimeoutException e) {
                // TODO: Should log the TimeoutException
                // Swallow the exception and move on. 
                // If a TimeoutException occurs, the queue is probably still processing.
                // If a CancellationException occurs, the task was cancelled so there is no point in waiting.
            } catch (InterruptedException e) {
                // Propogate the interrupt
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                // Exception thrown by the receiveTask
                queueRef.setReceiveFailure(e);
            }

            // Propagate prior failure (inform caller of synchronization issue)
            Exception failure = queueRef.getReceiveFailure();
            if (failure != null) {
                queueRef.setReceiveFailure(null);
                throw new SKException("Unexpected failure broadcasting to channel: " + channelRef.getChannel().getClass(), failure);
            }
        })
        .then(Mono.just(channelRef.getChannel()));
    }

    /**
     * Processes the specified queue with the provided channel, until queue is empty.
     * @param channelRef the channel reference
     * @param queueRef the queue reference
     */
    private static Runnable receiveAsync(ChannelReference channelRef, QueueReference queueRef)
    {
        return () -> {
            // Need to capture any failure that may occur during execution of receiveTask.
            // It's an array to get around the final requirement for lambdas.
            Exception[] failures = new Exception[1];

            boolean isEmpty = true; // Default to fall-through state

            // This is a somewhat faithful translation of the .NET code.
            do
            {
                failures[0] = null;
                
                Mono<Void> receiveTask;
                
                // Queue state is only changed within acquired QueueLock.
                // If its empty here, it is synchronized.
                queueRef.getQueueLock().lock();
                try {
                    isEmpty = queueRef.isEmpty();
                    
                    // Process non empty queue
                    if (isEmpty) {
                        break;
                    }
                    
                    List<ChatMessageContent<?>> messages = queueRef.getQueue().peek();
                    receiveTask = channelRef.getChannel().receiveAsync(messages);
                } finally {
                    queueRef.getQueueLock().unlock();
                }
                
                // Queue not empty.
                receiveTask.onErrorMap(e -> {
                    if (e instanceof Exception) {
                        failures[0] = (Exception)e;
                    }
                    return e;
                })
                .block();

                queueRef.getQueueLock().lock();
                try {
                    // Propagate failure or update queue
                    if (failures[0] != null) {
                        queueRef.setReceiveFailure(failures[0]);
                        break; // Failure on non-empty queue means, still not empty.
                    }

                    // Queue has already been peeked.  Remove head on success.
                    queueRef.getQueue().remove();

                    isEmpty = queueRef.isEmpty(); // Re-evaluate state
                } finally {
                    queueRef.getQueueLock().unlock();
                }
            }
            while (!isEmpty);
        };
    }

}

