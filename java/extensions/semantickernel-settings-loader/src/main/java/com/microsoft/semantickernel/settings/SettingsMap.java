// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.settings;

import static com.microsoft.semantickernel.exceptions.ConfigurationException.ErrorCodes.CONFIGURATION_NOT_FOUND;
import static com.microsoft.semantickernel.exceptions.ConfigurationException.ErrorCodes.COULD_NOT_READ_CONFIGURATION;
import static com.microsoft.semantickernel.exceptions.ConfigurationException.ErrorCodes.NO_VALID_CONFIGURATIONS_FOUND;

import com.microsoft.semantickernel.exceptions.ConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.slf4j.Logger;

/**
 * Creates a map of settings to be used in configuration, the settings are loaded from the following
 * sources in the order of precedence (lower number in this list overrides a higher number):
 *
 * <ul>
 *   <li>1. System properties.
 *   <li>2. Environment variables.
 *   <li>3. Properties file set via CONF_PROPERTIES environment variable.
 *   <li>4. Properties file: ./conf.properties (Not used if CONF_PROPERTIES was set).
 *   <li>5. Properties file: ~/.sk/conf.properties (Not used if CONF_PROPERTIES was set).
 * </ul>
 */
public class SettingsMap {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(SettingsMap.class);

    public static final String CONF_PROPERTIES_NAME = "CONF_PROPERTIES";
    public static final String CONF_PROPERTIES =
            getEnvOrProperty(CONF_PROPERTIES_NAME, "conf.properties");

    private static final List<File> DEFAULT_PROPERTIES_LOCATIONS =
            Arrays.asList(
                    new File(new File(System.getProperty("user.home"), ".sk"), "conf.properties"),
                    new File("conf.properties"));
    @Nullable private static Map<String, String> DEFAULT_INST;

    private static void initSettings() {
        if (DEFAULT_INST != null) {
            return;
        }

        // Create the default instance
        Map<String, String> DEFAULT_INST_TMP;
        try {
            DEFAULT_INST_TMP = get(DEFAULT_PROPERTIES_LOCATIONS);
        } catch (ConfigurationException e) {
            LOGGER.error("Failed to load settings", e);
            DEFAULT_INST_TMP = null;
        }
        DEFAULT_INST = DEFAULT_INST_TMP;
    }

    /**
     * Get settings, looks for settings in the default locations
     *
     * @return A map of settings
     */
    public static Map<String, String> getDefault() throws ConfigurationException {
        initSettings();
        if (DEFAULT_INST == null) {
            throw new ConfigurationException(NO_VALID_CONFIGURATIONS_FOUND);
        }
        return Collections.unmodifiableMap(DEFAULT_INST);
    }

    /**
     * Get settings, looks for settings in the locations plus the additional locations provided
     *
     * @param propertyFileLocations additional locations to look for settings
     * @return A client instance
     */
    public static Map<String, String> get(List<File> propertyFileLocations)
            throws ConfigurationException {
        return Collections.unmodifiableMap(loadAllSettings(propertyFileLocations));
    }

    /**
     * Get settings, looks for settings in the default locations plus the additional locations
     * provided
     *
     * @param propertyFileLocations additional locations to look for settings
     * @return A client instance
     */
    public static Map<String, String> getWithAdditional(List<File> propertyFileLocations)
            throws ConfigurationException {
        ArrayList<File> locations = new ArrayList<>(DEFAULT_PROPERTIES_LOCATIONS);
        locations.addAll(propertyFileLocations);
        return get(locations);
    }

    private static Map<String, String> loadAllSettings(List<File> propertyFileLocations)
            throws ConfigurationException {
        Properties properties = new Properties();

        if (getEnvOrProperty(CONF_PROPERTIES_NAME, null) != null) {
            // User has explicitly set CONF_PROPERTIES, so ONLY use that and System properties
            if (Files.isRegularFile(new File(CONF_PROPERTIES).toPath())) {
                try (FileInputStream fis = new FileInputStream(CONF_PROPERTIES)) {
                    properties.load(fis);
                } catch (FileNotFoundException e) {
                    throw new ConfigurationException(CONFIGURATION_NOT_FOUND, CONF_PROPERTIES);
                } catch (IOException e) {
                    throw new ConfigurationException(COULD_NOT_READ_CONFIGURATION, CONF_PROPERTIES);
                }
            }
        } else {
            // Use default locations
            propertyFileLocations.forEach(
                    file -> {
                        if (Files.isRegularFile(file.toPath())) {
                            try (FileInputStream fis = new FileInputStream(file)) {
                                properties.load(fis);
                                LOGGER.info("Added settings from: {}", file);
                            } catch (FileNotFoundException e) {
                                LOGGER.info("No config file found at: {}", file);
                            } catch (IOException e) {
                                LOGGER.info("Failed to read config file at: {}", file);
                            }
                        } else {
                            LOGGER.info("Did not find configuration file: {}", file);
                        }
                    });
        }

        // lowercase and replace _ with ., to convert environment variables to properties
        Map<String, String> envVariables =
                System.getenv().entrySet().stream()
                        .collect(
                                Collectors.toMap(
                                        key -> key.getKey().toLowerCase().replace("_", "."),
                                        Entry::getValue));

        // Overlay environment variables
        properties.putAll(envVariables);

        // Overlay system properties
        properties.putAll(System.getProperties());

        return properties.stringPropertyNames().stream()
                .collect(
                        HashMap::new,
                        (m, k) -> m.put(k, properties.getProperty(k)),
                        HashMap::putAll);
    }

    private static String getEnvOrProperty(String propertyName, String defaultValue) {
        String env = System.getenv(propertyName);
        if (env != null && !env.isEmpty()) {
            return env;
        }

        String property = System.getProperty(propertyName);
        if (property != null && !property.isEmpty()) {
            return property;
        }

        return defaultValue;
    }
}
