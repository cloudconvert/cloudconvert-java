package com.cloudconvert.client.setttings;

public class SystemPropertySettingsProvider extends AbstractSettingsProvider {

    public SystemPropertySettingsProvider() {
        super(System.getProperty(API_KEY), System.getProperty(WEBHOOK_SIGNING_SECRET), System.getProperty(USE_SANDBOX));
    }
}
