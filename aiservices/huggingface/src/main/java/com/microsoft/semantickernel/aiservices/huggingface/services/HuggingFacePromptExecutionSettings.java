// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.aiservices.huggingface.services;

import com.microsoft.semantickernel.orchestration.PromptExecutionSettings;
import com.microsoft.semantickernel.orchestration.responseformat.ResponseFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;

/**
 * Represents the settings for executing a prompt with the Hugging Face API.
 */
public class HuggingFacePromptExecutionSettings extends PromptExecutionSettings {

    @Nullable
    private final Integer topK;
    @Nullable
    private final Double repetitionPenalty;
    @Nullable
    private final Double maxTime;
    @Nullable
    private final Boolean details;
    @Nullable
    private final Boolean logProbs;
    @Nullable
    private final Integer topLogProbs;
    @Nullable
    private final Long seed;

    /**
     * Create a new instance of HuggingFacePromptExecutionSettings.
     *
     * @param copy The PromptExecutionSettings to copy.
     */
    public HuggingFacePromptExecutionSettings(PromptExecutionSettings copy) {
        super(
            copy.getServiceId(),
            copy.getModelId(),
            copy.getTemperature(),
            copy.getTopP(),
            copy.getPresencePenalty(),
            copy.getFrequencyPenalty(),
            copy.getMaxTokens(),
            copy.getResultsPerPrompt(),
            copy.getBestOf(),
            copy.getUser(),
            copy.getStopSequences(),
            copy.getTokenSelectionBiases(),
            copy.getResponseFormat() == null ? null : copy.getResponseFormat());
        this.topK = null;
        this.repetitionPenalty = null;
        this.maxTime = null;
        this.details = null;
        this.logProbs = null;
        this.topLogProbs = null;
        this.seed = null;
    }

    /**
     * Create a new instance of PromptExecutionSettings.
     *
     * @param serviceId            The id of the AI service to use for prompt execution.
     * @param modelId              The id of the model to use for prompt execution.
     * @param temperature          The temperature setting for prompt execution.
     * @param topP                 The topP setting for prompt execution.
     * @param presencePenalty      The presence penalty setting for prompt execution.
     * @param frequencyPenalty     The frequency penalty setting for prompt execution.
     * @param maxTokens            The maximum number of tokens to generate in the output.
     * @param resultsPerPrompt     The number of results to generate for each prompt.
     * @param bestOf               The best of setting for prompt execution.
     * @param user                 The user to associate with the prompt execution.
     * @param stopSequences        The stop sequences to use for prompt execution.
     * @param tokenSelectionBiases The token selection biases to use for prompt execution.
     * @param responseFormat       The response format to use for prompt execution
     * @param topK                 The topK setting for prompt execution.
     * @param repetitionPenalty    The repetition penalty setting for prompt execution.
     * @param maxTime              The max time setting for prompt execution.
     * @param details              The details setting for prompt execution.
     * @param logProbs             The logprobs setting for prompt execution.
     * @param topLogProbs          The top log probs setting for prompt execution.
     * @param seed                 The seed setting for prompt execution
     */
    public HuggingFacePromptExecutionSettings(
        String serviceId,
        String modelId,
        Double temperature,
        Double topP,
        Double presencePenalty,
        Double frequencyPenalty,
        Integer maxTokens,
        Integer resultsPerPrompt,
        Integer bestOf,
        String user,
        @Nullable List<String> stopSequences,
        @Nullable Map<Integer, Integer> tokenSelectionBiases,
        @Nullable ResponseFormat responseFormat,
        @Nullable Integer topK,
        @Nullable Double repetitionPenalty,
        @Nullable Double maxTime,
        @Nullable Boolean details,
        @Nullable Boolean logProbs,
        @Nullable Integer topLogProbs,
        @Nullable Long seed) {
        super(
            serviceId, modelId, temperature, topP, presencePenalty, frequencyPenalty, maxTokens,
            resultsPerPrompt, bestOf, user, stopSequences, tokenSelectionBiases, responseFormat);

        this.topK = topK;
        this.repetitionPenalty = repetitionPenalty;
        this.maxTime = maxTime;
        this.details = details;
        this.logProbs = logProbs;
        this.topLogProbs = topLogProbs;
        this.seed = seed;
    }

