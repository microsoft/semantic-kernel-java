// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.aiservices.voyageai;

import com.microsoft.semantickernel.aiservices.voyageai.contextualizedembedding.VoyageAIContextualizedEmbeddingGenerationService;
import com.microsoft.semantickernel.aiservices.voyageai.core.VoyageAIClient;
import com.microsoft.semantickernel.aiservices.voyageai.core.VoyageAIModels;
import com.microsoft.semantickernel.services.textembedding.Embedding;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class VoyageAIContextualizedEmbeddingGenerationServiceTest {

    @Test
    public void testGenerateContextualizedEmbeddings() {
        VoyageAIClient mockClient = Mockito.mock(VoyageAIClient.class);

        VoyageAIModels.ContextualizedEmbeddingResponse mockResponse =
            new VoyageAIModels.ContextualizedEmbeddingResponse();

        VoyageAIModels.EmbeddingDataItem item1 = new VoyageAIModels.EmbeddingDataItem();
        item1.setEmbedding(new float[]{0.1f, 0.2f});
        item1.setIndex(0);

        VoyageAIModels.EmbeddingDataItem item2 = new VoyageAIModels.EmbeddingDataItem();
        item2.setEmbedding(new float[]{0.3f, 0.4f});
        item2.setIndex(0);

        VoyageAIModels.ContextualizedEmbeddingDataList dataList1 =
            new VoyageAIModels.ContextualizedEmbeddingDataList();
        dataList1.setData(Arrays.asList(item1));

        VoyageAIModels.ContextualizedEmbeddingDataList dataList2 =
            new VoyageAIModels.ContextualizedEmbeddingDataList();
        dataList2.setData(Arrays.asList(item2));

        mockResponse.setData(Arrays.asList(dataList1, dataList2));

        when(mockClient.sendRequestAsync(
            eq("contextualizedembeddings"),
            any(),
            eq(VoyageAIModels.ContextualizedEmbeddingResponse.class)))
            .thenReturn(Mono.just(mockResponse));

        VoyageAIContextualizedEmbeddingGenerationService service =
            new VoyageAIContextualizedEmbeddingGenerationService(mockClient, "voyage-3", null);

        List<List<String>> inputs = Arrays.asList(
            Arrays.asList("chunk1"),
            Arrays.asList("chunk2")
        );

        List<Embedding> results = service.generateContextualizedEmbeddingsAsync(inputs).block();

        assertNotNull(results);
        assertEquals(2, results.size());
        List<Float> expected1 = Arrays.asList(0.1f, 0.2f);
        List<Float> expected2 = Arrays.asList(0.3f, 0.4f);
        assertEquals(expected1, results.get(0).getVector());
        assertEquals(expected2, results.get(1).getVector());
    }

    @Test
    public void testGenerateEmbedding() {
        VoyageAIClient mockClient = Mockito.mock(VoyageAIClient.class);

        VoyageAIModels.ContextualizedEmbeddingResponse mockResponse =
            new VoyageAIModels.ContextualizedEmbeddingResponse();

        VoyageAIModels.EmbeddingDataItem item = new VoyageAIModels.EmbeddingDataItem();
        item.setEmbedding(new float[]{0.1f, 0.2f, 0.3f});
        item.setIndex(0);

        VoyageAIModels.ContextualizedEmbeddingDataList dataList =
            new VoyageAIModels.ContextualizedEmbeddingDataList();
        dataList.setData(Arrays.asList(item));

        mockResponse.setData(Arrays.asList(dataList));

        when(mockClient.sendRequestAsync(
            eq("contextualizedembeddings"),
            any(),
            eq(VoyageAIModels.ContextualizedEmbeddingResponse.class)))
            .thenReturn(Mono.just(mockResponse));

        VoyageAIContextualizedEmbeddingGenerationService service =
            new VoyageAIContextualizedEmbeddingGenerationService(mockClient, "voyage-3", null);

        Embedding result2 = service.generateEmbeddingAsync("test text").block();

        assertNotNull(result2);
        List<Float> expected = Arrays.asList(0.1f, 0.2f, 0.3f);
        assertEquals(expected, result2.getVector());
    }

    @Test
    public void testServiceIdAndModelId() {
        VoyageAIClient mockClient = Mockito.mock(VoyageAIClient.class);

        VoyageAIContextualizedEmbeddingGenerationService service =
            new VoyageAIContextualizedEmbeddingGenerationService(mockClient, "voyage-3", "test-service");

        assertEquals("test-service", service.getServiceId());
        assertEquals("voyage-3", service.getModelId());
    }

    @Test
    public void testBuilderPattern() {
        VoyageAIClient mockClient = Mockito.mock(VoyageAIClient.class);

        VoyageAIContextualizedEmbeddingGenerationService service =
            VoyageAIContextualizedEmbeddingGenerationService.builder()
                .withClient(mockClient)
                .withModelId("voyage-3")
                .withServiceId("test-service")
                .build();

        assertNotNull(service);
        assertEquals("test-service", service.getServiceId());
        assertEquals("voyage-3", service.getModelId());
    }

    @Test
    public void testNullClientThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
            new VoyageAIContextualizedEmbeddingGenerationService(null, "voyage-3", null));
    }

    @Test
    public void testNullModelIdThrowsException() {
        VoyageAIClient mockClient = Mockito.mock(VoyageAIClient.class);
        assertThrows(IllegalArgumentException.class, () ->
            new VoyageAIContextualizedEmbeddingGenerationService(mockClient, null, null));
    }
}
