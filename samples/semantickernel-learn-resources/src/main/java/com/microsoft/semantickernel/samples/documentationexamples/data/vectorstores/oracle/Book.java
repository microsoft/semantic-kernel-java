/*
 ** Oracle Database Vector Store Connector for Semantic Kernel (Java)
 **
 ** Copyright (c) 2025 Oracle and/or its affiliates. All rights reserved.
 **
 ** The MIT License (MIT)
 **
 ** Permission is hereby granted, free of charge, to any person obtaining a copy
 ** of this software and associated documentation files (the "Software"), to
 ** deal in the Software without restriction, including without limitation the
 ** rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 ** sell copies of the Software, and to permit persons to whom the Software is
 ** furnished to do so, subject to the following conditions:
 **
 ** The above copyright notice and this permission notice shall be included in
 ** all copies or substantial portions of the Software.
 **
 ** THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 ** IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 ** FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 ** AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 ** LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 ** FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 ** IN THE SOFTWARE.
 */
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