package com.cloudconvert.client.api.key;

import java.util.Optional;

public class SystemPropertyApiKeyProvider implements ApiKeyProvider {

    private final String apiKey;

    public SystemPropertyApiKeyProvider() {
        apiKey = Optional.ofNullable(System.getProperty(API_KEY))
            .orElseThrow(() -> new IllegalArgumentException("Could not resolve " + API_KEY + " system property, make sure it is set ..."));
    }

    @Override
    public String provide() {
        return apiKey;
    }
}
