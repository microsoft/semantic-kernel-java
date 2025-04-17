package com.microsoft.semantickernel.agents.chatcompletion;

import com.microsoft.semantickernel.agents.AgentThread;
import com.microsoft.semantickernel.agents.BaseAgentThread;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class ChatHistoryAgentThread extends BaseAgentThread {
    private ChatHistory chatHistory;

    public ChatHistoryAgentThread() {
    }

    /**
     * Constructor for com.microsoft.semantickernel.agents.chatcompletion.ChatHistoryAgentThread.
     *
     * @param id The ID of the thread.
     * @param chatHistory The chat history.
     */
    public ChatHistoryAgentThread(String id, @Nullable ChatHistory chatHistory) {
        super(id);
        this.chatHistory = chatHistory;
    }

    /**
     * Get the chat history.
     *
     * @return The chat history.
     */
    public ChatHistory getChatHistory() {
        return chatHistory;
    }

    @Override
    public Mono<String> createAsync() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
            chatHistory = new ChatHistory();
        }
        return Mono.just(id);
    }

    @Override
    public Mono<Void> deleteAsync() {
        return Mono.fromRunnable(chatHistory::clear);
    }

    @Override
    public Mono<Void> onNewMessageAsync(ChatMessageContent<?> newMessage) {
        return Mono.fromRunnable(() -> {
            chatHistory.addMessage(newMessage);
        });
    }

    public List<ChatMessageContent<?>> getMessages() {
        return chatHistory.getMessages();
    }
}
