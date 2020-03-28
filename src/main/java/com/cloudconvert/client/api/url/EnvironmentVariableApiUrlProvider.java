package com.cloudconvert.client.api.url;

import java.util.Optional;

public class EnvironmentVariableApiUrlProvider implements ApiUrlProvider {

    private final String apiUrl;

    public EnvironmentVariableApiUrlProvider() {
        apiUrl = Optional.ofNullable(System.getenv(API_URL))
            .orElseThrow(() -> new IllegalArgumentException("Could not resolve " + API_URL + " environment variable, make sure it is set ..."));
    }

    @Override
    public String provide() {
        return apiUrl;
    }
}
