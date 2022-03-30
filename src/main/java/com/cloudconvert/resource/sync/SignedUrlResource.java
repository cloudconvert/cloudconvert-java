package com.cloudconvert.resource.sync;

import com.cloudconvert.client.mapper.ObjectMapperProvider;
import com.cloudconvert.client.setttings.SettingsProvider;
import com.cloudconvert.executor.RequestExecutor;
import com.cloudconvert.resource.AbstractSignedUrlResource;

import java.io.IOException;

public class SignedUrlResource extends AbstractSignedUrlResource {

    private final RequestExecutor requestExecutor;

    public SignedUrlResource(
        final SettingsProvider settingsProvider,
        final ObjectMapperProvider objectMapperProvider, final RequestExecutor requestExecutor
    ) {
        super(settingsProvider, objectMapperProvider);

        this.requestExecutor = requestExecutor;
    }

    @Override
    public void close() throws IOException {
        requestExecutor.close();
    }
}
