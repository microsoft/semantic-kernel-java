// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantic.kernel.rag.splitting;

import com.microsoft.semantic.kernel.rag.splitting.overlap.CountOverlapCondition;
import com.microsoft.semantic.kernel.rag.splitting.overlap.NoOverlapCondition;
import com.microsoft.semantic.kernel.rag.splitting.overlap.PercentageOverlapCondition;
import com.microsoft.semantic.kernel.rag.splitting.postprocessors.RemoveWhitespace;
import com.microsoft.semantic.kernel.rag.splitting.splitconditions.CountSplitCondition;
import com.microsoft.semantic.kernel.rag.splitting.splitconditions.NewLineSplitter;
import com.microsoft.semantic.kernel.rag.splitting.splitconditions.ParagraphSplitter;
import com.microsoft.semantic.kernel.rag.splitting.splitconditions.SentenceSplitter;
import com.microsoft.semantic.kernel.rag.splitting.splitconditions.WhiteSpaceFilter;
import com.microsoft.semantic.kernel.rag.splitting.splitconditions.WordSplitter;
import com.microsoft.semantickernel.exceptions.SKException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

/**
 * Splits a document into chunks based on supplied chunking strategy.
 * <p>
 * The chunking strategies and conditions are somewhat soft limits. In scenarios where trivial
 * chunks or tokens would be formed by the chunking strategy (for instance a chunk of only a few
 * words would be formed), these will be merged into the previous chunk. As such it is possible that
 * the chunks returned may be larger than the specified chunking strategy would imply, after other
 * trivial chunks have been merged into them.
 */
public class Splitter {

    private static final Logger LOGGER = LoggerFactory.getLogger(Splitter.class.getName());

    private final List<ChunkEndCondition> chunkEndConditions;
    private final OverlapCondition overlapCondition;
    private final TrivialChunkFilter trivialChunkFilter;
    private final ChunkPostProcessor chunkPostProcessor;

    public Splitter(
        List<ChunkEndCondition> chunkEndConditions,
        OverlapCondition overlapCondition,
        TrivialChunkFilter trivialChunkFilter,
        ChunkPostProcessor chunkPostProcessor) {
        this.chunkEndConditions = Collections.unmodifiableList(chunkEndConditions);
        this.overlapCondition = overlapCondition;
        this.trivialChunkFilter = trivialChunkFilter;
        this.chunkPostProcessor = chunkPostProcessor;
    }

    /**
     * Splits a document into chunks.
     *
     * @param document the document to split
     * @return chunks
     */
    public Flux<Chunk> splitDocument(Document document) {
        return splitDocument(document, chunkEndConditions, overlapCondition, trivialChunkFilter,
            chunkPostProcessor);
    }

    /**
     * Splits a document into chunks.
     *
     * @param document           the document to split
     * @param chunkEndConditions the conditions that determine the end of a chunk
     * @param overlapCondition   the condition that determines the overlap between chunks
     * @param trivialChunkFilter the filter that determines if a chunk is trivial
     * @param chunkPostProcessor the post processor to apply to the chunks
     * @return a flux of chunks
     */
    public static Flux<Chunk> splitDocument(
        Document document,
        List<ChunkEndCondition> chunkEndConditions,
        OverlapCondition overlapCondition,
        TrivialChunkFilter trivialChunkFilter,
        ChunkPostProcessor chunkPostProcessor) {

        return document
            .getContent()
            //TODO: Make the chunking work on true streaming data
            .reduce("", (a, b) -> a + b)
            .flatMapMany(doc -> {
                List<Chunk> chunks = chunkDocument(
                    chunkEndConditions,
                    overlapCondition,
                    trivialChunkFilter,
                    chunkPostProcessor,
                    doc);

                return Flux.fromIterable(chunks);
            });
    }

