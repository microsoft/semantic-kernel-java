// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantic.kernel.rag.splitting.splitconditions;

import java.util.regex.Pattern;

/**
 * A splitter that splits text based on new lines.
 */
public class NewLineSplitter extends RegexSplitter {

    private static final Pattern NEW_LINE_SPLIT_REGEX = Pattern
        .compile("[(\\r\\n)\\n\\r]+", Pattern.MULTILINE);

    public NewLineSplitter() {
        super(NEW_LINE_SPLIT_REGEX);
    }
}
