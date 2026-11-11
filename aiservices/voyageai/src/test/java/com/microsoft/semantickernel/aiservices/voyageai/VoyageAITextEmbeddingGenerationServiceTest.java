// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.aiservices.voyageai;

import com.microsoft.semantickernel.aiservices.voyageai.core.VoyageAIClient;
import com.microsoft.semantickernel.aiservices.voyageai.core.VoyageAIModels;
import com.microsoft.semantickernel.aiservices.voyageai.textembedding.VoyageAITextEmbeddingGenerationService;
import com.microsoft.semantickernel.services.textembedding.Embedding;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class VoyageAITextEmbeddingGenerationServiceTest {

    @Test
    public void testGenerateEmbedding() {
        VoyageAIClient mockClient = Mockito.mock(VoyageAIClient.class);

        VoyageAIModels.EmbeddingResponse mockResponse = new VoyageAIModels.EmbeddingResponse();
        VoyageAIModels.EmbeddingDataItem item = new VoyageAIModels.EmbeddingDataItem();
        item.setEmbedding(new float[]{0.1f, 0.2f, 0.3f});
        item.setIndex(0);
        mockResponse.setData(Arrays.asList(item));

        VoyageAIModels.EmbeddingUsage usage = new VoyageAIModels.EmbeddingUsage();
        usage.setTotalTokens(10);
        mockResponse.setUsage(usage);

        when(mockClient.sendRequestAsync(
            eq("embeddings"),
            any(),
            eq(VoyageAIModels.EmbeddingResponse.class)))
            .thenReturn(Mono.just(mockResponse));

        VoyageAITextEmbeddingGenerationService service =
            new VoyageAITextEmbeddingGenerationService(mockClient, "voyage-3-large", null);

        Embedding result = service.generateEmbeddingAsync("test text").block();

        assertNotNull(result);
        List<Float> expected = Arrays.asList(0.1f, 0.2f, 0.3f);
        assertEquals(expected, result.getVector());
    }

    @Test
    public void testGenerateMultipleEmbeddings() {
        VoyageAIClient mockClient = Mockito.mock(VoyageAIClient.class);

        VoyageAIModels.EmbeddingResponse mockResponse = new VoyageAIModels.EmbeddingResponse();

        VoyageAIModels.EmbeddingDataItem item1 = new VoyageAIModels.EmbeddingDataItem();
        item1.setEmbedding(new float[]{0.1f, 0.2f});
        item1.setIndex(0);

        VoyageAIModels.EmbeddingDataItem item2 = new VoyageAIModels.EmbeddingDataItem();
        item2.setEmbedding(new float[]{0.3f, 0.4f});
        item2.setIndex(1);

        mockResponse.setData(Arrays.asList(item1, item2));

        VoyageAIModels.EmbeddingUsage usage = new VoyageAIModels.EmbeddingUsage();
        usage.setTotalTokens(20);
        mockResponse.setUsage(usage);

        when(mockClient.sendRequestAsync(
            eq("embeddings"),
            any(),
            eq(VoyageAIModels.EmbeddingResponse.class)))
            .thenReturn(Mono.just(mockResponse));

        VoyageAITextEmbeddingGenerationService service =
            new VoyageAITextEmbeddingGenerationService(mockClient, "voyage-3-large", null);

        List<Embedding> results = service.generateEmbeddingsAsync(
            Arrays.asList("text1", "text2")).block();

        assertNotNull(results);
        assertEquals(2, results.size());
        List<Float> expected1 = Arrays.asList(0.1f, 0.2f);
        List<Float> expected2 = Arrays.asList(0.3f, 0.4f);
        assertEquals(expected1, results.get(0).getVector());
        assertEquals(expected2, results.get(1).getVector());
    }

    @Test
    public void testServiceIdAndModelId() {
        VoyageAIClient mockClient = Mockito.mock(VoyageAIClient.class);

        VoyageAITextEmbeddingGenerationService service =
            new VoyageAITextEmbeddingGenerationService(mockClient, "voyage-3-large", "test-service");

        assertEquals("test-service", service.getServiceId());
        assertEquals("voyage-3-large", service.getModelId());
    }

    @Test
    public void testBuilderPattern() {
        VoyageAIClient mockClient = Mockito.mock(VoyageAIClient.class);

        VoyageAITextEmbeddingGenerationService service =
            VoyageAITextEmbeddingGenerationService.builder()
                .withClient(mockClient)
                .withModelId("voyage-3-large")
                .withServiceId("test-service")
                .build();

        assertNotNull(service);
        assertEquals("test-service", service.getServiceId());
        assertEquals("voyage-3-large", service.getModelId());
    }

    @Test
    public void testNullClientThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
            new VoyageAITextEmbeddingGenerationService(null, "voyage-3-large", null));
    }

    @Test
    public void testNullModelIdThrowsException() {
        VoyageAIClient mockClient = Mockito.mock(VoyageAIClient.class);
        assertThrows(IllegalArgumentException.class, () ->
            new VoyageAITextEmbeddingGenerationService(mockClient, null, null));
    }
}
