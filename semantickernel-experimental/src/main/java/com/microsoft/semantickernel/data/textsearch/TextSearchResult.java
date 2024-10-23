// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.textsearch;

import com.microsoft.semantickernel.exceptions.SKException;

import java.lang.reflect.Field;

/**
 * Represents a text search result.
 */
public class TextSearchResult {
    private final String name;
    private final String value;
    private final String link;

    /**
     * Creates a new instance of the TextSearchResult class.
     *
     * @param name  The name of the search result.
     * @param value The value of the search result.
     * @param link  The link of the search result.
     */
    TextSearchResult(String name, String value, String link) {
        this.name = name;
        this.value = value;
        this.link = link;
    }

    /**
     * Gets the name of the search result.
     *
     * @return The name of the search result.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the value of the search result.
     *
     * @return The value of the search result.
     */
    public String getValue() {
        return value;
    }

    /**
     * Gets the link of the search result.
     *
     * @return The link of the search result.
     */
    public String getLink() {
        return link;
    }

    /**
     * Creates a new instance of the {@link TextSearchResult} class from a record.
     * The record should have fields annotated with {@link TextSearchResultName}, {@link TextSearchResultValue}, and {@link TextSearchResultLink}.
     *
     * @param record The record.
     * @return The TextSearchResult.
     */
    public static TextSearchResult fromRecord(Object record) {
        String name = null, value = null, link = null;

        try {
            for (Field field : record.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(TextSearchResultName.class)) {
                    if (name != null) {
                        throw new SKException("Multiple fields with @TextSearchResultName found");
                    }

                    field.setAccessible(true);
                    name = (String) field.get(record);
                }
                if (field.isAnnotationPresent(TextSearchResultValue.class)) {
                    if (value != null) {
                        throw new SKException("Multiple fields with @TextSearchResultValue found");
                    }

                    field.setAccessible(true);
                    value = (String) field.get(record);
                }
                if (field.isAnnotationPresent(TextSearchResultLink.class)) {
                    if (link != null) {
                        throw new SKException("Multiple fields with @TextSearchResultLink found");
                    }

                    field.setAccessible(true);
                    link = (String) field.get(record);
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        if (value == null) {
            throw new SKException("No field with @TextSearchResultValue found");
        }

        return new TextSearchResult(name, value, link);
    }
}
