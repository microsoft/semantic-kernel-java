// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.localization;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public class SemanticKernelResources {

    private static ResourceBundle RESOURCE_BUNDLE;
    private static Locale LOCALE;

    static {
        LOCALE = new Locale(
            System.getProperty("semantickernel.locale",
                String.valueOf(Locale.getDefault().getLanguage())));

        setLocale(LOCALE);
    }

    public static void setLocale(Locale locale) {
        LOCALE = locale;
        ResourceBundle resourceBundle;
        try {
            resourceBundle = PropertyResourceBundle.getBundle(
                "com.microsoft.semantickernel.localization.ResourceBundle", locale);
        } catch (MissingResourceException e) {
            resourceBundle = PropertyResourceBundle.getBundle(
                "com.microsoft.semantickernel.localization.ResourceBundle");
        }
        RESOURCE_BUNDLE = resourceBundle;
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
