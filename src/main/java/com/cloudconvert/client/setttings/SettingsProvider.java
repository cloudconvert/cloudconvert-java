package com.cloudconvert.client.setttings;

/**
 * CloudConvert specific settings provider
 */
public interface SettingsProvider {

    String API_KEY = "CLOUDCONVERT_API_KEY";
    String WEBHOOK_SIGNING_SECRET = "CLOUDCONVERT_WEBHOOK_SIGNING_SECRET";

    String getApiKey();

    String getApiUrl();

    String getWebhookSigningSecret();
}
