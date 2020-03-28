package com.cloudconvert.client.api.key;

import java.util.Optional;
import java.util.Properties;

public class PropertiesApiKeyProvider implements ApiKeyProvider {

    private final String apiKey;

    public PropertiesApiKeyProvider(final Properties properties) {
        apiKey = Optional.ofNullable(properties.getProperty(API_KEY))
            .orElseThrow(() -> new IllegalArgumentException("Could not resolve " + API_KEY + " property, make sure it is set ..."));
    }

    @Override
    public String provide() {
        return apiKey;
    }
}
