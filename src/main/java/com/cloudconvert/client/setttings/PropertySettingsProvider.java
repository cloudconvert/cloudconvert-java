package com.cloudconvert.client.setttings;

import org.jetbrains.annotations.NotNull;

import java.util.Properties;

public class PropertySettingsProvider extends AbstractSettingsProvider {

    public PropertySettingsProvider(
        @NotNull final Properties properties
    ) {
        this(properties, false);
    }

    public PropertySettingsProvider(
        @NotNull final Properties properties, final boolean useSandbox
    ) {
        super(properties.getProperty(API_KEY), properties.getProperty(WEBHOOK_SIGNING_SECRET), useSandbox);
    }
}
