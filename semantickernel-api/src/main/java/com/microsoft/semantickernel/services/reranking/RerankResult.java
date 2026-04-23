// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.services.reranking;

/**
 * Represents a single reranking result containing a document and its relevance score.
 */
public class RerankResult {
    private final int index;
    private final String text;
    private final double relevanceScore;

    /**
     * Initializes a new instance of the {@link RerankResult} class.
     *
     * @param index          The index of the document in the original input list
     * @param text           The document text
     * @param relevanceScore The relevance score (higher scores indicate greater relevance)
     */
    public RerankResult(int index, String text, double relevanceScore) {
        if (text == null) {
            throw new IllegalArgumentException("Text cannot be null");
        }
        this.index = index;
        this.text = text;
        this.relevanceScore = relevanceScore;
    }

    /**
     * Gets the index of the document in the original input list.
     *
     * @return The index
     */
    public int getIndex() {
        return index;
    }

    /**
     * Gets the document text.
     *
     * @return The text
     */
    public String getText() {
        return text;
    }

    /**
     * Gets the relevance score assigned by the reranker.
     * Higher scores indicate greater relevance to the query.
     *
     * @return The relevance score
     */
    public double getRelevanceScore() {
        return relevanceScore;
    }
}
