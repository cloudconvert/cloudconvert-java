package com.cloudconvert.client.setttings;

public class SystemPropertySettingsProvider extends AbstractSettingsProvider {

    public SystemPropertySettingsProvider() {
        this(false);
    }

    public SystemPropertySettingsProvider(
        final boolean useSandbox
    ) {
        super(System.getProperty(API_KEY), System.getProperty(WEBHOOK_SIGNING_SECRET), useSandbox);
    }
}
