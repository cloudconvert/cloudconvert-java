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
        this(null);
    }

    public PropertyFileSettingsProvider(
        @Nullable final String propertiesFilePath
    ) throws IOException {
        super(PROPERTIES_FILE_PATH_TO_INPUT_STREAM.apply(propertiesFilePath));
    }
}
