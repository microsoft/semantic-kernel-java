// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.localization;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public class SemanticKernelResources {

    private static final String RESOURCE_BUNDLE_CLASS = "com.microsoft.semantickernel.localization.ResourceBundle";

    private static ResourceBundle RESOURCE_BUNDLE;
    private static Locale LOCALE;

    static {
        LOCALE = setLocale(new Locale(
            System.getProperty("semantickernel.locale",
                String.valueOf(Locale.getDefault().getLanguage()))));
        RESOURCE_BUNDLE = setResourceBundle(LOCALE);
    }

    public static ResourceBundle setResourceBundle(Locale locale) {
        ResourceBundle resourceBundle;
        try {
            resourceBundle = PropertyResourceBundle.getBundle(
                RESOURCE_BUNDLE_CLASS, locale);
        } catch (MissingResourceException e) {
            resourceBundle = PropertyResourceBundle.getBundle(
                RESOURCE_BUNDLE_CLASS);
        }
        RESOURCE_BUNDLE = resourceBundle;
        return resourceBundle;
    }

    public static Locale setLocale(Locale locale) {
        LOCALE = locale;
        setResourceBundle(locale);
        return locale;
    }

    public static String localize(String id, String defaultValue) {
        if (RESOURCE_BUNDLE.containsKey(id)) {
            return RESOURCE_BUNDLE.getString(id);
        } else {
            return defaultValue;
        }
    }

    public static String getString(String s) {
        return localize(s, s);
    }

}
