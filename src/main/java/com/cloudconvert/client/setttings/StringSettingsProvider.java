package com.cloudconvert.client.setttings;

import org.jetbrains.annotations.NotNull;

public class StringSettingsProvider extends AbstractSettingsProvider {

    public StringSettingsProvider(
        @NotNull final String apiKey, @NotNull final String webhookSigningSecret, final boolean useSandbox
    ) {
        super(apiKey, webhookSigningSecret, String.valueOf(useSandbox));
    }
}
