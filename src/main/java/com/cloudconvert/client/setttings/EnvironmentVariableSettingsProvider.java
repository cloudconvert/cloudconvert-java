package com.cloudconvert.client.setttings;

public class EnvironmentVariableSettingsProvider extends AbstractSettingsProvider {

    public EnvironmentVariableSettingsProvider() {
        this(false);
    }

    public EnvironmentVariableSettingsProvider(
        final boolean useSandbox
    ) {
        super(System.getenv(API_KEY), System.getenv(WEBHOOK_SIGNING_SECRET), useSandbox);
    }
}
