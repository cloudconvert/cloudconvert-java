package com.cloudconvert.client.setttings;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

public abstract class AbstractSettingsProvider implements SettingsProvider {

    public static final String API_KEY = "CLOUDCONVERT_API_KEY";
    public static final String WEBHOOK_SIGNING_SECRET = "CLOUDCONVERT_WEBHOOK_SIGNING_SECRET";
    public static final String API_URL_SANDBOX = "https://api.sandbox.cloudconvert.com/v2";
    public static final String API_URL_LIVE = "https://api.cloudconvert.com/v2";

    private String apiKey;
    private String webhookSigningSecret;
    private String apiUrl;

    protected AbstractSettingsProvider(
        final String apiKey, final String webhookSigningSecret, final boolean useSandbox
    ) {
        init(apiKey, webhookSigningSecret, useSandbox);
    }

    protected AbstractSettingsProvider(
        @NotNull final InputStream inputStream, final boolean useSandbox
    ) throws IOException {
        try (final InputStream tryWithResourceInputStream = inputStream) {
            final Properties properties = new Properties();
            properties.load(tryWithResourceInputStream);

            init(properties.getProperty(API_KEY), properties.getProperty(WEBHOOK_SIGNING_SECRET), useSandbox);
        }
    }

    private void init(final String apiKey, final String webhookSigningSecret, final boolean useSandbox) {
        this.apiKey = Optional.ofNullable(apiKey)
            .orElseThrow(() -> new IllegalArgumentException("Could not resolve " + API_KEY + ", make sure it is set correctly ..."));
        this.webhookSigningSecret = Optional.ofNullable(webhookSigningSecret)
            .orElseThrow(() -> new IllegalArgumentException("Could not resolve " + WEBHOOK_SIGNING_SECRET + ", make sure it is set correctly ..."));
        this.apiUrl = useSandbox ? API_URL_SANDBOX : API_URL_LIVE;
    }

    @Override
    public String getApiKey() {
        return apiKey;
    }

    @Override
    public String getWebhookSigningSecret() {
        return webhookSigningSecret;
    }

    @Override
    public String getApiUrl() {
        return apiUrl;
    }
}
