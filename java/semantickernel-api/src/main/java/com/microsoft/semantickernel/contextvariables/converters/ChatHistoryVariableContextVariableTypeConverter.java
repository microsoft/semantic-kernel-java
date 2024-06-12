// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.contextvariables.converters;

import static com.microsoft.semantickernel.contextvariables.ContextVariableTypes.convert;

import com.microsoft.semantickernel.contextvariables.ContextVariableTypeConverter;
import com.microsoft.semantickernel.contextvariables.ContextVariableTypes;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import java.util.stream.Collectors;

/**
 * A {@link ContextVariableTypeConverter} for
 * {@code com.microsoft.semantickernel.chathistory.ChatHistory} variables. Use
 * {@code ContextVariableTypes.getGlobalVariableTypeForClass(ChatHistory.class)} to get an instance
 * of this class.
 *
 * @see ContextVariableTypes#getGlobalVariableTypeForClass(Class)
 */
public class ChatHistoryVariableContextVariableTypeConverter extends
    ContextVariableTypeConverter<ChatHistory> {

    /**
     * Initializes a new instance of the {@link ChatHistoryVariableContextVariableTypeConverter}
     * class.
     */
    public ChatHistoryVariableContextVariableTypeConverter() {
        super(
            ChatHistory.class,
            s -> convert(s, ChatHistory.class),
            ChatHistoryVariableContextVariableTypeConverter::toXmlString,
            x -> {
                throw new UnsupportedOperationException(
                    "ChatHistoryVariableConverter does not support fromPromptString");
            });
    }

    /*
     * Converts a {@link ChatHistory} to an XML string.
     */
    private static String toXmlString(ChatHistory chatHistory) {
        String messages = chatHistory
            .getMessages()
            .stream()
            .map(message -> String.format("<message role=\"%s\">%s</message>",
                message.getAuthorRole(),
                escapeXmlString(message.getContent())))
            .collect(Collectors.joining("\n"));

        return String.format("<messages>%n%s%n</messages>", messages);
    }
}
