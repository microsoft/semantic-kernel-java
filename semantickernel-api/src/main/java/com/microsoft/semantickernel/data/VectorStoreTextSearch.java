// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data;

import com.microsoft.semantickernel.data.textsearch.DefaultTextSearchResultMapper;
import com.microsoft.semantickernel.data.textsearch.DefaultTextSearchStringMapper;
import com.microsoft.semantickernel.data.textsearch.KernelSearchResults;
import com.microsoft.semantickernel.data.textsearch.TextSearch;
import com.microsoft.semantickernel.data.textsearch.TextSearchOptions;
import com.microsoft.semantickernel.data.textsearch.TextSearchResult;
import com.microsoft.semantickernel.data.textsearch.TextSearchResultMapper;
import com.microsoft.semantickernel.data.textsearch.TextSearchStringMapper;
import com.microsoft.semantickernel.data.vectorsearch.VectorSearchFilter;
import com.microsoft.semantickernel.data.vectorsearch.VectorSearchResults;
import com.microsoft.semantickernel.data.vectorsearch.VectorizedSearch;
import com.microsoft.semantickernel.data.vectorstorage.options.VectorSearchOptions;
import com.microsoft.semantickernel.exceptions.SKException;
import com.microsoft.semantickernel.services.textembedding.TextEmbeddingGenerationService;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.stream.Collectors;

/**
 * A text search implementation that uses a vector record collection to perform the search.
 *
 * @param <Record> The record type.
 */
public class VectorStoreTextSearch<Record> implements TextSearch {

    private final VectorizedSearch<Record> vectorizedSearch;
    private final TextEmbeddingGenerationService textEmbeddingGenerationService;
    private final TextSearchStringMapper stringMapper;
    private final TextSearchResultMapper resultMapper;
    private final VectorStoreTextSearchOptions options;

    /**
     * Create a new instance of VectorStoreTextSearch with {@link VectorizedSearch} to perform vectorized search and
     * {@link TextEmbeddingGenerationService} to generate text embeddings.
     *
     * @param vectorizedSearch               The vectorized search. Usually a vector record collection.
     * @param textEmbeddingGenerationService The text embedding generation service.
     * @param stringMapper                   The string mapper.
     * @param resultMapper                   The result mapper.
     * @param options                        The options.
     */
    public VectorStoreTextSearch(
        @Nonnull VectorizedSearch<Record> vectorizedSearch,
        @Nonnull TextEmbeddingGenerationService textEmbeddingGenerationService,
        @Nullable TextSearchStringMapper stringMapper,
        @Nullable TextSearchResultMapper resultMapper,
        @Nullable VectorStoreTextSearchOptions options) {
        this.vectorizedSearch = vectorizedSearch;
        this.textEmbeddingGenerationService = textEmbeddingGenerationService;
        this.stringMapper = stringMapper == null ? new DefaultTextSearchStringMapper()
            : stringMapper;
        this.resultMapper = resultMapper == null ? new DefaultTextSearchResultMapper()
            : resultMapper;
        this.options = options == null ? new VectorStoreTextSearchOptions() : options;
    }

    private Mono<VectorSearchResults<Record>> executeSearchAsync(String query,
        TextSearchOptions options) {
        if (options == null) {
            options = TextSearchOptions.createDefault();
        }

        VectorSearchOptions vectorSearchOptions = VectorSearchOptions.builder()
            .withVectorSearchFilter(options.getFilter() != null
                ? new VectorSearchFilter(options.getFilter().getFilterClauses())
                : null)
            .withTop(options.getTop())
            .withSkip(options.getSkip())
            .withIncludeTotalCount(options.isIncludeTotalCount())
            .build();

        return textEmbeddingGenerationService.generateEmbeddingAsync(query)
            .flatMap(embedding -> vectorizedSearch.searchAsync(embedding.getVector(),
                vectorSearchOptions));
    }

    /**
     * Perform a search for content related to the specified query and return String values representing the search results.
     *
     * @param query   The text to search for.
     * @param options The search options.
     * @return The search results.
     */
    @Override
    public Mono<KernelSearchResults<String>> searchAsync(String query, TextSearchOptions options) {
        return executeSearchAsync(query, options)
            .map(results -> new KernelSearchResults<>(
                results.getResults().stream()
                    .map(r -> stringMapper.fromResultToString(r.getRecord()))
                    .collect(Collectors.toList()),
                results.getTotalCount(),
                results.getMetadata()));
    }

