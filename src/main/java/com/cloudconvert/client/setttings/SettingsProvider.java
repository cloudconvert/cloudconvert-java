package com.cloudconvert.client.setttings;

/**
 * CloudConvert specific settings provider
 */
public interface SettingsProvider {

    String getApiKey();

    String getApiUrl();

    String getWebhookSigningSecret();
}
