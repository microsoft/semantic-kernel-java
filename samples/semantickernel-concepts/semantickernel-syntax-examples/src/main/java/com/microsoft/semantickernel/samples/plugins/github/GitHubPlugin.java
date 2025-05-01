// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.samples.plugins.github;

import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import com.microsoft.semantickernel.semanticfunctions.annotations.DefineKernelFunction;
import com.microsoft.semantickernel.semanticfunctions.annotations.KernelFunctionParameter;

import java.io.IOException;
import java.util.List;

public class GitHubPlugin {
    public static final String baseUrl = "https://api.github.com";
    private final String token;

    public GitHubPlugin(String token) {
        this.token = token;
    }

    @DefineKernelFunction(name = "get_user_info", description = "Get user information from GitHub", returnType = "com.microsoft.semantickernel.samples.plugins.github.GitHubModel$User")
    public Mono<GitHubModel.User> getUserProfileAsync() {
        HttpClient client = createClient();

        return makeRequestAsync(client, "/user")
            .map(json -> {
                try {
                    return GitHubModel.objectMapper.readValue(json, GitHubModel.User.class);
                } catch (IOException e) {
                    throw new IllegalStateException("Failed to deserialize GitHubUser", e);
                }
            });
    }

    @DefineKernelFunction(name = "get_repo_info", description = "Get repository information from GitHub", returnType = "com.microsoft.semantickernel.samples.plugins.github.GitHubModel$Repository")
    public Mono<GitHubModel.Repository> getRepositoryAsync(
        @KernelFunctionParameter(name = "organization", description = "The name of the repository to retrieve information for") String organization,
        @KernelFunctionParameter(name = "repo_name", description = "The name of the repository to retrieve information for") String repoName) {
        HttpClient client = createClient();

        return makeRequestAsync(client, String.format("/repos/%s/%s", organization, repoName))
            .map(json -> {
                try {
                    return GitHubModel.objectMapper.readValue(json, GitHubModel.Repository.class);
                } catch (IOException e) {
                    throw new IllegalStateException("Failed to deserialize GitHubRepository", e);
                }
            });
    }

    @DefineKernelFunction(name = "get_issues", description = "Get issues from GitHub", returnType = "java.util.List")
    public Mono<List<GitHubModel.Issue>> getIssuesAsync(
        @KernelFunctionParameter(name = "organization", description = "The name of the organization to retrieve issues for") String organization,
        @KernelFunctionParameter(name = "repo_name", description = "The name of the repository to retrieve issues for") String repoName,
        @KernelFunctionParameter(name = "max_results", description = "The maximum number of issues to retrieve", required = false, defaultValue = "10", type = int.class) int maxResults,
        @KernelFunctionParameter(name = "state", description = "The state of the issues to retrieve", required = false, defaultValue = "open") String state,
        @KernelFunctionParameter(name = "assignee", description = "The assignee of the issues to retrieve", required = false) String assignee) {
        HttpClient client = createClient();

        String query = String.format("/repos/%s/%s/issues", organization, repoName);
        query = buildQueryString(query, "state", state);
        query = buildQueryString(query, "assignee", assignee);
        query = buildQueryString(query, "per_page", String.valueOf(maxResults));

        return makeRequestAsync(client, query)
            .flatMap(json -> {
                try {
                    GitHubModel.Issue[] issues = GitHubModel.objectMapper.readValue(json,
                        GitHubModel.Issue[].class);
                    return Mono.just(List.of(issues));
                } catch (IOException e) {
                    throw new IllegalStateException("Failed to deserialize GitHubIssues", e);
                }
            });
    }

    @DefineKernelFunction(name = "get_issue_detail_info", description = "Get detail information of a single issue from GitHub", returnType = "com.microsoft.semantickernel.samples.plugins.github.GitHubModel$IssueDetail")
    public GitHubModel.IssueDetail getIssueDetailAsync(
        @KernelFunctionParameter(name = "organization", description = "The name of the repository to retrieve information for") String organization,
        @KernelFunctionParameter(name = "repo_name", description = "The name of the repository to retrieve information for") String repoName,
        @KernelFunctionParameter(name = "issue_number", description = "The issue number to retrieve information for", type = int.class) int issueNumber) {
        HttpClient client = createClient();

        return makeRequestAsync(client,
            String.format("/repos/%s/%s/issues/%d", organization, repoName, issueNumber))
            .map(json -> {
                try {
                    return GitHubModel.objectMapper.readValue(json, GitHubModel.IssueDetail.class);
                } catch (IOException e) {
                    throw new IllegalStateException("Failed to deserialize GitHubIssue", e);
                }
            }).block();
    }

    private HttpClient createClient() {
        return HttpClient.create()
            .baseUrl(baseUrl)
            .headers(headers -> {
                headers.add("User-Agent", "request");
                headers.add("Accept", "application/vnd.github+json");
                headers.add("Authorization", "Bearer " + token);
                headers.add("X-GitHub-Api-Version", "2022-11-28");
            });
    }

    private static String buildQueryString(String path, String param, String value) {
        if (value == null || value.isEmpty()
            || value.equals(KernelFunctionParameter.NO_DEFAULT_VALUE)) {
            return path;
        }

        return path + (path.contains("?") ? "&" : "?") + param + "=" + value;
    }

    private Mono<String> makeRequestAsync(HttpClient client, String path) {
        return client
            .get()
            .uri(path)
            .responseSingle((res, content) -> {
                if (res.status().code() != 200) {
                    return Mono.error(new IllegalStateException("Request failed: " + res.status()));
                }
                return content.asString();
            });
    }
}