    /**
     * Create a new instance of PromptExecutionSettings from a PromptExecutionSettings.
     * This method handles the whether the PromptExecutionSettings is already a
     * HuggingFacePromptExecutionSettings or a new instance needs to be created
     * from the provided PromptExecutionSettings.
     * @param promptExecutionSettings The PromptExecutionSettings to copy.
     * @return The PromptExecutionSettings mapped to a HuggingFacePromptExecutionSettings.
     */
    public static HuggingFacePromptExecutionSettings fromExecutionSettings(
        PromptExecutionSettings promptExecutionSettings) {
        if (promptExecutionSettings instanceof HuggingFacePromptExecutionSettings) {
            return (HuggingFacePromptExecutionSettings) promptExecutionSettings;
        }

        return new HuggingFacePromptExecutionSettings(
            promptExecutionSettings.getServiceId(),
            promptExecutionSettings.getModelId(),
            promptExecutionSettings.getTemperature(),
            promptExecutionSettings.getTopP(),
            promptExecutionSettings.getPresencePenalty(),
            promptExecutionSettings.getFrequencyPenalty(),
            promptExecutionSettings.getMaxTokens(),
            promptExecutionSettings.getResultsPerPrompt(),
            promptExecutionSettings.getBestOf(),
            promptExecutionSettings.getUser(),
            promptExecutionSettings.getStopSequences(),
            promptExecutionSettings.getTokenSelectionBiases(),
            promptExecutionSettings.getResponseFormat() != null
                ? promptExecutionSettings.getResponseFormat()
                : null,
            null,
            null,
            null,
            null,
            null,
            null,
            null);
    }

    /**
     * Gets the topK setting for prompt execution.
     * @return The topK setting for prompt execution
     */
    @Nullable
    public Integer getTopK() {
        return topK;
    }

    /**
     * Gets the repetition penalty setting for prompt execution.
     * @return The repetition penalty setting for prompt execution
     */
    @Nullable
    public Double getRepetitionPenalty() {
        return repetitionPenalty;
    }

    /**
     * Gets the max time setting for prompt execution.
     * @return The max time setting for prompt execution
     */
    @Nullable
    public Double getMaxTime() {
        return maxTime;
    }

    /**
     * Gets the details setting for prompt execution.
     * @return The details setting for prompt execution
     */
    @Nullable
    public Boolean getDetails() {
        return details;
    }

    /**
     * Gets the logprobs setting for prompt execution.
     * @return The logprobs setting for prompt execution
     */
    @Nullable
    public Boolean getLogprobs() {
        return logProbs;
    }

    /**
     * Gets the top log probs setting for prompt execution.
     * @return The top log probs setting for prompt execution
     */
    @Nullable
    public Integer getTopLogProbs() {
        return topLogProbs;
    }

    /**
     * Gets the seed setting for prompt execution.
     * @return The seed setting for prompt execution
     */
    @Nullable
    public Long getSeed() {
        return seed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof HuggingFacePromptExecutionSettings)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        HuggingFacePromptExecutionSettings that = (HuggingFacePromptExecutionSettings) o;
        return Objects.equals(topK, that.topK) &&
            Objects.equals(repetitionPenalty, that.repetitionPenalty) &&
            Objects.equals(maxTime, that.maxTime) &&
            Objects.equals(details, that.details) &&
            Objects.equals(logProbs, that.logProbs) &&
            Objects.equals(topLogProbs, that.topLogProbs) &&
            Objects.equals(seed, that.seed);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), topK, repetitionPenalty, maxTime, details, logProbs,
            topLogProbs, seed);
    }
}
