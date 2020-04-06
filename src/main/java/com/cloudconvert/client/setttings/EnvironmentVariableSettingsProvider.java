package com.cloudconvert.client.setttings;

public class EnvironmentVariableSettingsProvider extends AbstractSettingsProvider {

    public EnvironmentVariableSettingsProvider() {
        super(System.getenv(API_KEY), System.getenv(WEBHOOK_SIGNING_SECRET), System.getenv(USE_SANDBOX));
    }
}
