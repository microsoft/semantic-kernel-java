// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.aiservices.voyageai;

import com.microsoft.semantickernel.aiservices.voyageai.core.VoyageAIClient;
import com.microsoft.semantickernel.aiservices.voyageai.core.VoyageAIModels;
import com.microsoft.semantickernel.aiservices.voyageai.multimodalembedding.VoyageAIMultimodalEmbeddingGenerationService;
import com.microsoft.semantickernel.services.textembedding.Embedding;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class VoyageAIMultimodalEmbeddingGenerationServiceTest {

    @Test
    public void testGenerateMultimodalEmbeddings() {
        VoyageAIClient mockClient = Mockito.mock(VoyageAIClient.class);

        VoyageAIModels.MultimodalEmbeddingResponse mockResponse =
            new VoyageAIModels.MultimodalEmbeddingResponse();

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
            eq("multimodalembeddings"),
            any(),
            eq(VoyageAIModels.MultimodalEmbeddingResponse.class)))
            .thenReturn(Mono.just(mockResponse));

        VoyageAIMultimodalEmbeddingGenerationService service =
            new VoyageAIMultimodalEmbeddingGenerationService(mockClient, "voyage-multimodal-3", null);

        // Create properly structured multimodal inputs
        VoyageAIModels.MultimodalContentItem content1 = new VoyageAIModels.MultimodalContentItem("text", "text1");
        VoyageAIModels.MultimodalContentItem content2 = new VoyageAIModels.MultimodalContentItem("text", "text2");
        VoyageAIModels.MultimodalInput input1 = new VoyageAIModels.MultimodalInput(Arrays.asList(content1));
        VoyageAIModels.MultimodalInput input2 = new VoyageAIModels.MultimodalInput(Arrays.asList(content2));
        List<VoyageAIModels.MultimodalInput> inputs = Arrays.asList(input1, input2);

        List<Embedding> results = service.generateMultimodalEmbeddingsAsync(inputs).block();

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

        VoyageAIModels.MultimodalEmbeddingResponse mockResponse =
            new VoyageAIModels.MultimodalEmbeddingResponse();

        VoyageAIModels.EmbeddingDataItem item = new VoyageAIModels.EmbeddingDataItem();
        item.setEmbedding(new float[]{0.1f, 0.2f, 0.3f});
        item.setIndex(0);

        mockResponse.setData(Arrays.asList(item));

        VoyageAIModels.EmbeddingUsage usage = new VoyageAIModels.EmbeddingUsage();
        usage.setTotalTokens(10);
        mockResponse.setUsage(usage);

        when(mockClient.sendRequestAsync(
            eq("multimodalembeddings"),
            any(),
            eq(VoyageAIModels.MultimodalEmbeddingResponse.class)))
            .thenReturn(Mono.just(mockResponse));

        VoyageAIMultimodalEmbeddingGenerationService service =
            new VoyageAIMultimodalEmbeddingGenerationService(mockClient, "voyage-multimodal-3", null);

        Embedding result = service.generateEmbeddingAsync("test text").block();

        assertNotNull(result);
        List<Float> expected = Arrays.asList(0.1f, 0.2f, 0.3f);
        assertEquals(expected, result.getVector());
    }

    @Test
    public void testGenerateEmbeddingsFromTextList() {
        VoyageAIClient mockClient = Mockito.mock(VoyageAIClient.class);

        VoyageAIModels.MultimodalEmbeddingResponse mockResponse =
            new VoyageAIModels.MultimodalEmbeddingResponse();

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
            eq("multimodalembeddings"),
            any(),
            eq(VoyageAIModels.MultimodalEmbeddingResponse.class)))
            .thenReturn(Mono.just(mockResponse));

        VoyageAIMultimodalEmbeddingGenerationService service =
            new VoyageAIMultimodalEmbeddingGenerationService(mockClient, "voyage-multimodal-3", null);

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

        VoyageAIMultimodalEmbeddingGenerationService service =
            new VoyageAIMultimodalEmbeddingGenerationService(mockClient, "voyage-multimodal-3", "test-service");

        assertEquals("test-service", service.getServiceId());
        assertEquals("voyage-multimodal-3", service.getModelId());
    }

    @Test
    public void testBuilderPattern() {
        VoyageAIClient mockClient = Mockito.mock(VoyageAIClient.class);

        VoyageAIMultimodalEmbeddingGenerationService service =
            VoyageAIMultimodalEmbeddingGenerationService.builder()
                .withClient(mockClient)
                .withModelId("voyage-multimodal-3")
                .withServiceId("test-service")
                .build();

        assertNotNull(service);
        assertEquals("test-service", service.getServiceId());
        assertEquals("voyage-multimodal-3", service.getModelId());
    }

    @Test
    public void testNullClientThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
            new VoyageAIMultimodalEmbeddingGenerationService(null, "voyage-multimodal-3", null));
    }

    @Test
    public void testNullModelIdThrowsException() {
        VoyageAIClient mockClient = Mockito.mock(VoyageAIClient.class);
        assertThrows(IllegalArgumentException.class, () ->
            new VoyageAIMultimodalEmbeddingGenerationService(mockClient, null, null));
    }

    @Test
    public void testVoyageMultimodal35ModelId() {
        VoyageAIClient mockClient = Mockito.mock(VoyageAIClient.class);

        VoyageAIMultimodalEmbeddingGenerationService service =
            VoyageAIMultimodalEmbeddingGenerationService.builder()
                .withClient(mockClient)
                .withModelId("voyage-multimodal-3.5")
                .withServiceId("multimodal-3.5-service")
                .build();

        assertNotNull(service);
        assertEquals("voyage-multimodal-3.5", service.getModelId());
        assertEquals("multimodal-3.5-service", service.getServiceId());
    }

    @Test
    public void testVoyageMultimodal35GenerateEmbeddings() {
        VoyageAIClient mockClient = Mockito.mock(VoyageAIClient.class);

        VoyageAIModels.MultimodalEmbeddingResponse mockResponse =
            new VoyageAIModels.MultimodalEmbeddingResponse();

        VoyageAIModels.EmbeddingDataItem item = new VoyageAIModels.EmbeddingDataItem();
        item.setEmbedding(new float[]{0.5f, 0.6f, 0.7f, 0.8f});
        item.setIndex(0);

        mockResponse.setData(Arrays.asList(item));

        VoyageAIModels.EmbeddingUsage usage = new VoyageAIModels.EmbeddingUsage();
        usage.setTotalTokens(15);
        mockResponse.setUsage(usage);

        when(mockClient.sendRequestAsync(
            eq("multimodalembeddings"),
            any(),
            eq(VoyageAIModels.MultimodalEmbeddingResponse.class)))
            .thenReturn(Mono.just(mockResponse));

        VoyageAIMultimodalEmbeddingGenerationService service =
            new VoyageAIMultimodalEmbeddingGenerationService(mockClient, "voyage-multimodal-3.5", null);

        Embedding result = service.generateEmbeddingAsync("test with voyage-multimodal-3.5").block();

        assertNotNull(result);
        List<Float> expected = Arrays.asList(0.5f, 0.6f, 0.7f, 0.8f);
        assertEquals(expected, result.getVector());
    }
}
