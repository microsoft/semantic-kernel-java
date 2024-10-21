// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.samples.syntaxexamples.memory;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.KeyCredential;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.MetricsOptions;
import com.azure.core.util.TracingOptions;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.semantickernel.aiservices.openai.textembedding.OpenAITextEmbeddingGenerationService;
import com.microsoft.semantickernel.connectors.data.redis.RedisHashSetVectorStoreRecordCollectionOptions;
import com.microsoft.semantickernel.connectors.data.redis.RedisJsonVectorStoreRecordCollectionOptions;
import com.microsoft.semantickernel.connectors.data.redis.RedisStorageType;
import com.microsoft.semantickernel.connectors.data.redis.RedisVectorStore;
import com.microsoft.semantickernel.connectors.data.redis.RedisVectorStoreOptions;
import com.microsoft.semantickernel.data.vectorsearch.VectorSearchResult;
import com.microsoft.semantickernel.data.vectorstorage.VectorStoreRecordCollection;
import com.microsoft.semantickernel.data.vectorstorage.attributes.VectorStoreRecordDataAttribute;
import com.microsoft.semantickernel.data.vectorstorage.attributes.VectorStoreRecordKeyAttribute;
import com.microsoft.semantickernel.data.vectorstorage.attributes.VectorStoreRecordVectorAttribute;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.microsoft.semantickernel.data.vectorstorage.definition.DistanceFunction;
import com.microsoft.semantickernel.data.vectorstorage.definition.IndexKind;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import redis.clients.jedis.JedisPooled;

public class VectorStoreWithRedis {

    private static final String CLIENT_KEY = System.getenv("CLIENT_KEY");
    private static final String AZURE_CLIENT_KEY = System.getenv("AZURE_CLIENT_KEY");

    // Only required if AZURE_CLIENT_KEY is set
    private static final String CLIENT_ENDPOINT = System.getenv("CLIENT_ENDPOINT");

    private static final String MODEL_ID = System.getenv()
        .getOrDefault("EMBEDDING_MODEL_ID", "text-embedding-3-large");
    private static final int EMBEDDING_DIMENSIONS = 1536;

    public static class GitHubFile {
        @JsonProperty("fileId") // Set a different name for the storage field if needed
        @VectorStoreRecordKeyAttribute()
        private final String id;
        @VectorStoreRecordDataAttribute()
        private final String description;
        @VectorStoreRecordDataAttribute
        private final String link;
        @VectorStoreRecordVectorAttribute(dimensions = EMBEDDING_DIMENSIONS, indexKind = IndexKind.HNSW, distanceFunction = DistanceFunction.COSINE_DISTANCE)
        private final List<Float> embedding;

        public GitHubFile() {
            this(null, null, null, Collections.emptyList());
        }

        public GitHubFile(
            String id,
            String description,
            String link,
            List<Float> embedding) {
            this.id = id;
            this.description = description;
            this.link = link;
            this.embedding = embedding;
        }

        public String getId() {
            return id;
        }

        public String getDescription() {
            return description;
        }

        public String getLink() {
            return link;
        }

        public List<Float> getEmbedding() {
            return embedding;
        }

        static String encodeId(String realId) {
            return VectorStoreWithAzureAISearch.GitHubFile.encodeId(realId);
        }
    }

    // Can start a test server with:
    // docker run -d --name redis-stack -p 6379:6379 -p 8001:8001 redis/redis-stack:latest
    private static final String REDIS_URL = "redis://127.0.0.1:6379";

    public static void main(String[] args) {
        System.out.println("==============================================================");
        System.out.println("================ Redis Vector Store Example ==================");
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

        storeAndSearch(embeddingGeneration);
    }

    public static void storeAndSearch(
        OpenAITextEmbeddingGenerationService embeddingGeneration) {
        // Configure redis client
        JedisPooled jedis = new JedisPooled(REDIS_URL);

        // Build a vector store
        // Available storage types are JSON and HASHSET. Default is JSON.
        var vectorStore = RedisVectorStore.builder()
            .withClient(jedis)
            .withOptions(
                RedisVectorStoreOptions.builder().withStorageType(RedisStorageType.JSON).build())
            .build();

        // Set up the record collection to use
        String collectionName = "skgithubfiles";
        var collection = vectorStore.getCollection(collectionName,
            RedisJsonVectorStoreRecordCollectionOptions.<GitHubFile>builder()
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

        if (results == null || results.isEmpty()) {
            System.out.println("No search results found.");
            return;
        }
        var searchResult = results.get(0);
        System.out.printf("Search result with score: %f.%n Link: %s, Description: %s%n",
            searchResult.getScore(), searchResult.getRecord().link,
            searchResult.getRecord().description);
    }

    private static Mono<List<VectorSearchResult<GitHubFile>>> search(
        String searchText,
        VectorStoreRecordCollection<String, GitHubFile> recordCollection,
        OpenAITextEmbeddingGenerationService embeddingGeneration) {
        // Generate embeddings for the search text and search for the closest records
        return embeddingGeneration.generateEmbeddingsAsync(Collections.singletonList(searchText))
            .flatMap(r -> recordCollection.searchAsync(r.get(0).getVector(), null));
    }

    private static Mono<List<String>> storeData(
        VectorStoreRecordCollection<String, GitHubFile> recordStore,
        OpenAITextEmbeddingGenerationService embeddingGeneration,
        Map<String, String> data) {

        return Flux.fromIterable(data.entrySet())
            .flatMap(entry -> {
                System.out.println("Save '" + entry.getKey() + "' to memory.");

                // Generate embeddings for the data and store it
                return embeddingGeneration
                    .generateEmbeddingsAsync(Collections.singletonList(entry.getValue()))
                    .flatMap(embeddings -> {
                        GitHubFile gitHubFile = new GitHubFile(
                            GitHubFile.encodeId(entry.getKey()),
                            entry.getValue(),
                            entry.getKey(),
                            embeddings.get(0).getVector());
                        return recordStore.upsertAsync(gitHubFile, null);
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
}
