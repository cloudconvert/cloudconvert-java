package com.cloudconvert.resource.async;

import com.cloudconvert.client.mapper.ObjectMapperProvider;
import com.cloudconvert.client.setttings.SettingsProvider;
import com.cloudconvert.executor.AsyncRequestExecutor;
import com.cloudconvert.resource.AbstractSignedUrlResource;


import java.io.IOException;


public class AsyncSignedUrlResource extends AbstractSignedUrlResource {

    private final AsyncRequestExecutor asyncRequestExecutor;

    public AsyncSignedUrlResource(
            final SettingsProvider settingsProvider,
            final ObjectMapperProvider objectMapperProvider, final AsyncRequestExecutor asyncRequestExecutor
    ) {
        super(settingsProvider, objectMapperProvider);

        this.asyncRequestExecutor = asyncRequestExecutor;
    }

    @Override
    public void close() throws IOException {
        asyncRequestExecutor.close();
    }
}
