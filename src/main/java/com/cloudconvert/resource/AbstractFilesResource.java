package com.cloudconvert.resource;

import com.cloudconvert.client.api.key.ApiKeyProvider;
import com.cloudconvert.client.api.url.ApiUrlProvider;
import com.cloudconvert.client.mapper.ObjectMapperProvider;
import com.cloudconvert.dto.result.AbstractResult;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

public abstract class AbstractFilesResource<ISAR extends AbstractResult<InputStream>> extends AbstractResource {

    public AbstractFilesResource(
        final ApiUrlProvider apiUrlProvider, final ApiKeyProvider apiKeyProvider, final ObjectMapperProvider objectMapperProvider
    ) {
        super(apiUrlProvider, apiKeyProvider, objectMapperProvider);
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
