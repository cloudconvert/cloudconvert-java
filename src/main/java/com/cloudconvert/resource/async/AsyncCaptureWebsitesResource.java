package com.cloudconvert.resource.async;

import com.cloudconvert.client.api.key.ApiKeyProvider;
import com.cloudconvert.client.api.url.ApiUrlProvider;
import com.cloudconvert.client.mapper.ObjectMapperProvider;
import com.cloudconvert.dto.request.CaptureWebsitesTaskRequest;
import com.cloudconvert.dto.response.TaskResponseData;
import com.cloudconvert.dto.result.AsyncResult;
import com.cloudconvert.executor.AsyncRequestExecutor;
import com.cloudconvert.resource.AbstractCaptureWebsitesResource;
import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@Slf4j
public class AsyncCaptureWebsitesResource extends AbstractCaptureWebsitesResource<AsyncResult<TaskResponseData>> {

    private final AsyncRequestExecutor asyncRequestExecutor;

    public AsyncCaptureWebsitesResource(
        final ApiUrlProvider apiUrlProvider, final ApiKeyProvider apiKeyProvider,
        final ObjectMapperProvider objectMapperProvider, final AsyncRequestExecutor asyncRequestExecutor
    ) {
        super(apiUrlProvider, apiKeyProvider, objectMapperProvider);

        this.asyncRequestExecutor = asyncRequestExecutor;
    }

    @Override
    public AsyncResult<TaskResponseData> capture(
        @NotNull final CaptureWebsitesTaskRequest captureWebsitesTaskRequest
    ) throws IOException, URISyntaxException {
        final URI uri = getUri(ImmutableList.of(PATH_SEGMENT_CAPTURE_WEBSITE));
        final HttpEntity httpEntity = getHttpEntity(captureWebsitesTaskRequest);

        return asyncRequestExecutor.execute(getHttpUriRequest(HttpPost.class, uri, httpEntity), TASK_RESPONSE_DATA_TYPE_REFERENCE);
    }

    @Override
    public void close() throws IOException {
        asyncRequestExecutor.close();
    }
}
