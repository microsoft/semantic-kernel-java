// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.aiservices.voyageai.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * VoyageAI API request and response models.
 */
public class VoyageAIModels {

    // Embedding Models

    /**
     * Request model for text embeddings.
     */
    public static class EmbeddingRequest {
        @JsonProperty("input")
        private List<String> input;

        @JsonProperty("model")
        private String model;

        @JsonProperty("input_type")
        private String inputType;

        @JsonProperty("truncation")
        private Boolean truncation;

        @JsonProperty("output_dimension")
        private Integer outputDimension;

        @JsonProperty("output_dtype")
        private String outputDtype;

        public List<String> getInput() {
            return input;
        }

        public void setInput(List<String> input) {
            this.input = input;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getInputType() {
            return inputType;
        }

        public void setInputType(String inputType) {
            this.inputType = inputType;
        }

        public Boolean getTruncation() {
            return truncation;
        }

        public void setTruncation(Boolean truncation) {
            this.truncation = truncation;
        }

        public Integer getOutputDimension() {
            return outputDimension;
        }

        public void setOutputDimension(Integer outputDimension) {
            this.outputDimension = outputDimension;
        }

        public String getOutputDtype() {
            return outputDtype;
        }

        public void setOutputDtype(String outputDtype) {
            this.outputDtype = outputDtype;
        }
    }

    /**
     * Response model for embeddings.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EmbeddingResponse {
        @JsonProperty("data")
        private List<EmbeddingDataItem> data;

        @JsonProperty("usage")
        private EmbeddingUsage usage;

        public List<EmbeddingDataItem> getData() {
            return data;
        }

        public void setData(List<EmbeddingDataItem> data) {
            this.data = data;
        }

        public EmbeddingUsage getUsage() {
            return usage;
        }

        public void setUsage(EmbeddingUsage usage) {
            this.usage = usage;
        }
    }

    /**
     * Embedding data item.
     */
    public static class EmbeddingDataItem {
        @JsonProperty("object")
        private String object;

        @JsonProperty("embedding")
        private float[] embedding;

        @JsonProperty("index")
        private int index;

        public String getObject() {
            return object;
        }

        public void setObject(String object) {
            this.object = object;
        }

        public float[] getEmbedding() {
            return embedding;
        }

        public void setEmbedding(float[] embedding) {
            this.embedding = embedding;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }
    }

    /**
     * Usage information.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EmbeddingUsage {
        @JsonProperty("total_tokens")
        private int totalTokens;

        public int getTotalTokens() {
            return totalTokens;
        }

        public void setTotalTokens(int totalTokens) {
            this.totalTokens = totalTokens;
        }
    }

    // Reranking Models

    /**
     * Request model for reranking.
     */
    public static class RerankRequest {
        @JsonProperty("query")
        private String query;

        @JsonProperty("documents")
        private List<String> documents;

        @JsonProperty("model")
        private String model;

        @JsonProperty("top_k")
        private Integer topK;

        @JsonProperty("truncation")
        private Boolean truncation;

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        public List<String> getDocuments() {
            return documents;
        }

        public void setDocuments(List<String> documents) {
            this.documents = documents;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public Integer getTopK() {
            return topK;
        }

        public void setTopK(Integer topK) {
            this.topK = topK;
        }

        public Boolean getTruncation() {
            return truncation;
        }

        public void setTruncation(Boolean truncation) {
            this.truncation = truncation;
        }
    }

    /**
     * Response model for reranking.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RerankResponse {
        @JsonProperty("data")
        private List<RerankDataItem> data;

        @JsonProperty("usage")
        private EmbeddingUsage usage;

        public List<RerankDataItem> getData() {
            return data;
        }

        public void setData(List<RerankDataItem> data) {
            this.data = data;
        }

        public EmbeddingUsage getUsage() {
            return usage;
        }

        public void setUsage(EmbeddingUsage usage) {
            this.usage = usage;
        }
    }

    /**
     * Rerank data item.
     */
    public static class RerankDataItem {
        @JsonProperty("index")
        private int index;

        @JsonProperty("relevance_score")
        private double relevanceScore;

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public double getRelevanceScore() {
            return relevanceScore;
        }

        public void setRelevanceScore(double relevanceScore) {
            this.relevanceScore = relevanceScore;
        }
    }

    // Contextualized Embedding Models