    private static List<Chunk> chunkDocument(List<ChunkEndCondition> chunkEndConditions,
        OverlapCondition overlapCondition, TrivialChunkFilter trivialChunkFilter,
        ChunkPostProcessor chunkPostProcessor, String doc) {
        List<Chunk> chunks = new ArrayList<>();

        int previousChunkEndIndex = -1;

        while (doc != null && doc.length() > 0) {

            String finalDoc = doc;

            Optional<Integer> index = chunkEndConditions
                .stream()
                .map(condition -> condition.getEndOfNextChunk(finalDoc))
                .filter(i -> i != -1)
                .min(Integer::compareTo);

            if (index.isPresent()) {
                String chunkText = doc.substring(0, index.get());

                if (chunkText.length() <= previousChunkEndIndex) {
                    LOGGER.warn(
                        "This entier chunk consists of overlapped data, this will result in infinite loop. Skipping this chunk.");

                    // previous chunk should already contain this text..skip it
                    doc = doc.substring(previousChunkEndIndex, doc.length());

                    previousChunkEndIndex = 0;
                    continue;
                }

                int overlapIndex = overlapCondition.getOverlapIndex(chunkText);
                previousChunkEndIndex = chunkText.length() - overlapIndex;
                doc = doc.substring(overlapIndex, doc.length());

                chunks.add(new Chunk(chunkText));
            } else {
                chunks.add(new Chunk(doc));
                break;
            }
        }

        chunks = mergeTrivialChunks(chunks, trivialChunkFilter);
        chunks = tidyChunks(chunks, chunkPostProcessor);
        return chunks;
    }

    /**
     * Tidies up the chunks by applying the post processor.
     *
     * @param chunks             the list of chunks to tidy
     * @param chunkPostProcessor the post processor to apply
     * @return the list of tidied chunks
     */
    private static List<Chunk> tidyChunks(List<Chunk> chunks,
        ChunkPostProcessor chunkPostProcessor) {
        return chunks
            .stream()
            .map(chunkPostProcessor::process)
            .toList();
    }

