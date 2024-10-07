// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantic.kernel.rag.splitting.splitconditions;

import java.util.regex.Pattern;

/**
 * A splitter that splits text into words.
 */
public class WordSplitter extends RegexSplitter {

    private static final Pattern WORD_SPLIT_REGEX = Pattern
        .compile("[,、;: ()\\[\\]{}\t\n]+", Pattern.MULTILINE);

    public WordSplitter() {
        super(WORD_SPLIT_REGEX, 1);
    }
}
