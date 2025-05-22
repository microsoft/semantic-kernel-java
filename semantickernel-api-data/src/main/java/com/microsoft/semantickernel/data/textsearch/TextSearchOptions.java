// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.textsearch;

/**
 * Represents the options for a text search.
 */
public class TextSearchOptions {

    /**
     * The default number of search results to return.
     */
    public static final int DEFAULT_TOP = 3;
    private final boolean includeTotalCount;
    private final int top;
    private final int skip;
    private final TextSearchFilter filter;

    /**
     * Creates a new instance of the TextSearchOptions class with default values.
     *
     * @return A new instance of the TextSearchOptions class with default values.
     */
    public static TextSearchOptions createDefault() {
        return new TextSearchOptions(false, DEFAULT_TOP, 0, null);
    }

    /**
     * Creates a new instance of the TextSearchOptions class.
     *
     * @param includeTotalCount A value indicating whether to include the total count of search results.
     * @param top              The limit of the number of results to return.
     * @param skip             The offset of the results to return.
     * @param filter           The search filter.
     */
    TextSearchOptions(boolean includeTotalCount, int top, int skip, TextSearchFilter filter) {
        this.includeTotalCount = includeTotalCount;
        this.top = top;
        this.skip = skip;
        this.filter = filter;
    }

    /**
     * Gets a value indicating whether to include the total count of search results.
     *
     * @return A value indicating whether to include the total count of search results.
     */
    public boolean isIncludeTotalCount() {
        return includeTotalCount;
    }

    /**
     * Gets the limit of the number of results to return.
     *
     * @return The limit of the number of results to return.
     */
    public int getTop() {
        return top;
    }

    /**
     * Gets the offset of the results to return.
     *
     * @return The offset of the results to return.
     */
    public int getSkip() {
        return skip;
    }

    /**
     * Gets the search filter.
     *
     * @return The search filter.
     */
    public TextSearchFilter getFilter() {
        return filter;
    }

    /**
     * Creates a new instance of the {@link Builder} class.
     *
     * @return The builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * The builder for the {@link TextSearchOptions} class.
     */
    public static class Builder {
        private boolean includeTotalCount = false;
        private int top = DEFAULT_TOP;
        private int skip = 0;
        private TextSearchFilter filter;

        /**
         * Sets a value indicating whether to include the total count of search results.
         *
         * @param includeTotalCount A value indicating whether to include the total count of search results.
         * @return The builder.
         */
        public Builder withIncludeTotalCount(boolean includeTotalCount) {
            this.includeTotalCount = includeTotalCount;
            return this;
        }

        /**
         * Sets the limit of the number of results to return.
         *
         * @param top The limit of the number of results to return.
         * @return The builder.
         */
        public Builder withTop(int top) {
            this.top = top;
            return this;
        }

        /**
         * Sets the offset of the results to return.
         *
         * @param skip The offset of the results to return.
         * @return The builder.
         */
        public Builder withSkip(int skip) {
            this.skip = skip;
            return this;
        }

        /**
         * Sets the search filter.
         *
         * @param filter The search filter.
         * @return The builder.
         */
        public Builder withFilter(TextSearchFilter filter) {
            this.filter = filter;
            return this;
        }

        /**
         * Builds a new instance of the {@link TextSearchOptions} class.
         *
         * @return A new instance of the TextSearchOptions class.
         */
        public TextSearchOptions build() {
            return new TextSearchOptions(includeTotalCount, top, skip, filter);
        }
    }

}
