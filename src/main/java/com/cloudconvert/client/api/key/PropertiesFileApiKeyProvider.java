package com.cloudconvert.client.api.key;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

public class PropertiesFileApiKeyProvider implements ApiKeyProvider {

    private static final String DEFAULT_PROPERTIES_PATH = "application.properties";

    private final String apiKey;

    public PropertiesFileApiKeyProvider() throws IOException {
        this(null);
    }

    public PropertiesFileApiKeyProvider(@Nullable final String propertiesFilePath) throws IOException {
        final String notNullPropertiesFilePath = Optional.ofNullable(propertiesFilePath).orElse(DEFAULT_PROPERTIES_PATH);

        try (
            final InputStream inputStream = PropertiesFileApiKeyProvider.class.getClassLoader().getResourceAsStream(notNullPropertiesFilePath)
        ) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Could not read properties file " + propertiesFilePath + ", make sure it exists ...");
            } else {
                final Properties properties = new Properties();
                properties.load(inputStream);

                apiKey = Optional.ofNullable(properties.getProperty(API_KEY))
                    .orElseThrow(() -> new IllegalArgumentException("Could not resolve " + API_KEY + " property, make sure it is set ..."));
            }
        }
    }

    @Override
    public String provide() {
        return apiKey;
    }
}
