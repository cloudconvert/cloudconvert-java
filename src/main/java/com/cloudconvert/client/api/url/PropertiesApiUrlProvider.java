package com.cloudconvert.client.api.url;

import java.util.Optional;
import java.util.Properties;

public class PropertiesApiUrlProvider implements ApiUrlProvider {

    private final String apiUrl;

    public PropertiesApiUrlProvider(final Properties properties) {
        apiUrl = Optional.ofNullable(properties.getProperty(API_URL))
            .orElseThrow(() -> new IllegalArgumentException("Could not resolve " + API_URL + " property, make sure it is set ..."));
    }

    @Override
    public String provide() {
        return apiUrl;
    }
}
