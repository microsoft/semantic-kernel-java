// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.aiservices.voyageai;

import com.microsoft.semantickernel.aiservices.voyageai.core.VoyageAIClient;
import com.microsoft.semantickernel.aiservices.voyageai.core.VoyageAIModels;
import com.microsoft.semantickernel.aiservices.voyageai.reranking.VoyageAITextRerankingService;
import com.microsoft.semantickernel.services.reranking.RerankResult;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class VoyageAITextRerankingServiceTest {

    @Test
    public void testRerank() {
        VoyageAIClient mockClient = Mockito.mock(VoyageAIClient.class);

        VoyageAIModels.RerankResponse mockResponse = new VoyageAIModels.RerankResponse();

        VoyageAIModels.RerankDataItem item1 = new VoyageAIModels.RerankDataItem();
        item1.setIndex(1);
        item1.setRelevanceScore(0.9);

        VoyageAIModels.RerankDataItem item2 = new VoyageAIModels.RerankDataItem();
        item2.setIndex(0);
        item2.setRelevanceScore(0.5);

        mockResponse.setData(Arrays.asList(item1, item2));

        VoyageAIModels.EmbeddingUsage usage = new VoyageAIModels.EmbeddingUsage();
        usage.setTotalTokens(20);
        mockResponse.setUsage(usage);

        when(mockClient.sendRequestAsync(
            eq("rerank"),
            any(),
            eq(VoyageAIModels.RerankResponse.class)))
            .thenReturn(Mono.just(mockResponse));

        VoyageAITextRerankingService service =
            new VoyageAITextRerankingService(mockClient, "rerank-2", null, null);

        List<String> documents = Arrays.asList("Document A", "Document B");
        List<RerankResult> results = service.rerankAsync("test query", documents).block();

        assertNotNull(results);
        assertEquals(2, results.size());

        // Results should be sorted by relevance score descending
        assertEquals(1, results.get(0).getIndex());
        assertEquals("Document B", results.get(0).getText());
        assertEquals(0.9, results.get(0).getRelevanceScore(), 0.001);

        assertEquals(0, results.get(1).getIndex());
        assertEquals("Document A", results.get(1).getText());
        assertEquals(0.5, results.get(1).getRelevanceScore(), 0.001);
    }

    @Test
    public void testServiceIdAndModelId() {
        VoyageAIClient mockClient = Mockito.mock(VoyageAIClient.class);

        VoyageAITextRerankingService service =
            new VoyageAITextRerankingService(mockClient, "rerank-2", "test-service", null);

        assertEquals("test-service", service.getServiceId());
        assertEquals("rerank-2", service.getModelId());
    }

    @Test
    public void testBuilderPattern() {
        VoyageAIClient mockClient = Mockito.mock(VoyageAIClient.class);

        VoyageAITextRerankingService service =
            VoyageAITextRerankingService.builder()
                .withClient(mockClient)
                .withModelId("rerank-2")
                .withServiceId("test-service")
                .withTopK(5)
                .build();

        assertNotNull(service);
        assertEquals("test-service", service.getServiceId());
        assertEquals("rerank-2", service.getModelId());
    }

    @Test
    public void testNullClientThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
            new VoyageAITextRerankingService(null, "rerank-2", null, null));
    }

    @Test
    public void testNullModelIdThrowsException() {
        VoyageAIClient mockClient = Mockito.mock(VoyageAIClient.class);
        assertThrows(IllegalArgumentException.class, () ->
            new VoyageAITextRerankingService(mockClient, null, null, null));
    }

    @Test
    public void testNullQueryThrowsException() {
        VoyageAIClient mockClient = Mockito.mock(VoyageAIClient.class);
        VoyageAITextRerankingService service =
            new VoyageAITextRerankingService(mockClient, "rerank-2", null, null);

        assertThrows(IllegalArgumentException.class, () ->
            service.rerankAsync(null, Arrays.asList("doc")).block());
    }
}