    /**
     * Perform a search for content related to the specified query and return TextSearchResult values representing the search results.
     *
     * @param query   The text to search for.
     * @param options The search options.
     * @return The search results.
     */
    @Override
    public Mono<KernelSearchResults<TextSearchResult>> getTextSearchResultsAsync(String query,
        TextSearchOptions options) {
        return executeSearchAsync(query, options)
            .map(results -> new KernelSearchResults<>(
                results.getResults().stream()
                    .map(r -> resultMapper.fromResultToTextSearchResult(r.getRecord()))
                    .collect(Collectors.toList()),
                results.getTotalCount(),
                results.getMetadata()));
    }

    /**
     * Perform a search for content related to the specified query and return Object values representing the search results.
     *
     * @param query   The text to search for.
     * @param options The search options.
     * @return The search results.
     */
    @Override
    public Mono<KernelSearchResults<Object>> getSearchResultsAsync(String query,
        TextSearchOptions options) {
        return executeSearchAsync(query, options)
            .map(results -> new KernelSearchResults<>(
                results.getResults().stream()
                    .map(r -> resultMapper.fromResultToTextSearchResult(r.getRecord()))
                    .collect(Collectors.toList()),
                results.getTotalCount(),
                results.getMetadata()));
    }

    /**
     * Create a new instance of {@link Builder}.
     *
     * @param <Record> The record type.
     * @return The builder.
     */
    public static <Record> Builder<Record> builder() {
        return new Builder<>();
    }

    /**
     * A builder for the {@link VectorStoreTextSearch} class.
     *
     * @param <Record> The record type.
     */
    public static class Builder<Record> {
        @Nullable
        private VectorizedSearch<Record> vectorizedSearch;
        @Nullable
        private TextEmbeddingGenerationService textEmbeddingGenerationService;
        @Nullable
        private TextSearchStringMapper stringMapper;
        @Nullable
        private TextSearchResultMapper resultMapper;
        @Nullable
        private VectorStoreTextSearchOptions options;

        /**
         * Sets the vectorized search.
         *
         * @param vectorizedSearch The vectorized search.
         * @return The builder.
         */
        public Builder<Record> withVectorizedSearch(VectorizedSearch<Record> vectorizedSearch) {
            this.vectorizedSearch = vectorizedSearch;
            return this;
        }

        /**
         * Sets the text embedding generation service.
         *
         * @param textEmbeddingGenerationService The text embedding generation service.
         * @return The builder.
         */
        public Builder<Record> withTextEmbeddingGenerationService(
            TextEmbeddingGenerationService textEmbeddingGenerationService) {
            this.textEmbeddingGenerationService = textEmbeddingGenerationService;
            return this;
        }

        /**
         * Sets the string mapper.
         *
         * @param stringMapper The string mapper.
         * @return The builder.
         */
        public Builder<Record> withStringMapper(TextSearchStringMapper stringMapper) {
            this.stringMapper = stringMapper;
            return this;
        }

        /**
         * Sets the result mapper.
         *
         * @param resultMapper The result mapper.
         * @return The builder.
         */
        public Builder<Record> withResultMapper(TextSearchResultMapper resultMapper) {
            this.resultMapper = resultMapper;
            return this;
        }

        /**
         * Sets the options.
         *
         * @param options The options.
         * @return The builder.
         */
        public Builder<Record> withOptions(VectorStoreTextSearchOptions options) {
            this.options = options;
            return this;
        }

        /**
         * Builds the {@link VectorStoreTextSearch} instance.
         *
         * @return The {@link VectorStoreTextSearch} instance.
         */
        public VectorStoreTextSearch<Record> build() {
            if (vectorizedSearch == null) {
                throw new SKException("Vectorized search is required");
            }
            if (textEmbeddingGenerationService == null) {
                throw new SKException("Text embedding generation service is required");
            }

            return new VectorStoreTextSearch<>(vectorizedSearch, textEmbeddingGenerationService,
                stringMapper, resultMapper, options);
        }
    }
}
