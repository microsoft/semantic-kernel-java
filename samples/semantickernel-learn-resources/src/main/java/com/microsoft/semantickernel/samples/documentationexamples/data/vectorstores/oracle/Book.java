// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.samples.documentationexamples.data.vectorstores.oracle;

import com.microsoft.semantickernel.data.vectorstorage.annotations.VectorStoreRecordData;
import com.microsoft.semantickernel.data.vectorstorage.annotations.VectorStoreRecordKey;
import com.microsoft.semantickernel.data.vectorstorage.annotations.VectorStoreRecordVector;
import java.util.List;

public class Book {

    public Book() {}

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

    @VectorStoreRecordKey
    private String isbn;

    @VectorStoreRecordData(isFilterable = true)
    private String title;

    @VectorStoreRecordData(isFilterable = true)
    private String author;

    @VectorStoreRecordData
    private int pages;

    @VectorStoreRecordData(isFilterable = true)
    private List<String> tags;

    @VectorStoreRecordData( isFilterable = true, isFullTextSearchable = true )
    private String summary;

    @VectorStoreRecordVector(dimensions = 2)
    private List<Float> summaryEmbedding;

    public String getIsbn() {
        return isbn;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public int getPages() {
        return pages;
    }

    public List<String> getTags() {
        return tags;
    }

    public String getSummary() {
        return summary;
    }

    public List<Float> getSummaryEmbedding() {
        return summaryEmbedding;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public void setSummaryEmbedding(List<Float> summaryEmbedding) {
        this.summaryEmbedding = summaryEmbedding;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}