// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.aiservices.voyageai;

import com.microsoft.semantickernel.aiservices.voyageai.contextualizedembedding.VoyageAIContextualizedEmbeddingGenerationService;
import com.microsoft.semantickernel.aiservices.voyageai.core.VoyageAIClient;
import com.microsoft.semantickernel.aiservices.voyageai.multimodalembedding.VoyageAIMultimodalEmbeddingGenerationService;
import com.microsoft.semantickernel.aiservices.voyageai.reranking.VoyageAITextRerankingService;
import com.microsoft.semantickernel.aiservices.voyageai.textembedding.VoyageAITextEmbeddingGenerationService;
import com.microsoft.semantickernel.services.reranking.RerankResult;
import com.microsoft.semantickernel.services.textembedding.Embedding;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for VoyageAI services.
 * Requires VOYAGE_API_KEY environment variable to be set.
 */
public class VoyageAIIntegrationTest {

    private static final String API_KEY_ENV_VAR = "VOYAGE_API_KEY";
    private static final String DEFAULT_EMBEDDING_MODEL = "voyage-3-large";
    private static final String DEFAULT_CONTEXTUALIZED_MODEL = "voyage-context-3";
    private static final String DEFAULT_MULTIMODAL_MODEL = "voyage-multimodal-3";
    private static final String DEFAULT_RERANK_MODEL = "rerank-2";

    private String apiKey;

    @BeforeEach
    public void setUp() {
        apiKey = System.getenv(API_KEY_ENV_VAR);
        Assumptions.assumeTrue(
            apiKey != null && !apiKey.isEmpty(),
            "Skipping integration test: " + API_KEY_ENV_VAR + " environment variable not set"
        );
    }

    @Test
    public void testTextEmbeddingGeneration() {
        VoyageAIClient client = new VoyageAIClient(apiKey);
        VoyageAITextEmbeddingGenerationService service =
            VoyageAITextEmbeddingGenerationService.builder()
                .withClient(client)
                .withModelId(DEFAULT_EMBEDDING_MODEL)
                .build();

        Embedding embedding = service.generateEmbeddingAsync("Hello, world!").block();

        assertNotNull(embedding, "Embedding should not be null");
        assertNotNull(embedding.getVector(), "Embedding vector should not be null");
        assertTrue(embedding.getVector().size() > 0, "Embedding vector should not be empty");

        System.out.println("Generated embedding with dimension: " + embedding.getVector().size());
    }

    @Test
    public void testMultipleTextEmbeddings() {
        VoyageAIClient client = new VoyageAIClient(apiKey);
        VoyageAITextEmbeddingGenerationService service =
            VoyageAITextEmbeddingGenerationService.builder()
                .withClient(client)
                .withModelId(DEFAULT_EMBEDDING_MODEL)
                .build();

        List<String> texts = Arrays.asList(
            "Hello, world!",
            "Semantic Kernel is awesome",
            "VoyageAI provides great embeddings"
        );

        List<Embedding> embeddings = service.generateEmbeddingsAsync(texts).block();

        assertNotNull(embeddings, "Embeddings should not be null");
        assertEquals(3, embeddings.size(), "Should generate 3 embeddings");

        for (Embedding embedding : embeddings) {
            assertNotNull(embedding.getVector(), "Each embedding vector should not be null");
            assertTrue(embedding.getVector().size() > 0, "Each embedding vector should not be empty");
        }

        System.out.println("Generated " + embeddings.size() + " embeddings");
    }

    @Test
    public void testTextReranking() {
        VoyageAIClient client = new VoyageAIClient(apiKey);
        VoyageAITextRerankingService service =
            VoyageAITextRerankingService.builder()
                .withClient(client)
                .withModelId(DEFAULT_RERANK_MODEL)
                .build();

        String query = "What is the capital of France?";
        List<String> documents = Arrays.asList(
            "Paris is the capital and most populous city of France.",
            "Berlin is the capital of Germany.",
            "The Eiffel Tower is located in Paris.",
            "London is the capital of the United Kingdom."
        );

        List<RerankResult> results = service.rerankAsync(query, documents).block();

        assertNotNull(results, "Rerank results should not be null");
        assertEquals(4, results.size(), "Should have 4 reranked results");

        // The first result should have the highest relevance score
        assertTrue(results.get(0).getRelevanceScore() >= results.get(1).getRelevanceScore(),
            "Results should be sorted by relevance score descending");

        System.out.println("Reranking results:");
        for (int i = 0; i < results.size(); i++) {
            RerankResult result = results.get(i);
            System.out.printf("%d. [Index: %d, Score: %.4f] %s%n",
                i + 1, result.getIndex(), result.getRelevanceScore(), result.getText());
        }

        // The most relevant document should be about Paris being the capital
        assertEquals(0, results.get(0).getIndex(),
            "Most relevant document should be the one about Paris being the capital");
    }

