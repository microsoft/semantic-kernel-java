// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.samples.syntaxexamples.memory;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.KeyCredential;
import com.microsoft.semantickernel.aiservices.openai.textembedding.OpenAITextEmbeddingGenerationService;
import com.microsoft.semantickernel.connectors.data.jdbc.JDBCVectorStore;
import com.microsoft.semantickernel.connectors.data.jdbc.JDBCVectorStoreOptions;
import com.microsoft.semantickernel.connectors.data.jdbc.MySQLVectorStoreQueryProvider;
import com.microsoft.semantickernel.data.VectorStoreRecordCollection;
import com.microsoft.semantickernel.data.recordattributes.VectorStoreRecordDataAttribute;
import com.microsoft.semantickernel.data.recordattributes.VectorStoreRecordKeyAttribute;
import com.microsoft.semantickernel.data.recordattributes.VectorStoreRecordVectorAttribute;
import com.mysql.cj.jdbc.MysqlDataSource;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class JDBC_DataStorage {

    private static final String CLIENT_KEY = System.getenv("CLIENT_KEY");
    private static final String AZURE_CLIENT_KEY = System.getenv("AZURE_CLIENT_KEY");

    // Only required if AZURE_CLIENT_KEY is set
    private static final String CLIENT_ENDPOINT = System.getenv("CLIENT_ENDPOINT");
    private static final String MODEL_ID = System.getenv()
        .getOrDefault("EMBEDDING_MODEL_ID", "text-embedding-3-large");
    private static final int EMBEDDING_DIMENSIONS = 1536;

    // Run a MySQL server with:
    // docker run -d --name mysql-container -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=sk -p 3306:3306 mysql:latest

    static class GitHubFile {

        @VectorStoreRecordKeyAttribute()
        private final String id;
        @VectorStoreRecordDataAttribute(hasEmbedding = true, embeddingFieldName = "embedding")
        private final String description;
        @VectorStoreRecordDataAttribute
        private final String link;
        @VectorStoreRecordVectorAttribute(dimensions = EMBEDDING_DIMENSIONS, indexKind = "Hnsw")
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

        static String encodeId(String realId) {
            byte[] bytes = Base64.getUrlEncoder().encode(realId.getBytes(StandardCharsets.UTF_8));
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }

    public static void main(String[] args) throws SQLException {
        System.out.println("==============================================================");
        System.out.println("========== JDBC Vector Store Example ==============");
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

        var dataSource = new MysqlDataSource();
        dataSource.setUrl("jdbc:mysql://localhost:3306/sk");
        dataSource.setPassword("root");
        dataSource.setUser("root");

        dataStorageWithMySQL(dataSource, embeddingGeneration);
    }

    public static void dataStorageWithMySQL(
        DataSource dataSource,
        OpenAITextEmbeddingGenerationService embeddingGeneration) {

        // Build a query provider
        var queryProvider = MySQLVectorStoreQueryProvider.builder()
            .withDataSource(dataSource)
            .build();

        // Create a new vector store
        var jdbcVectorStore = JDBCVectorStore.builder()
            .withDataSource(dataSource)
            .withOptions(JDBCVectorStoreOptions.builder()
                .withQueryProvider(queryProvider)
                .build())
            .build();

        String collectionName = "skgithubfiles";
        var collection = jdbcVectorStore.getCollection(collectionName,
            GitHubFile.class,
            null);

        // Create collection if it does not exist and store data
        List<String> ids = collection
            .createCollectionIfNotExistsAsync()
            .then(storeData(collection, embeddingGeneration, sampleData()))
            .block();

        List<GitHubFile> data = collection.getBatchAsync(ids, null).block();

        data.forEach(gitHubFile -> System.out.println("Retrieved: " + gitHubFile.getDescription()));
    }

    private static Mono<List<String>> storeData(
        VectorStoreRecordCollection<String, GitHubFile> recordStore,
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
