// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.samples.syntaxexamples.memory;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.KeyCredential;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.MetricsOptions;
import com.azure.core.util.TracingOptions;
import com.azure.search.documents.indexes.SearchIndexAsyncClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.semantickernel.aiservices.openai.textembedding.OpenAITextEmbeddingGenerationService;
import com.microsoft.semantickernel.connectors.data.azureaisearch.AzureAISearchVectorStoreOptions;
import com.microsoft.semantickernel.connectors.data.azureaisearch.AzureAISearchVectorStoreRecordCollection;
import com.microsoft.semantickernel.connectors.data.azureaisearch.AzureAISearchVectorStoreRecordCollectionOptions;
import com.microsoft.semantickernel.data.VectorSearchResult;
import com.microsoft.semantickernel.data.record.attributes.VectorStoreRecordDataAttribute;
import com.microsoft.semantickernel.data.record.attributes.VectorStoreRecordKeyAttribute;
import com.microsoft.semantickernel.data.record.attributes.VectorStoreRecordVectorAttribute;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class AzureAISearchVectorStore {

    private static final String CLIENT_KEY = System.getenv("CLIENT_KEY");
    private static final String AZURE_CLIENT_KEY = System.getenv("AZURE_CLIENT_KEY");

    // Only required if AZURE_CLIENT_KEY is set
    private static final String CLIENT_ENDPOINT = System.getenv("CLIENT_ENDPOINT");

    //////////////////////////////////////////////////////////////
    // Azure AI Search configuration
    //////////////////////////////////////////////////////////////
    private static final String AZURE_AI_SEARCH_ENDPOINT = System.getenv("AZURE_AISEARCH_ENDPOINT");
    private static final String AZURE_AISEARCH_KEY = System.getenv("AZURE_AISEARCH_KEY");
    private static final String MODEL_ID = System.getenv()
        .getOrDefault("EMBEDDING_MODEL_ID", "text-embedding-3-large");
    private static final int EMBEDDING_DIMENSIONS = 1536;

    static class GitHubFile {
        @JsonProperty("fileId") // Set a different name for the storage field if needed
        @VectorStoreRecordKeyAttribute()
        private final String id;
        @VectorStoreRecordDataAttribute()
        private final String description;
        @VectorStoreRecordDataAttribute
        private final String link;
        @VectorStoreRecordVectorAttribute(dimensions = EMBEDDING_DIMENSIONS, indexKind = "Hnsw")
        private final List<Float> embedding;

        public GitHubFile() {
            this(null, null, null, Collections.emptyList());
        }

        public GitHubFile(
            @JsonProperty("fileId") String id,
            @JsonProperty("description") String description,
            @JsonProperty("link") String link,
            @JsonProperty("embedding") List<Float> embedding) {
            this.id = id;
            this.description = description;
            this.link = link;
            this.embedding = embedding;
        }

        static String encodeId(String realId) {
            byte[] bytes = Base64.getUrlEncoder().encode(realId.getBytes(StandardCharsets.UTF_8));
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }

    public static void main(String[] args) {
        System.out.println("==============================================================");
        System.out.println("========== Azure AI Search Vector Store Example ==============");
        System.out.println("==============================================================");

        OpenAIAsyncClient client;

        if (AZURE_CLIENT_KEY != null) {
            client = new OpenAIClientBuilder()
                .credential(new AzureKeyCredential(AZURE_CLIENT_KEY))
                .endpoint(CLIENT_ENDPOINT)
                .buildAsyncClient();

        } else {
            client = new OpenAIClientBuilder()
                .credential(new KeyCredential(CLIENT_KEY))
                .buildAsyncClient();
        }

        var embeddingGeneration = OpenAITextEmbeddingGenerationService.builder()
            .withOpenAIAsyncClient(client)
            .withModelId(MODEL_ID)
            .withDimensions(EMBEDDING_DIMENSIONS)
            .build();

        var searchClient = new SearchIndexClientBuilder()
            .endpoint(AZURE_AI_SEARCH_ENDPOINT)
            .credential(new AzureKeyCredential(AZURE_AISEARCH_KEY))
            .clientOptions(clientOptions())
            .buildAsyncClient();

        dataStorageWithAzureAISearch(searchClient, embeddingGeneration);
    }

    public static void dataStorageWithAzureAISearch(
        SearchIndexAsyncClient searchClient,
        OpenAITextEmbeddingGenerationService embeddingGeneration) {

        // Create a new Azure AI Search vector store
        var azureAISearchVectorStore = com.microsoft.semantickernel.connectors.data.azureaisearch.AzureAISearchVectorStore.builder()
            .withSearchIndexAsyncClient(searchClient)
            .withOptions(new AzureAISearchVectorStoreOptions())
            .build();

        String collectionName = "skgithubfiles";
        var collection = (AzureAISearchVectorStoreRecordCollection<GitHubFile>) azureAISearchVectorStore.getCollection(
            collectionName,
            AzureAISearchVectorStoreRecordCollectionOptions.<GitHubFile>builder()
                .withRecordClass(GitHubFile.class)
                .build());

        // Create collection if it does not exist and store data
        collection
            .createCollectionIfNotExistsAsync()
            .then(storeData(collection, embeddingGeneration, sampleData()))
            .block();

        // Search for results
        // Might need to wait for the data to be indexed
        var results = search("How to get started", collection, embeddingGeneration).block();
        var searchResult = results.get(0);
        System.out.printf("Search result with score: %f.%n Link: %s, Description: %s%n",
                searchResult.getScore(), searchResult.getRecord().link, searchResult.getRecord().description);
    }


    private static Mono<List<VectorSearchResult<GitHubFile>>> search(
            String searchText,
            AzureAISearchVectorStoreRecordCollection<GitHubFile> recordCollection,
            OpenAITextEmbeddingGenerationService embeddingGeneration) {

        return embeddingGeneration.generateEmbeddingsAsync(Collections.singletonList(searchText))
                .flatMap(r -> recordCollection.searchAsync("How to get started", null));
    }

    private static Mono<List<String>> storeData(
        AzureAISearchVectorStoreRecordCollection<GitHubFile> recordCollection,
        OpenAITextEmbeddingGenerationService embeddingGeneration,
        Map<String, String> data) {

        return Flux.fromIterable(data.entrySet())
            .flatMap(entry -> {
                System.out.println("Save '" + entry.getKey() + "' to memory.");

                return embeddingGeneration
                    .generateEmbeddingsAsync(Collections.singletonList(entry.getValue()))
                    .flatMap(embeddings -> {
                        GitHubFile gitHubFile = new GitHubFile(
                            GitHubFile.encodeId(entry.getKey()),
                            entry.getValue(),
                            entry.getKey(),
                            embeddings.get(0).getVector());
                        return recordCollection.upsertAsync(gitHubFile, null);
                    });
            })
            .collectList();
    }

    private static Map<String, String> sampleData() {
        return Arrays.stream(new String[][] {
                { "https://github.com/microsoft/semantic-kernel/blob/main/README.md",
                        "README: Installation, getting started with Semantic Kernel, and how to contribute" },
                { "https://github.com/microsoft/semantic-kernel/blob/main/samples/notebooks/dotnet/02-running-prompts-from-file.ipynb",
                        "Jupyter notebook describing how to pass prompts from a file to a semantic skill or function" },
                { "https://github.com/microsoft/semantic-kernel/tree/main/samples/skills/ChatSkill/ChatGPT",
                        "Sample demonstrating how to create a chat skill interfacing with ChatGPT" },
                { "https://github.com/microsoft/semantic-kernel/blob/main/dotnet/src/SemanticKernel/Memory/VolatileMemoryStore.cs",
                        "C# class that defines a volatile embedding store" },
                { "https://github.com/microsoft/semantic-kernel/blob/main/samples/dotnet/KernelHttpServer/README.md",
                        "README: How to set up a Semantic Kernel Service API using Azure Function Runtime v4" },
                { "https://github.com/microsoft/semantic-kernel/blob/main/samples/apps/chat-summary-webapp-react/README.md",
                        "README: README associated with a sample chat summary react-based webapp" },
        }).collect(Collectors.toMap(element -> element[0], element -> element[1]));
    }

    private static ClientOptions clientOptions() {
        return new ClientOptions()
            .setTracingOptions(new TracingOptions())
            .setMetricsOptions(new MetricsOptions())
            .setApplicationId("Semantic-Kernel");
    }
}
