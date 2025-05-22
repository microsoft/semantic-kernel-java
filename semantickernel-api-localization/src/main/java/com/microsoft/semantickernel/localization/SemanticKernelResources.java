// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.localization;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * Provides access to the resources used by the Semantic Kernel.
 */
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

    /**
     * Load the localized resource bundle for the Semantic Kernel.
     * If there is no resource bundle for the specified locale, the default
     * resource bundle will be loaded.
     * @param locale The locale to use.
     * @return the resource bundle.
     */
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

    /**
     * Set the locale for the Semantic Kernel. As a side effect, 
     * the localized resource bundle will be loaded.
     * @param locale The locale to use.
     * @return the locale.
     */
    public static Locale setLocale(Locale locale) {
        LOCALE = locale;
        setResourceBundle(locale);
        return locale;
    }

    /**
     * Get the string for the specified id from the resource bundle.
     * @param id The id of the string.
     * @param defaultValue The default value to return if the string is not found.
     * @return the localized string, or the default value if the string is not found.
     */
    public static String localize(String id, String defaultValue) {
        if (RESOURCE_BUNDLE.containsKey(id)) {
            return RESOURCE_BUNDLE.getString(id);
        } else {
            return defaultValue;
        }
    }

    /**
     * Get the string for the specified id from the resource bundle.
     * @param id The id of the string.
     * @return the localized string, or the id if the string is not found.
     */
    public static String getString(String id) {
        return localize(id, id);
    }

}
