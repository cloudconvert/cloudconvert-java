package com.cloudconvert.resource.async;

import com.cloudconvert.client.mapper.ObjectMapperProvider;
import com.cloudconvert.client.setttings.SettingsProvider;
import com.cloudconvert.dto.request.OptimizeFilesTaskRequest;
import com.cloudconvert.dto.response.TaskResponseData;
import com.cloudconvert.dto.result.AsyncResult;
import com.cloudconvert.executor.AsyncRequestExecutor;
import com.cloudconvert.resource.AbstractOptimizeFilesResource;
import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@Slf4j
public class AsyncOptimizeFilesResource extends AbstractOptimizeFilesResource<AsyncResult<TaskResponseData>> {

    private final AsyncRequestExecutor asyncRequestExecutor;

    public AsyncOptimizeFilesResource(
        final SettingsProvider settingsProvider,
        final ObjectMapperProvider objectMapperProvider, final AsyncRequestExecutor asyncRequestExecutor
    ) {
        super(settingsProvider, objectMapperProvider);

        this.asyncRequestExecutor = asyncRequestExecutor;
    }

    @Override
    public AsyncResult<TaskResponseData> optimize(
        @NotNull final OptimizeFilesTaskRequest optimizeFilesTaskRequest
    ) throws IOException, URISyntaxException {
        final URI uri = getUri(ImmutableList.of(PATH_SEGMENT_OPTIMIZE));
        final HttpEntity httpEntity = getHttpEntity(optimizeFilesTaskRequest);

        return asyncRequestExecutor.execute(getHttpUriRequest(HttpPost.class, uri, httpEntity), TASK_RESPONSE_DATA_TYPE_REFERENCE);
    }

    @Override
    public void close() throws IOException {
        asyncRequestExecutor.close();
    }
}
