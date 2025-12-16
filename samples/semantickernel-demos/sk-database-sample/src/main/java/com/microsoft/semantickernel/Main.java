// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.KeyCredential;
import com.microsoft.semantickernel.aiservices.openai.chatcompletion.OpenAIChatCompletion;
import com.microsoft.semantickernel.aiservices.openai.textembedding.OpenAITextEmbeddingGenerationService;
import com.microsoft.semantickernel.data.jdbc.JDBCVectorStore;
import com.microsoft.semantickernel.data.jdbc.JDBCVectorStoreOptions;
import com.microsoft.semantickernel.data.jdbc.JDBCVectorStoreRecordCollectionOptions;
import com.microsoft.semantickernel.data.jdbc.mysql.MySQLVectorStoreQueryProvider;
import com.microsoft.semantickernel.data.vectorsearch.VectorSearchResults;
import com.microsoft.semantickernel.data.vectorstorage.VectorStoreRecordCollection;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import com.mysql.cj.jdbc.MysqlDataSource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class Main {

    private static final String USE_AZURE_CLIENT = System.getenv("USE_AZURE_CLIENT");

    private static final String CLIENT_KEY = System.getenv("CLIENT_KEY");
    private static final String AZURE_CLIENT_KEY = System.getenv("AZURE_CLIENT_KEY");

    // Only required if AZURE_CLIENT_KEY is set
    private static final String CLIENT_ENDPOINT = System.getenv("CLIENT_ENDPOINT");
    private static final String MODEL_ID = System.getenv()
        .getOrDefault("MODEL_ID", "gpt-4o");

    private static final String EMBEDDING_MODEL_ID = System.getenv()
        .getOrDefault("EMBEDDING_MODEL_ID", "text-embedding-3-large");

    public static final String JDBC_MYSQL_TESTDB = "jdbc:mysql://mysql:3306/testdb";
    public static final String USERNAME = "a-user";
    public static final String PASSWORD = "a-password";

    public static void main(String[] args) throws InterruptedException {

        OpenAIAsyncClient client;

        if (Boolean.parseBoolean(USE_AZURE_CLIENT)) {
            client = new OpenAIClientBuilder()
                .credential(new AzureKeyCredential(AZURE_CLIENT_KEY))
                .endpoint(CLIENT_ENDPOINT)
                .buildAsyncClient();

        } else {
            client = new OpenAIClientBuilder()
                .credential(new KeyCredential(CLIENT_KEY))
                .buildAsyncClient();
        }

        Kernel kernel = buildKernel(client);

        // Create an OpenAI text embedding generation service
        var embeddingGeneration = OpenAITextEmbeddingGenerationService.builder()
            .withOpenAIAsyncClient(client)
            .withModelId(EMBEDDING_MODEL_ID)
            .withDimensions(1536)
            .build();

        JDBCVectorStore dataSource = buildDataSource();

        VectorStoreRecordCollection<String, GitHubFile> collection = createCollection(
            embeddingGeneration, dataSource);

        searchFor("How to get started", embeddingGeneration, collection);
        searchFor("How do I create a react webapp", embeddingGeneration,
            collection);
    }

    public static VectorStoreRecordCollection<String, GitHubFile> createCollection(
        OpenAITextEmbeddingGenerationService embeddingGeneration,
        JDBCVectorStore jdbcVectorStore) {

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

        return collection;
    }

    private static void searchFor(String request,
        OpenAITextEmbeddingGenerationService embeddingGeneration,
        VectorStoreRecordCollection<String, GitHubFile> collection) {
        System.out.println("-----------------------------------------------");
        System.out.println("Searching for '" + request + "' in the collection.");

        // Search for results

        var results = search(request, collection, embeddingGeneration).block();

        if (results == null || results.getTotalCount() == 0) {
            System.out.println("No search results found.");
            return;
        }
        var searchResult = results.getResults().get(0);
        System.out.printf("Search result with score: %f.%n Link: %s, Description: %s%n",
            searchResult.getScore(), searchResult.getRecord().getLink(),
            searchResult.getRecord().getDescription());
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


    private static JDBCVectorStore buildDataSource() {
        MysqlDataSource mysqlDataSource = new MysqlDataSource();
        mysqlDataSource.setUrl(JDBC_MYSQL_TESTDB);
        mysqlDataSource.setUser(USERNAME);
        mysqlDataSource.setPassword(PASSWORD);
        MySQLVectorStoreQueryProvider queryProvider = MySQLVectorStoreQueryProvider.builder()
            .withDataSource(mysqlDataSource)
            .build();

        JDBCVectorStore vectorStore = JDBCVectorStore.builder()
            .withDataSource(mysqlDataSource)
            .withOptions(
                JDBCVectorStoreOptions.builder()
                    .withQueryProvider(queryProvider)
                    .build()
            )
            .build();

        vectorStore.prepareAsync().block();
        return vectorStore;
    }

    private static Kernel buildKernel(OpenAIAsyncClient client) {

        ChatCompletionService chat = OpenAIChatCompletion.builder()
            .withModelId(MODEL_ID)
            .withOpenAIAsyncClient(client)
            .build();

        return Kernel
            .builder()
            .withAIService(ChatCompletionService.class, chat)
            .build();

    }

    private static Map<String, String> sampleData() {
        return Arrays.stream(new String[][]{
            {"https://github.com/microsoft/semantic-kernel/blob/main/README.md",
                "README: Installation, getting started with Semantic Kernel, and how to contribute"},
            {"https://github.com/microsoft/semantic-kernel/blob/main/samples/notebooks/dotnet/02-running-prompts-from-file.ipynb",
                "Jupyter notebook describing how to pass prompts from a file to a semantic skill or function"},
            {"https://github.com/microsoft/semantic-kernel/tree/main/samples/skills/ChatSkill/ChatGPT",
                "Sample demonstrating how to create a chat skill interfacing with ChatGPT"},
            {"https://github.com/microsoft/semantic-kernel/blob/main/dotnet/src/SemanticKernel/Memory/VolatileMemoryStore.cs",
                "C# class that defines a volatile embedding store"},
            {"https://github.com/microsoft/semantic-kernel/blob/main/samples/dotnet/KernelHttpServer/README.md",
                "README: How to set up a Semantic Kernel Service API using Azure Function Runtime v4"},
            {"https://github.com/microsoft/semantic-kernel/blob/main/samples/apps/chat-summary-webapp-react/README.md",
                "README: README associated with a sample chat summary react-based webapp"},
        }).collect(Collectors.toMap(element -> element[0], element -> element[1]));
    }

}
