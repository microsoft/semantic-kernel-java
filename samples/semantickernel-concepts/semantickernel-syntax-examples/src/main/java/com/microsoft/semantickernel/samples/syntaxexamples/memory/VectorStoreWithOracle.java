// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.samples.syntaxexamples.memory;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.KeyCredential;
import com.microsoft.semantickernel.aiservices.openai.textembedding.OpenAITextEmbeddingGenerationService;
import com.microsoft.semantickernel.data.jdbc.JDBCVectorStore;
import com.microsoft.semantickernel.data.jdbc.JDBCVectorStoreOptions;
import com.microsoft.semantickernel.data.jdbc.JDBCVectorStoreRecordCollectionOptions;
import com.microsoft.semantickernel.data.jdbc.postgres.PostgreSQLVectorStoreQueryProvider;
import com.microsoft.semantickernel.data.vectorsearch.VectorSearchResults;
import com.microsoft.semantickernel.data.vectorstorage.VectorStoreRecordCollection;
import com.microsoft.semantickernel.data.vectorstorage.annotations.VectorStoreRecordData;
import com.microsoft.semantickernel.data.vectorstorage.annotations.VectorStoreRecordKey;
import com.microsoft.semantickernel.data.vectorstorage.annotations.VectorStoreRecordVector;
import com.microsoft.semantickernel.data.vectorstorage.definition.DistanceFunction;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.postgresql.ds.PGSimpleDataSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class VectorStoreWithOracle {

    static class GitHubFile {
        @VectorStoreRecordKey
        private final String id;
        @VectorStoreRecordData
        private final String description;
        @VectorStoreRecordData
        private final String link;
        @VectorStoreRecordVector(dimensions = EMBEDDING_DIMENSIONS, distanceFunction = DistanceFunction.COSINE_DISTANCE)
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
            byte[] bytes = Base64.getUrlEncoder().encode(realId.getBytes(StandardCharsets.UTF_8));
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }

    // Run a PostgreSQL server with:
    // docker run -d --name pgvector-container -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=root -e POSTGRES_DB=sk -p 5432:5432 pgvector/pgvector:pg17

    public static void main(String[] args) throws SQLException {
        System.out.println("==============================================================");
        System.out.println("============== Oracle Vector Store Example ===================");
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

        // Create an OpenAI text embedding generation service
        var embeddingGeneration = OpenAITextEmbeddingGenerationService.builder()
            .withOpenAIAsyncClient(client)
            .withModelId(MODEL_ID)
            .withDimensions(EMBEDDING_DIMENSIONS)
            .build();

        storeAndSearch(embeddingGeneration);
    }

    public static void storeAndSearch(OpenAITextEmbeddingGenerationService embeddingGeneration) {
        // Configure the data source
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUrl("jdbc:postgresql://localhost:5432/sk");
        dataSource.setUser("postgres");
        dataSource.setPassword("root");

        // Build a query provider
        // Other available query providers are PostgreSQLVectorStoreQueryProvider and SQLiteVectorStoreQueryProvider
        var queryProvider = PostgreSQLVectorStoreQueryProvider.builder()
            .withDataSource(dataSource)
            .build();

        // Build a vector store
        var jdbcVectorStore = JDBCVectorStore.builder()
            .withDataSource(dataSource)
            .withOptions(JDBCVectorStoreOptions.builder()
                .withQueryProvider(queryProvider)
                .build())
            .build();

        // Set up the record collection to use
        String collectionName = "skgithubfiles";
        var collection = jdbcVectorStore.getCollection(collectionName,
            JDBCVectorStoreRecordCollectionOptions.<GitHubFile>builder()
                .withRecordClass(GitHubFile.class)
                .build());

        // Create collection if it does not exist and store data
        collection
            .createCollectionIfNotExistsAsync()
            .then(storeData(collection, embeddingGeneration, sampleData()))
            .block();

        // Search for results
        var results = search("How to get started", collection, embeddingGeneration).block();

        if (results == null || results.getTotalCount() == 0) {
            System.out.println("No search results found.");
            return;
        }
        var searchResult = results.getResults().get(0);
        System.out.printf("Search result with score: %f.%n Link: %s, Description: %s%n",
            searchResult.getScore(), searchResult.getRecord().link,
            searchResult.getRecord().description);
    }

    private static Mono<VectorSearchResults<GitHubFile>> search(
        String searchText,
        VectorStoreRecordCollection<String, GitHubFile> recordCollection,
        OpenAITextEmbeddingGenerationService embeddingGeneration) {
        // Generate embeddings for the search text and search for the closest records
        return embeddingGeneration.generateEmbeddingAsync(searchText)
            .flatMap(r -> recordCollection.searchAsync(r.getVector(), null));
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
