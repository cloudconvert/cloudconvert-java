package com.cloudconvert.client.setttings;

public class EnvironmentVariableSettingsProvider extends AbstractSettingsProvider {

    private final EnvironmentVariables environmentVariables;

    public EnvironmentVariableSettingsProvider() {
        this(new SystemEnvironmentVariables());
    }

    public EnvironmentVariableSettingsProvider(EnvironmentVariables environmentVariables) {
        super(environmentVariables.getenv(API_KEY), environmentVariables.getenv(WEBHOOK_SIGNING_SECRET), environmentVariables.getenv(USE_SANDBOX));
        this.environmentVariables = environmentVariables;
    }
}
