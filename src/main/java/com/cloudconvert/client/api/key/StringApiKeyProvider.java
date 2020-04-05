package com.cloudconvert.client.api.key;

import org.jetbrains.annotations.NotNull;

public class StringApiKeyProvider implements ApiKeyProvider {

    private final String apiKey;

    public StringApiKeyProvider(@NotNull final String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public String provide() {
        return apiKey;
    }
}
