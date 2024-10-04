// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantic.kernel.rag.splitting.splitconditions;

import java.util.regex.Pattern;

/**
 * A splitter that splits text into sentences.
 */
public class SentenceSplitter extends RegexSplitter {

    private static final Pattern SENTENCE_SPLIT_REGEX = Pattern
        .compile("[\\.\\．!\\?。]+", Pattern.MULTILINE);

    public SentenceSplitter() {
        super(SENTENCE_SPLIT_REGEX);
    }
}
