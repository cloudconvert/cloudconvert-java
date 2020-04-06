package com.cloudconvert.resource.sync;

import com.cloudconvert.client.mapper.ObjectMapperProvider;
import com.cloudconvert.client.setttings.SettingsProvider;
import com.cloudconvert.dto.request.ExecuteCommandsTaskRequest;
import com.cloudconvert.dto.response.TaskResponseData;
import com.cloudconvert.dto.result.Result;
import com.cloudconvert.executor.RequestExecutor;
import com.cloudconvert.resource.AbstractExecuteCommandsResource;
import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@Slf4j
public class ExecuteCommandsResource extends AbstractExecuteCommandsResource<Result<TaskResponseData>> {

    private final RequestExecutor requestExecutor;

    public ExecuteCommandsResource(
        final SettingsProvider settingsProvider,
        final ObjectMapperProvider objectMapperProvider, final RequestExecutor requestExecutor
    ) {
        super(settingsProvider, objectMapperProvider);

        this.requestExecutor = requestExecutor;
    }

    @Override
    public Result<TaskResponseData> command(
        @NotNull final ExecuteCommandsTaskRequest executeCommandsTaskRequest
    ) throws IOException, URISyntaxException {
        final URI uri = getUri(ImmutableList.of(PATH_SEGMENT_COMMAND));
        final HttpEntity httpEntity = getHttpEntity(executeCommandsTaskRequest);

        return requestExecutor.execute(getHttpUriRequest(HttpPost.class, uri, httpEntity), TASK_RESPONSE_DATA_TYPE_REFERENCE);
    }

    @Override
    public void close() throws IOException {
        requestExecutor.close();
    }
}
