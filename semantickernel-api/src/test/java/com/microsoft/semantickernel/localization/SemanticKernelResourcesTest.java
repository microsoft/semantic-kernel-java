// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.localization;

import java.util.Locale;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SemanticKernelResourcesTest {

    @Test
    public void languageAndCountry() {
        SemanticKernelResources.setLocale(new Locale("en", "GB"));

        String result = SemanticKernelResources.localize("test_language_country", "a test string");

        Assertions.assertEquals("GB english test value", result);
    }

    @Test
    public void valueAtTheLanguageLevel() {
        SemanticKernelResources.setLocale(new Locale("en", "GB"));

        String result = SemanticKernelResources.localize("test_language", "a test string");

        Assertions.assertEquals("English test value", result);
    }

    @Test
    public void topLevelValue() {
        SemanticKernelResources.setLocale(new Locale("en", "GB"));

        String result = SemanticKernelResources.localize("test_top", "default value");

        Assertions.assertEquals("Top level value", result);
    }

    @Test
    public void defaultValue() {
        SemanticKernelResources.setLocale(new Locale("en", "GB"));

        String result = SemanticKernelResources.localize("not-there", "default value");

        Assertions.assertEquals("default value", result);
    }
}
