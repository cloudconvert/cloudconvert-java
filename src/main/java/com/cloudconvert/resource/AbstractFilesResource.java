package com.cloudconvert.resource;

import com.cloudconvert.client.mapper.ObjectMapperProvider;
import com.cloudconvert.client.setttings.SettingsProvider;
import com.cloudconvert.dto.result.AbstractResult;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

public abstract class AbstractFilesResource<ISAR extends AbstractResult<InputStream>> extends AbstractResource {

    public AbstractFilesResource(
        final SettingsProvider settingsProvider, final ObjectMapperProvider objectMapperProvider
    ) {
        super(settingsProvider, objectMapperProvider);
    }

    /**
     * Download file
     *
     * @param path path to file
     * @return IS
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract ISAR download(
        @NotNull final String path
    ) throws IOException, URISyntaxException;
}
