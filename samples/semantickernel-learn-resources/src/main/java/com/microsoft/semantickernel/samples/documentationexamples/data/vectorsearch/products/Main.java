// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.samples.documentationexamples.data.vectorsearch.products;

import com.microsoft.semantickernel.data.VolatileVectorStore;
import com.microsoft.semantickernel.data.VolatileVectorStoreRecordCollectionOptions;
import com.microsoft.semantickernel.data.vectorsearch.VectorSearchFilter;
import com.microsoft.semantickernel.data.vectorstorage.VectorStoreRecordCollection;
import com.microsoft.semantickernel.data.vectorstorage.annotations.VectorStoreRecordData;
import com.microsoft.semantickernel.data.vectorstorage.annotations.VectorStoreRecordKey;
import com.microsoft.semantickernel.data.vectorstorage.annotations.VectorStoreRecordVector;
import com.microsoft.semantickernel.data.vectorstorage.options.VectorSearchOptions;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // Build a query provider
        var vectorStore = new VolatileVectorStore();
        var collection = vectorStore.getCollection("skproducts",
            VolatileVectorStoreRecordCollectionOptions.<Product>builder()
                .withRecordClass(Product.class)
                .build());
        collection.createCollectionIfNotExistsAsync().block();
        var vector = generateEmbeddingsAsync().block();
        collection.upsertAsync(
            new Product("1", "Product 1", List.of("Feature 1", "Feature 2"), vector, vector), null)
            .block();

        withVectorFieldName(collection);
        withTopAndSkip(collection);
        withIncludeVectors(collection);
        withVectorSearchFilter();
    }

    public static void withVectorFieldName(
        VectorStoreRecordCollection<String, Product> collection) {
        // Create the vector search options and indicate that we want to search the FeatureListEmbedding field.
        var searchOptions = VectorSearchOptions.builder()
            .withVectorFieldName("featureListEmbedding")
            .build();

        // Generate a vector for your search text, using the embedding model of your choice
        var searchVector = generateEmbeddingsAsync().block();

        // Do the search
        var searchResult = collection.searchAsync(searchVector, searchOptions).block();
    }

    public static void withTopAndSkip(VectorStoreRecordCollection<String, Product> collection) {
        // Create the vector search options and indicate that we want to skip the first 40 results and then get the next 20.
        var searchOptions = VectorSearchOptions.builder()
            .withTop(20)
            .withSkip(40)
            .build();

        // Generate a vector for your search text, using the embedding model of your choice
        var searchVector = generateEmbeddingsAsync().block();

        // Do the search
        var searchResult = collection.searchAsync(searchVector, searchOptions).block();
    }

    public static void withIncludeVectors(VectorStoreRecordCollection<String, Product> collection) {
        // Create the vector search options and indicate that we want to include vectors in the search results.
        var searchOptions = VectorSearchOptions.builder()
            .withIncludeVectors(true)
            .build();

        // Generate a vector for your search text, using the embedding model of your choice
        var searchVector = generateEmbeddingsAsync().block();

        // Do the search
        var searchResult = collection.searchAsync(searchVector, searchOptions).block();
    }

    public static void withVectorSearchFilter() {
        // Build a query provider
        var vectorStore = new VolatileVectorStore();
        var collection = vectorStore.getCollection("skglossary",
            VolatileVectorStoreRecordCollectionOptions.<Glossary>builder()
                .withRecordClass(Glossary.class)
                .build());
        collection.createCollectionIfNotExistsAsync().block();
        var vector = generateEmbeddingsAsync().block();
        collection.upsertAsync(new Glossary("1", "External Definitions", List.of("memory"),
            "Memory", "The power of the mind to remember things", vector), null).block();

        // Filter where category == 'External Definitions' and tags contain 'memory'.
        var filter = VectorSearchFilter.builder()
            .equalTo("category", "External Definitions")
            .anyTagEqualTo("tags", "memory")
            .build();

        // Create the vector search options and indicate that we want to filter the search results by a specific field.
        var searchOptions = VectorSearchOptions.builder()
            .withVectorSearchFilter(filter)
            .build();

        // Generate a vector for your search text, using the embedding model of your choice
        var searchVector = generateEmbeddingsAsync().block();

        // Do the search
        var searchResult = collection.searchAsync(searchVector, searchOptions).block();
    }

    public static class Product {
        @VectorStoreRecordKey
        private String key;

        @VectorStoreRecordData
        private String description;

        @VectorStoreRecordData
        private List<String> featureList;

        @VectorStoreRecordVector(dimensions = 1536)
        public List<Float> descriptionEmbedding;

        @VectorStoreRecordVector(dimensions = 1536)
        public List<Float> featureListEmbedding;

        public Product() {
        }

        public Product(String key, String description, List<String> featureList,
            List<Float> descriptionEmbedding, List<Float> featureListEmbedding) {
            this.key = key;
            this.description = description;
            this.featureList = featureList;
            this.descriptionEmbedding = Collections.unmodifiableList(descriptionEmbedding);
            this.featureListEmbedding = Collections.unmodifiableList(featureListEmbedding);
        }

        public String getKey() {
            return key;
        }

        public String getDescription() {
            return description;
        }

        public List<String> getFeatureList() {
            return featureList;
        }

        public List<Float> getDescriptionEmbedding() {
            return descriptionEmbedding;
        }

        public List<Float> getFeatureListEmbedding() {
            return featureListEmbedding;
        }
    }

    public static class Glossary {
        @VectorStoreRecordKey
        private String key;

        @VectorStoreRecordData(isFilterable = true)
        private String category;

        @VectorStoreRecordData(isFilterable = true)
        private List<String> tags;

        @VectorStoreRecordData
        private String term;

        @VectorStoreRecordData
        private String definition;

        @VectorStoreRecordVector(dimensions = 1536)
        private List<Float> definitionEmbedding;

        public Glossary() {
        }

        public Glossary(String key, String category, List<String> tags, String term,
            String definition, List<Float> definitionEmbedding) {
            this.key = key;
            this.category = category;
            this.tags = tags;
            this.term = term;
            this.definition = definition;
            this.definitionEmbedding = Collections.unmodifiableList(definitionEmbedding);
        }

        public String getKey() {
            return key;
        }

        public String getCategory() {
            return category;
        }

        public List<String> getTags() {
            return tags;
        }

        public String getTerm() {
            return term;
        }

        public String getDefinition() {
            return definition;
        }

        public List<Float> getDefinitionEmbedding() {
            return definitionEmbedding;
        }
    }

    private static Mono<List<Float>> generateEmbeddingsAsync() {
        return Mono.just(new ArrayList<>(Collections.nCopies(1536, 1.0f)));
    }
}
