// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.ai.openai.util;

import com.microsoft.semantickernel.exceptions.ConfigurationException;
import java.util.Map;
import javax.annotation.Nullable;

public class OpenAISettings extends AbstractOpenAIClientSettings {
    public static final String OPEN_AI_ORGANIZATION_SUFFIX = "organizationid";
    private static final String DEFAULT_SETTINGS_PREFIX = "client.openai";

    @Nullable private final String key;
    @Nullable private final String organizationId;
    private final String settingsPrefix;

    public OpenAISettings(Map<String, String> settings) {
        this(DEFAULT_SETTINGS_PREFIX, settings);
    }

    public OpenAISettings(String settingsPrefix, Map<String, String> settings) {
        this.settingsPrefix = settingsPrefix;
        key = settings.get(settingsPrefix + "." + KEY_SUFFIX);
        organizationId = settings.get(settingsPrefix + "." + OPEN_AI_ORGANIZATION_SUFFIX);
    }

    public String getKey() throws ConfigurationException {
        if (key == null) {
            throw new ConfigurationException(
                    ConfigurationException.ErrorCodes.VALUE_NOT_FOUND, "key");
        }
        return key;
    }

    public String getOrganizationId() throws ConfigurationException {
        if (organizationId == null) {
            throw new ConfigurationException(
                    ConfigurationException.ErrorCodes.VALUE_NOT_FOUND, "organizationid");
        }
        return organizationId;
    }

    @Override
    public boolean assertIsValid() throws ConfigurationException {
        if (key == null) {
            throw new ConfigurationException(
                    ConfigurationException.ErrorCodes.VALUE_NOT_FOUND,
                    settingsPrefix + "." + KEY_SUFFIX);
        }
        return true;
    }
}
