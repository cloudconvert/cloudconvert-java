package com.cloudconvert.resource.sync;

import com.cloudconvert.client.mapper.ObjectMapperProvider;
import com.cloudconvert.client.setttings.SettingsProvider;
import com.cloudconvert.dto.request.GetMetadataTaskRequest;
import com.cloudconvert.dto.response.TaskResponse;
import com.cloudconvert.dto.result.Result;
import com.cloudconvert.exception.CloudConvertClientException;
import com.cloudconvert.exception.CloudConvertServerException;
import com.cloudconvert.executor.RequestExecutor;
import com.cloudconvert.resource.AbstractGetMetadataResource;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URISyntaxException;

@Slf4j
public class GetMetadataResource extends AbstractGetMetadataResource<Result<TaskResponse>> {

    private final RequestExecutor requestExecutor;

    public GetMetadataResource(
        final SettingsProvider settingsProvider,
        final ObjectMapperProvider objectMapperProvider, final RequestExecutor requestExecutor
    ) {
        super(settingsProvider, objectMapperProvider);

        this.requestExecutor = requestExecutor;
    }

    @Override
    public Result<TaskResponse> metadata(
        @NotNull final GetMetadataTaskRequest getMetadataTaskRequest
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        return requestExecutor.execute(getMetadataHttpUriRequest(getMetadataTaskRequest), TASK_RESPONSE_TYPE_REFERENCE);
    }

    @Override
    public void close() throws IOException {
        requestExecutor.close();
    }
}
