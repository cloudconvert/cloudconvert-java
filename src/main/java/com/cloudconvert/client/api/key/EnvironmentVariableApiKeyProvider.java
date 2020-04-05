package com.cloudconvert.client.api.key;

import java.util.Optional;

public class EnvironmentVariableApiKeyProvider implements ApiKeyProvider {

    private final String apiKey;

    public EnvironmentVariableApiKeyProvider() {
        apiKey = Optional.ofNullable(System.getenv(API_KEY))
            .orElseThrow(() -> new IllegalArgumentException("Could not resolve " + API_KEY + " environment variable, make sure it is set ..."));
    }

    @Override
    public String provide() {
        return apiKey;
    }
}
