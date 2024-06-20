// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.presidio;

import java.util.Collections;
import java.util.Map;

public class AnonymizedText {

    private final String unRedacted;
    private final String redacted;
    private final Map<String, String> redactedTokenMap;

    public AnonymizedText(
        String unRedacted,
        String redacted,
        Map<String, String> redactedTokenMap) {
        this.unRedacted = unRedacted;
        this.redacted = redacted;
        this.redactedTokenMap = Collections.unmodifiableMap(redactedTokenMap);
    }

    public String getUnRedacted() {
        return unRedacted;
    }

    public String getRedacted() {
        return redacted;
    }

    public Map<String, String> getRedactedTokenMap() {
        return redactedTokenMap;
    }

    public String unredact(String message) {
        return redactedTokenMap
            .entrySet()
            .stream()
            .reduce(message,
                (msg, entry) -> msg.replaceAll(entry.getKey(), entry.getValue()),
                (msg1, msg2) -> msg1 + msg2);
    }
}
