package com.cloudconvert.resource.async;

import com.cloudconvert.client.mapper.ObjectMapperProvider;
import com.cloudconvert.client.setttings.SettingsProvider;
import com.cloudconvert.dto.request.AddWatermarkTaskRequest;
import com.cloudconvert.dto.response.TaskResponse;
import com.cloudconvert.dto.result.AsyncResult;
import com.cloudconvert.executor.AsyncRequestExecutor;
import com.cloudconvert.resource.AbstractAddWatermarkResource;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URISyntaxException;

@Slf4j
public class AsyncAddWatermarkResource extends AbstractAddWatermarkResource<AsyncResult<TaskResponse>> {

    private final AsyncRequestExecutor asyncRequestExecutor;

    public AsyncAddWatermarkResource(
        final SettingsProvider settingsProvider,
        final ObjectMapperProvider objectMapperProvider, final AsyncRequestExecutor asyncRequestExecutor
    ) {
        super(settingsProvider, objectMapperProvider);

        this.asyncRequestExecutor = asyncRequestExecutor;
    }

    @Override
    public AsyncResult<TaskResponse> watermark(
        @NotNull final AddWatermarkTaskRequest addWatermarkTaskRequest
    ) throws IOException, URISyntaxException {
        return asyncRequestExecutor.execute(getWatermarkHttpUriRequest(addWatermarkTaskRequest), TASK_RESPONSE_TYPE_REFERENCE);
    }

    @Override
    public void close() throws IOException {
        asyncRequestExecutor.close();
    }
}
