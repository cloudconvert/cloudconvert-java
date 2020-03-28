package com.cloudconvert.client.api.url;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

public class PropertiesFileApiUrlProvider implements ApiUrlProvider {

    private static final String DEFAULT_PROPERTIES_PATH = "application.properties";

    private final String apiUrl;

    public PropertiesFileApiUrlProvider() throws IOException {
        this(null);
    }

    public PropertiesFileApiUrlProvider(@Nullable final String propertiesFilePath) throws IOException {
        final String notNullPropertiesFilePath = Optional.ofNullable(propertiesFilePath).orElse(DEFAULT_PROPERTIES_PATH);

        try (
            final InputStream inputStream = PropertiesFileApiUrlProvider.class.getClassLoader().getResourceAsStream(notNullPropertiesFilePath)
        ) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Could not read properties file " + propertiesFilePath + ", make sure it exists ...");
            } else {
                final Properties properties = new Properties();
                properties.load(inputStream);

                apiUrl = Optional.ofNullable(properties.getProperty(API_URL))
                    .orElseThrow(() -> new IllegalArgumentException("Could not resolve " + API_URL + " property, make sure it is set ..."));
            }
        }
    }

    @Override
    public String provide() {
        return apiUrl;
    }
}
