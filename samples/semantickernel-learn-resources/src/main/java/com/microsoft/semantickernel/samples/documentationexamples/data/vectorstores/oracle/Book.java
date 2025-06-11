// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.samples.documentationexamples.data.vectorstores.oracle;

import com.microsoft.semantickernel.data.vectorstorage.annotations.VectorStoreRecordData;
import com.microsoft.semantickernel.data.vectorstorage.annotations.VectorStoreRecordKey;
import com.microsoft.semantickernel.data.vectorstorage.annotations.VectorStoreRecordVector;
import com.microsoft.semantickernel.data.vectorstorage.definition.DistanceFunction;
import com.microsoft.semantickernel.data.vectorstorage.definition.IndexKind;
import java.util.List;

public class Book {

    @VectorStoreRecordKey
    private final String isbn;

    public Book(String isbn, String title, String author, int pages,
        List<String> tags, String summary, List<Float> summaryEmbedding) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.pages = pages;
        this.tags = tags;
        this.summary = summary;
        this.summaryEmbedding = summaryEmbedding;
    }

    @VectorStoreRecordData(isFilterable = true)
    private final String title;

    @VectorStoreRecordData(isFilterable = true)
    private final String author;

    @VectorStoreRecordData
    private final int pages;

    @VectorStoreRecordData(isFilterable = true)
    private final List<String> tags;

    @VectorStoreRecordData( isFilterable = true, isFullTextSearchable = true )
    private final String summary;

    @VectorStoreRecordVector(dimensions = 4, distanceFunction = DistanceFunction.COSINE_DISTANCE, indexKind = IndexKind.HNSW)
    private final List<Float> summaryEmbedding;

}