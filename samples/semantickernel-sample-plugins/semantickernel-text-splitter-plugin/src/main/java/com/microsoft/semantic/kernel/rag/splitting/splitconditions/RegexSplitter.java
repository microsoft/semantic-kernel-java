// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantic.kernel.rag.splitting.splitconditions;

import com.microsoft.semantic.kernel.rag.splitting.TextSplitter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A text splitter that uses a regex to find the deliminators
 */
public abstract class RegexSplitter implements TextSplitter {

    public static final int DEFAULT_TRIVIAL_SPLIT_LENGTH = 10;

    private final Pattern pattern;
    /**
     * Splits below this length are considered trivial and will be merged
     */
    private final int trivialSplitLength;

    /**
     * Splitter that uses the given regex pattern to split the text
     *
     * @param pattern the regex pattern to split the text
     */
    public RegexSplitter(Pattern pattern) {
        this(pattern, DEFAULT_TRIVIAL_SPLIT_LENGTH);
    }

    /**
     * Splitter that uses the given regex pattern to split the text
     *
     * @param pattern            the regex pattern to split the text
     * @param trivialSplitLength the length of a split below which it will be considered trivial and
     *                           will be merged
     */
    public RegexSplitter(Pattern pattern, int trivialSplitLength) {
        this.pattern = pattern;
        this.trivialSplitLength = trivialSplitLength;
    }

    @Override
    public List<SplitPoint> getNSplitPoints(String doc, int n) {
        Matcher matcher = pattern.matcher(doc);

        List<MatchResult> points = matcher.results()
            .collect(Collectors.toList());

        List<SplitPoint> result = new ArrayList<>();

        int previousEnd = 0;
        for (MatchResult point : points) {
            if (isTrivialSplit(
                /* start= */ previousEnd,
                /* end= */ point.start(),
                doc,
                trivialSplitLength)) {
                continue;
            }
            result.add(new SplitPoint(previousEnd, point.end()));
            previousEnd = point.end();
            if (result.size() >= n) {
                break;
            }
        }

        if (result.size() < n && !isTrivialSplit(previousEnd, doc.length(), doc, 1)) {
            result.add(new SplitPoint(previousEnd, doc.length()));
        }

        if (result.isEmpty()) {
            return List.of(new SplitPoint(0, doc.length()));
        }

        return result;
    }

    private boolean isTrivialSplit(int start, int end, String doc, int trivialSplitLength) {
        String split = doc.substring(start, end);

        // Remove all split characters and whitespace
        split = pattern.matcher(split).replaceAll("");
        split = split.replaceAll("\\s+", "");

        return split.length() <= trivialSplitLength;

    }
}
