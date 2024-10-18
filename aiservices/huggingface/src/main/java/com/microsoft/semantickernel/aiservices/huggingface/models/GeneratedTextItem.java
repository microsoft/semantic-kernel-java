// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.aiservices.huggingface.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

/**
 * Represents a generated text item deserialized from a JSON response.
 */
public class GeneratedTextItem {

    @Nullable
    @JsonProperty("generated_text")
    private final String generatedText;

    @Nullable
    @JsonProperty("details")
    private final TextGenerationDetails details;

    /** 
     * Constructor used by Jackson to deserialize a generated text item.
     * @param generatedText The generated text.
     * @param details The details of the generation.
     */
    @JsonCreator
    public GeneratedTextItem(
        @JsonProperty("generated_text") @Nullable String generatedText,
        @JsonProperty("details") @Nullable TextGenerationDetails details) {
        this.generatedText = generatedText;
        this.details = details;
    }

    /**
     * Gets the generated text.
     * @return The generated text.
     */
    @Nullable
    public String getGeneratedText() {
        return generatedText;
    }

    /**
     * Gets the details of the generation.
     * @return The details of the generation.
     */
    @Nullable
    public TextGenerationDetails getDetails() {
        return details;
    }

    /**
     * Represents the details of a text generation deserialized from a JSON response.
     */
    public static class TextGenerationDetails {

        @Nullable
        @JsonProperty("finish_reason")
        private final String finishReason;

        @JsonProperty("generated_tokens")
        private final int generatedTokens;

        @Nullable
        @JsonProperty("seed")
        private final Long seed;

        @Nullable
        @JsonProperty("prefill")
        private final List<TextGenerationPrefillToken> prefill;

        @Nullable
        @JsonProperty("tokens")
        private final List<TextGenerationToken> tokens;

        /**
         * Constructor used by Jackson to deserialize text generation details.
         * @param finishReason The reason the generation finished.
         * @param generatedTokens The number of tokens generated.
         * @param seed The seed used for generation.
         * @param prefill The prefill tokens.
         * @param tokens The generated tokens.
         */
        @JsonCreator
        public TextGenerationDetails(
            @JsonProperty("finish_reason") @Nullable String finishReason,
            @JsonProperty("generated_tokens") int generatedTokens,
            @JsonProperty("seed") @Nullable Long seed,
            @JsonProperty("prefill") @Nullable List<TextGenerationPrefillToken> prefill,
            @JsonProperty("tokens") @Nullable List<TextGenerationToken> tokens) {
            this.finishReason = finishReason;
            this.generatedTokens = generatedTokens;
            this.seed = seed;
            if (prefill != null) {
                this.prefill = new ArrayList<>(prefill);
            } else {
                this.prefill = null;
            }
            if (tokens != null) {
                this.tokens = new ArrayList<>(tokens);
            } else {
                this.tokens = null;
            }
        }

        /**
         * Gets the reason the generation finished.
         * @return The reason the generation finished.
         */
        @Nullable
        public String getFinishReason() {
            return finishReason;
        }

        /**
         * Gets the number of tokens generated.
         * @return The number of tokens generated.
         */
        public int getGeneratedTokens() {
            return generatedTokens;
        }

        /**
         * Gets the seed used for generation.
         * @return The seed used for generation.
         */
        @Nullable
        public Long getSeed() {
            return seed;
        }

        /**
         * Gets the prefill tokens.
         * @return The prefill tokens.
         */
        @Nullable
        public List<TextGenerationPrefillToken> getPrefill() {
            return Collections.unmodifiableList(prefill);
        }

        /**
         * Gets the generated tokens.
         * @return The generated tokens.
         */
        @Nullable
        public List<TextGenerationToken> getTokens() {
            return Collections.unmodifiableList(tokens);
        }
    }

    /**
     * Represents a prefill token deserialized from a JSON response.
     */
    public static class TextGenerationPrefillToken {

        @JsonProperty("id")
        private final int id;

        @Nullable
        @JsonProperty("text")
        private final String text;

        @JsonProperty("logprob")
        private final double logProb;

        /**
         * Constructor used by Jackson to deserialize a prefill token.
         * @param id The token ID.
         * @param text The token text.
         * @param logProb The log probability of the token.
         */
        @JsonCreator
        public TextGenerationPrefillToken(
            @JsonProperty("id") int id,
            @JsonProperty("text") @Nullable String text,
            @JsonProperty("logprob") double logProb) {
            this.id = id;
            this.text = text;
            this.logProb = logProb;
        }

        /**
         * Gets the token ID.
         * @return The token ID.
         */
        public int getId() {
            return id;
        }

        /**
         * Gets the token text.
         * @return The token text.
         */
        @Nullable
        public String getText() {
            return text;
        }

        /**
         * Gets the log probability of the token.
         * @return The log probability of the token.
         */
        public double getLogProb() {
            return logProb;
        }
    }

    /**
     * Represents a generated token deserialized from a JSON response.
     */
    public static class TextGenerationToken extends TextGenerationPrefillToken {

        @JsonProperty("special")
        private final boolean special;

        /**
         * Constructor used by Jackson to deserialize a generated token.
         * @param special Whether the token is special.
         * @param id The token ID.
         * @param text The token text.
         * @param logProb The log probability of the token.
         */
        @JsonCreator
        public TextGenerationToken(
            @JsonProperty("special") boolean special,
            @JsonProperty("id") int id,
            @JsonProperty("text") @Nullable String text,
            @JsonProperty("logprob") double logProb) {
            super(id, text, logProb);
            this.special = special;
        }

        /**
         * Gets whether the token is special.
         * @return Whether the token is special.
         */
        public boolean isSpecial() {
            return special;
        }
    }
}