    @Test
    public void testRerankingWithTopK() {
        VoyageAIClient client = new VoyageAIClient(apiKey);
        VoyageAITextRerankingService service =
            VoyageAITextRerankingService.builder()
                .withClient(client)
                .withModelId(DEFAULT_RERANK_MODEL)
                .withTopK(2)
                .build();

        String query = "Machine learning";
        List<String> documents = Arrays.asList(
            "Machine learning is a subset of artificial intelligence.",
            "Cooking is an art form.",
            "Deep learning uses neural networks.",
            "The weather is nice today."
        );

        List<RerankResult> results = service.rerankAsync(query, documents).block();

        assertNotNull(results, "Rerank results should not be null");
        // VoyageAI might return all results sorted, or just top K
        assertTrue(results.size() >= 2, "Should have at least 2 results");

        System.out.println("Top K reranking results:");
        for (RerankResult result : results) {
            System.out.printf("[Index: %d, Score: %.4f] %s%n",
                result.getIndex(), result.getRelevanceScore(), result.getText());
        }
    }

    @Test
    public void testContextualizedEmbeddings() {
        VoyageAIClient client = new VoyageAIClient(apiKey);
        VoyageAIContextualizedEmbeddingGenerationService service =
            VoyageAIContextualizedEmbeddingGenerationService.builder()
                .withClient(client)
                .withModelId(DEFAULT_CONTEXTUALIZED_MODEL)
                .build();

        // Create document chunks with context
        List<List<String>> inputs = Arrays.asList(
            Arrays.asList("Introduction to semantic kernel", "Semantic kernel is a framework"),
            Arrays.asList("VoyageAI provides embeddings", "VoyageAI is an AI company")
        );

        List<Embedding> embeddings = service.generateContextualizedEmbeddingsAsync(inputs).block();

        assertNotNull(embeddings, "Contextualized embeddings should not be null");
        // Each input document has 2 chunks, so we expect 4 embeddings total (2 documents * 2 chunks each)
        assertEquals(4, embeddings.size(), "Should generate 4 embeddings (2 per document)");

        for (Embedding embedding : embeddings) {
            assertNotNull(embedding.getVector(), "Each embedding vector should not be null");
            assertTrue(embedding.getVector().size() > 0, "Each embedding vector should not be empty");
        }

        System.out.println("Generated " + embeddings.size() + " contextualized embeddings");
    }

    @Test
    public void testMultimodalEmbeddings() {
        VoyageAIClient client = new VoyageAIClient(apiKey);
        VoyageAIMultimodalEmbeddingGenerationService service =
            VoyageAIMultimodalEmbeddingGenerationService.builder()
                .withClient(client)
                .withModelId(DEFAULT_MULTIMODAL_MODEL)
                .build();

        // Test using generateEmbeddingsAsync which handles text conversion
        List<String> texts = Arrays.asList(
            "This is a text description",
            "Another text example"
        );

        List<Embedding> embeddings = service.generateEmbeddingsAsync(texts).block();

        assertNotNull(embeddings, "Multimodal embeddings should not be null");
        assertEquals(2, embeddings.size(), "Should generate 2 embeddings");

        for (Embedding embedding : embeddings) {
            assertNotNull(embedding.getVector(), "Each embedding vector should not be null");
            assertTrue(embedding.getVector().size() > 0, "Each embedding vector should not be empty");
        }

        System.out.println("Generated " + embeddings.size() + " multimodal embeddings");
        System.out.println("Embedding dimension: " + embeddings.get(0).getVector().size());
    }
}
