package com.cloudconvert.resource.async;

import com.cloudconvert.client.mapper.ObjectMapperProvider;
import com.cloudconvert.client.setttings.SettingsProvider;
import com.cloudconvert.dto.result.AsyncResult;
import com.cloudconvert.executor.AsyncRequestExecutor;
import com.cloudconvert.resource.AbstractFilesResource;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.URIBuilder;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

@Slf4j
public class AsyncFilesResource extends AbstractFilesResource<AsyncResult<InputStream>> {

    private final AsyncRequestExecutor asyncRequestExecutor;

    public AsyncFilesResource(
        final SettingsProvider settingsProvider,
        final ObjectMapperProvider objectMapperProvider, final AsyncRequestExecutor asyncRequestExecutor
    ) {
        super(settingsProvider, objectMapperProvider);

        this.asyncRequestExecutor = asyncRequestExecutor;
    }

    @Override
    public AsyncResult<InputStream> download(
        @NotNull final String path
    ) throws IOException, URISyntaxException {
        final URI uri = new URIBuilder(path).build();
        final HttpUriRequest httpUriRequest = RequestBuilder.get().setUri(uri).build();

        return asyncRequestExecutor.execute(httpUriRequest, INPUT_STREAM_TYPE_REFERENCE);
    }

    @Override
    public void close() throws IOException {
        asyncRequestExecutor.close();
    }
}
