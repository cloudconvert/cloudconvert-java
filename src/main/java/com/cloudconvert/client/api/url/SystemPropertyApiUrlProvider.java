package com.cloudconvert.client.api.url;

import java.util.Optional;

public class SystemPropertyApiUrlProvider implements ApiUrlProvider {

    private final String apiUrl;

    public SystemPropertyApiUrlProvider() {
        apiUrl = Optional.ofNullable(System.getProperty(API_URL))
            .orElseThrow(() -> new IllegalArgumentException("Could not resolve " + API_URL + " system property, make sure it is set ..."));
    }

    @Override
    public String provide() {
        return apiUrl;
    }
}