    /**
     * Request model for contextualized embeddings.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ContextualizedEmbeddingRequest {
        @JsonProperty("inputs")
        private List<List<String>> inputs;

        @JsonProperty("model")
        private String model;

        @JsonProperty("input_type")
        private String inputType;

        @JsonProperty("truncation")
        private Boolean truncation;

        @JsonProperty("output_dimension")
        private Integer outputDimension;

        @JsonProperty("output_dtype")
        private String outputDtype;

        public List<List<String>> getInputs() {
            return inputs;
        }

        public void setInputs(List<List<String>> inputs) {
            this.inputs = inputs;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getInputType() {
            return inputType;
        }

        public void setInputType(String inputType) {
            this.inputType = inputType;
        }

        public Boolean getTruncation() {
            return truncation;
        }

        public void setTruncation(Boolean truncation) {
            this.truncation = truncation;
        }

        public Integer getOutputDimension() {
            return outputDimension;
        }

        public void setOutputDimension(Integer outputDimension) {
            this.outputDimension = outputDimension;
        }

        public String getOutputDtype() {
            return outputDtype;
        }

        public void setOutputDtype(String outputDtype) {
            this.outputDtype = outputDtype;
        }
    }

    /**
     * Response model for contextualized embeddings.
     * VoyageAI returns a nested list structure: {"object":"list","data":[{"object":"list","data":[...]}]}
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ContextualizedEmbeddingResponse {
        @JsonProperty("data")
        private List<ContextualizedEmbeddingDataList> data;

        public List<ContextualizedEmbeddingDataList> getData() {
            return data;
        }

        public void setData(List<ContextualizedEmbeddingDataList> data) {
            this.data = data;
        }
    }

    /**
     * Nested data list for contextualized embeddings.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ContextualizedEmbeddingDataList {
        @JsonProperty("data")
        private List<EmbeddingDataItem> data;

        public List<EmbeddingDataItem> getData() {
            return data;
        }

        public void setData(List<EmbeddingDataItem> data) {
            this.data = data;
        }
    }

    /**
     * Contextualized embedding result.
     */
    public static class ContextualizedEmbeddingResult {
        @JsonProperty("embeddings")
        private List<EmbeddingItem> embeddings;

        public List<EmbeddingItem> getEmbeddings() {
            return embeddings;
        }

        public void setEmbeddings(List<EmbeddingItem> embeddings) {
            this.embeddings = embeddings;
        }
    }

    /**
     * Embedding item with chunk information.
     */
    public static class EmbeddingItem {
        @JsonProperty("embedding")
        private float[] embedding;

        @JsonProperty("chunk")
        private String chunk;

        @JsonProperty("index")
        private int index;

        public float[] getEmbedding() {
            return embedding;
        }

        public void setEmbedding(float[] embedding) {
            this.embedding = embedding;
        }

        public String getChunk() {
            return chunk;
        }

        public void setChunk(String chunk) {
            this.chunk = chunk;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }
    }

    // Multimodal Embedding Models

    /**
     * Content item for multimodal input (text or image).
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MultimodalContentItem {
        @JsonProperty("type")
        private String type;  // "text" or "image_url"

        @JsonProperty("text")
        private String text;

        @JsonProperty("image_url")
        private String imageUrl;

        public MultimodalContentItem() {
            // Default constructor for Jackson
        }

        public MultimodalContentItem(String type, String value) {
            this.type = type;
            if ("text".equals(type)) {
                this.text = value;
            } else if ("image_url".equals(type)) {
                this.imageUrl = value;
            }
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }
    }

    /**
     * Input for multimodal embedding (contains a list of content items).
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MultimodalInput {
        @JsonProperty("content")
        private List<MultimodalContentItem> content;

        public MultimodalInput() {
            // Default constructor for Jackson
        }

        public MultimodalInput(List<MultimodalContentItem> content) {
            this.content = content;
        }

        public List<MultimodalContentItem> getContent() {
            return content;
        }

        public void setContent(List<MultimodalContentItem> content) {
            this.content = content;
        }
    }

    /**
     * Request model for multimodal embeddings.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MultimodalEmbeddingRequest {
        @JsonProperty("inputs")
        private List<MultimodalInput> inputs;

        @JsonProperty("model")
        private String model;

        @JsonProperty("input_type")
        private String inputType;

        @JsonProperty("truncation")
        private Boolean truncation;

        public List<MultimodalInput> getInputs() {
            return inputs;
        }

        public void setInputs(List<MultimodalInput> inputs) {
            this.inputs = inputs;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getInputType() {
            return inputType;
        }

        public void setInputType(String inputType) {
            this.inputType = inputType;
        }

        public Boolean getTruncation() {
            return truncation;
        }

        public void setTruncation(Boolean truncation) {
            this.truncation = truncation;
        }
    }

    /**
     * Response model for multimodal embeddings.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MultimodalEmbeddingResponse {
        @JsonProperty("data")
        private List<EmbeddingDataItem> data;

        @JsonProperty("usage")
        private EmbeddingUsage usage;

        public List<EmbeddingDataItem> getData() {
            return data;
        }

        public void setData(List<EmbeddingDataItem> data) {
            this.data = data;
        }

        public EmbeddingUsage getUsage() {
            return usage;
        }

        public void setUsage(EmbeddingUsage usage) {
            this.usage = usage;
        }
    }
}
