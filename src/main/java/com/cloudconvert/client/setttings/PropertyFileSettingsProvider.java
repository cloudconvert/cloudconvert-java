package com.cloudconvert.client.setttings;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.function.Function;

public class PropertyFileSettingsProvider extends AbstractSettingsProvider {

    private static final String DEFAULT_PROPERTIES_PATH = "application.properties";

    private static final Function<String, InputStream> PROPERTIES_FILE_PATH_TO_INPUT_STREAM = (propertiesFilePath) -> {
        final String notNullPropertiesFilePath = Optional.ofNullable(propertiesFilePath).orElse(DEFAULT_PROPERTIES_PATH);
        return Optional.ofNullable(PropertyFileSettingsProvider.class.getClassLoader().getResourceAsStream(notNullPropertiesFilePath))
            .orElseThrow(() -> new IllegalArgumentException("Could not read properties file " + propertiesFilePath + ", make sure it exists ..."));
    };

    public PropertyFileSettingsProvider() throws IOException {
        this(false);
    }

    public PropertyFileSettingsProvider(
        final boolean useSandbox
    ) throws IOException {
        this(null, useSandbox);
    }

    public PropertyFileSettingsProvider(
        @Nullable final String propertiesFilePath
    ) throws IOException {
        this(propertiesFilePath, false);
    }

    public PropertyFileSettingsProvider(
        @Nullable final String propertiesFilePath, final boolean useSandbox
    ) throws IOException {
        super(PROPERTIES_FILE_PATH_TO_INPUT_STREAM.apply(propertiesFilePath), useSandbox);
    }
}
