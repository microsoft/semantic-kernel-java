// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantic.kernel.rag.splitting.splitconditions;

import java.util.regex.Pattern;

/**
 * A splitter that splits text into paragraphs.
 * <p>
 * This is a very simple and inaccurate splitter that splits text into paragraphs based on either:
 * <p>
 * <ul>
 *     <li>Two or more consecutive newlines.</li>
 *     <li>A line ending in a end sentence character (i.e a period)</li>
 * </ul>
 */
public class ParagraphSplitter extends RegexSplitter {

    private static final Pattern PARAGRAPH_SPLIT_REGEX = Pattern
        .compile("[(\\r\\n)\\n\\r]{2,}|([\\.\\．!\\?。]+\\s*[(\\r\\n)\\n\\r])", Pattern.MULTILINE);

    public ParagraphSplitter() {
        super(PARAGRAPH_SPLIT_REGEX);
    }
}
