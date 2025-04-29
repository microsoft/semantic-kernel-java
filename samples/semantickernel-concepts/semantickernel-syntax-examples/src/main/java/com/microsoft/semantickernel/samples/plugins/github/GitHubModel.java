package com.microsoft.semantickernel.samples.plugins.github;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class GitHubModel {
    public final static ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Override
    public String toString() {
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static class User extends GitHubModel {
        @JsonProperty("login")
        private String login;
        @JsonProperty("id")
        private long id;
        @JsonProperty("name")
        private String name;
        @JsonProperty("company")
        private String company;
        @JsonProperty("html_url")
        private String url;
        @JsonCreator
        public User(@JsonProperty("login") String login,
                    @JsonProperty("id") long id,
                    @JsonProperty("name") String name,
                    @JsonProperty("company") String company,
                    @JsonProperty("html_url") String url) {
            this.login = login;
            this.id = id;
            this.name = name;
            this.company = company;
            this.url = url;
        }

        public String getLogin() {
            return login;
        }
        public long getId() {
            return id;
        }
        public String getName() {
            return name;
        }
        public String getCompany() {
            return company;
        }
        public String getUrl() {
            return url;
        }
    }

    public static class Repository extends GitHubModel {
        @JsonProperty("id")
        private long id;
        @JsonProperty("full_name")
        private String name;
        @JsonProperty("description")
        private String description;
        @JsonProperty("html_url")
        private String url;
        @JsonCreator
        public Repository(@JsonProperty("id") long id,
                          @JsonProperty("full_name") String name,
                          @JsonProperty("description") String description,
                          @JsonProperty("html_url") String url) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.url = url;
        }

        public long getId() {
            return id;
        }
        public String getName() {
            return name;
        }
        public String getDescription() {
            return description;
        }
        public String getUrl() {
            return url;
        }

        @Override
        public String toString() {
            try {
                return objectMapper.writeValueAsString(this);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class Issue extends GitHubModel {
        @JsonProperty("id")
        private long id;
        @JsonProperty("number")
        private long number;
        @JsonProperty("title")
        private String title;
        @JsonProperty("state")
        private String state;
        @JsonProperty("html_url")
        private String url;
        @JsonProperty("labels")
        private Label[] labels;
        @JsonProperty("created_at")
        private String createdAt;
        @JsonProperty("closed_at")
        private String closedAt;

        @JsonCreator
        public Issue(@JsonProperty("id") long id,
                     @JsonProperty("number") long number,
                     @JsonProperty("title") String title,
                     @JsonProperty("state") String state,
                     @JsonProperty("html_url") String url,
                     @JsonProperty("labels") Label[] labels,
                     @JsonProperty("created_at") String createdAt,
                     @JsonProperty("closed_at") String closedAt) {
            this.id = id;
            this.number = number;
            this.title = title;
            this.state = state;
            this.url = url;
            this.labels = labels;
            this.createdAt = createdAt;
            this.closedAt = closedAt;
        }

        public long getId() {
            return id;
        }
        public long getNumber() {
            return number;
        }
        public String getTitle() {
            return title;
        }
        public String getState() {
            return state;
        }
        public String getUrl() {
            return url;
        }
        public Label[] getLabels() {
            return labels;
        }
        public String getCreatedAt() {
            return createdAt;
        }
        public String getClosedAt() {
            return closedAt;
        }
    }

    public static class IssueDetail extends Issue {
        @JsonProperty("body")
        private String body;

        @JsonCreator
        public IssueDetail(@JsonProperty("id") long id,
                           @JsonProperty("number") long number,
                           @JsonProperty("title") String title,
                           @JsonProperty("state") String state,
                           @JsonProperty("html_url") String url,
                           @JsonProperty("labels") Label[] labels,
                           @JsonProperty("created_at") String createdAt,
                           @JsonProperty("closed_at") String closedAt,
                           @JsonProperty("body") String body) {
            super(id, number, title, state, url, labels, createdAt, closedAt);
            this.body = body;
        }

        public String getBody() {
            return body;
        }
    }

    public static class Label extends GitHubModel {
        @JsonProperty("id")
        private long id;
        @JsonProperty("name")
        private String name;
        @JsonProperty("description")
        private String description;

        @JsonCreator
        public Label(@JsonProperty("id") long id,
                     @JsonProperty("name") String name,
                     @JsonProperty("description") String description) {
            this.id = id;
            this.name = name;
            this.description = description;
        }

        public long getId() {
            return id;
        }
        public String getName() {
            return name;
        }
        public String getDescription() {
            return description;
        }
    }
}
