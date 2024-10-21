// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.vectorsearch;

/**
 * Represents a vector search result.
 * @param <Record> The type of the record.
 */
public class VectorSearchResult<Record> {
    private final Record record;
    private final double score;

    /**
     * Creates a new instance of VectorSearchResult.
     *
     * @param record The record.
     * @param score The score.
     */
    public VectorSearchResult(Record record, double score) {
        this.record = record;
        this.score = score;
    }

    /**
     * Gets the record.
     *
     * @return The record.
     */
    public Record getRecord() {
        return record;
    }

    /**
     * Gets the score.
     *
     * @return The score.
     */
    public double getScore() {
        return score;
    }
}
