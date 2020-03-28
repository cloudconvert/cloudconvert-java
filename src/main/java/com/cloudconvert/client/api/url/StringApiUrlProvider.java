package com.cloudconvert.client.api.url;

import org.jetbrains.annotations.NotNull;

public class StringApiUrlProvider implements ApiUrlProvider {

    private final String apiUrl;

    public StringApiUrlProvider(@NotNull final String apiUrl) {
        this.apiUrl = apiUrl;
    }

    @Override
    public String provide() {
        return apiUrl;
    }
}