    /**
     * Merges chunks considered trivial into the previous chunk.
     *
     * @param chunks             the list of chunks to merge
     * @param trivialChunkFilter the filter to determine if a chunk is trivial
     * @return the list of chunks with trivial chunks merged
     */
    private static List<Chunk> mergeTrivialChunks(List<Chunk> chunks,
        TrivialChunkFilter trivialChunkFilter) {

        List<Chunk> result = new ArrayList<>();
        Chunk current = null;
        for (int i = 0; i < chunks.size(); i++) {
            if (current == null) {
                current = chunks.get(i);
            } else {
                current = new Chunk(
                    current.getContents() + chunks.get(i).getContents());
            }

            if (!trivialChunkFilter.isTrivialChunk(current.getContents())) {
                result.add(current);
                current = null;
            }
        }

        if (current != null) {
            Chunk last = result.remove(result.size() - 1);
            result.add(new Chunk(last.getContents() + current.getContents()));
        }

        return result;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private List<ChunkEndCondition> chunkEndConditions = new ArrayList<>();
        private OverlapCondition overlapCondition = new NoOverlapCondition();
        private TrivialChunkFilter trivialChunkFilter = new WhiteSpaceFilter(10);
        private ChunkPostProcessor chunkPostProcessor;

        /**
         * Splits the document into chunks based on the number of sentences.
         *
         * @param maxSentencesPerChunk the number of sentences per chunk
         * @return the builder
         */
        public Builder maxSentencesPerChunk(int maxSentencesPerChunk) {
            return addChunkEndCondition(
                new CountSplitCondition(maxSentencesPerChunk, new SentenceSplitter()));
        }

        /**
         * Splits the document into chunks based on the number of words.
         *
         * @param maxWordsPerChunk the number of words per chunk
         * @return the builder
         */
        public Builder maxWordsPerChunk(int maxWordsPerChunk) {
            return addChunkEndCondition(
                new CountSplitCondition(maxWordsPerChunk, new WordSplitter()));
        }

        /**
         * Splits the document into chunks based on the number of lines.
         *
         * @param maxLinesPerChunk the number of lines per chunk
         * @return the builder
         */
        public Builder maxLinesPerChunk(int maxLinesPerChunk) {
            return addChunkEndCondition(
                new CountSplitCondition(maxLinesPerChunk, new NewLineSplitter()));
        }

        /**
         * Splits the document into chunks based on the number of paragraphs.
         * <p>
         * NOTE: The ParagraphSplitter is not perfect, see {@link ParagraphSplitter} for more
         * information.
         *
         * @param maxParagraphsPerChunk the number of paragraphs per chunk
         * @return the builder
         */
        public Builder maxParagraphsPerChunk(int maxParagraphsPerChunk) {
            return addChunkEndCondition(
                new CountSplitCondition(maxParagraphsPerChunk, new ParagraphSplitter()));
        }

        /**
         * Overlaps chunks by the given number of lines.
         *
         * @param overlap the number of characters to overlap
         * @return the builder
         */
        public Builder overlapNLines(int overlap) {
            return setOverlapCondition(new CountOverlapCondition(overlap, new NewLineSplitter()));
        }

        /**
         * Overlaps chunks by the given number of sentences.
         *
         * @param overlap the number of sentences to overlap
         * @return the builder
         */
        public Builder overlapNSentences(int overlap) {
            return setOverlapCondition(new CountOverlapCondition(overlap, new SentenceSplitter()));
        }

        /**
         * Overlaps chunks by the given number of words.
         *
         * @param overlap the number of words to overlap
         * @return the builder
         */
        public Builder overlapNWords(int overlap) {
            return setOverlapCondition(new CountOverlapCondition(overlap, new WordSplitter()));
        }

        /**
         * Overlaps chunks by the given percentage. Percentage is calculated based on the number of
         * characters in the chunk. Will split at the beginning of the word that gives the required
         * percentage.
         *
         * @param overlap the percentage overlap
         * @return the builder
         */
        public Builder overlapNPercent(float overlap) {
            return setOverlapCondition(new PercentageOverlapCondition(overlap, new WordSplitter()));
        }

        /**
         * Merges chunks that are less than the given character count.
         *
         * @param length the length of the chunk
         * @return the builder
         */
        public Builder mergeChunksLessThanCharCount(int length) {
            return setTrivialSplitFilter(new WhiteSpaceFilter(length));
        }

        /**
         * Trims whitespace from all chunks.
         *
         * @return the builder
         */
        public Builder trimWhitespace() {
            return setChunkPostProcessor(new RemoveWhitespace());
        }

        /**
         * Adds a chunk post processor to the builder. This is used to process the chunk after it
         * has been split. For example, to remove unwanted whitespace.
         *
         * @param chunkPostProcessor the post processor to add
         * @return the builder
         */
        public Builder setChunkPostProcessor(ChunkPostProcessor chunkPostProcessor) {
            this.chunkPostProcessor = chunkPostProcessor;
            return this;
        }

        /**
         * Adds a page end condition to the builder. These are applied as OR conditions, i.e the
         * page will be the size of the SMALLEST condition.
         *
         * @param chunkEndCondition
         * @return
         */
        public Builder addChunkEndCondition(ChunkEndCondition chunkEndCondition) {
            chunkEndConditions.add(chunkEndCondition);
            return this;
        }

        /**
         * Adds an overlap condition to the builder. This condition is used to determine the overlap
         * between chunks.
         *
         * @param overlapCondition
         * @return the builder
         */
        public Builder setOverlapCondition(OverlapCondition overlapCondition) {
            this.overlapCondition = overlapCondition;
            return this;
        }

        /**
         * Adds a trivial split filter to the builder. Pages that match this filter will be merged
         * into the previous chunk.
         *
         * @param trivialChunkFilter
         * @return the builder
         */
        public Builder setTrivialSplitFilter(TrivialChunkFilter trivialChunkFilter) {
            this.trivialChunkFilter = trivialChunkFilter;
            return this;
        }

        public Splitter build() {
            if (chunkEndConditions.size() == 0) {
                throw new SKException(
                    "At least one chunk end condition must be provided");
            }
            return new Splitter(
                chunkEndConditions,
                overlapCondition,
                trivialChunkFilter,
                chunkPostProcessor);
        }

    }
}
