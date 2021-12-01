package com.cloudconvert.resource.sync;

import com.cloudconvert.client.mapper.ObjectMapperProvider;
import com.cloudconvert.client.setttings.SettingsProvider;
import com.cloudconvert.dto.request.ConvertFilesTaskRequest;
import com.cloudconvert.dto.response.OperationResponse;
import com.cloudconvert.dto.response.Pageable;
import com.cloudconvert.dto.response.TaskResponse;
import com.cloudconvert.dto.result.Result;
import com.cloudconvert.exception.CloudConvertClientException;
import com.cloudconvert.exception.CloudConvertServerException;
import com.cloudconvert.executor.RequestExecutor;
import com.cloudconvert.resource.AbstractConvertFilesResource;
import com.cloudconvert.resource.params.Filter;
import com.cloudconvert.resource.params.Include;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

@Slf4j
public class ConvertFilesResource extends AbstractConvertFilesResource<
    Result<TaskResponse>, Result<Pageable<OperationResponse>>> {

    private final RequestExecutor requestExecutor;

    public ConvertFilesResource(
        final SettingsProvider settingsProvider,
        final ObjectMapperProvider objectMapperProvider, final RequestExecutor requestExecutor
    ) {
        super(settingsProvider, objectMapperProvider);

        this.requestExecutor = requestExecutor;
    }

    @Override
    public Result<TaskResponse> convert(
        @NotNull final ConvertFilesTaskRequest convertFilesTaskRequest
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        return requestExecutor.execute(getConvertHttpUriRequest(convertFilesTaskRequest), TASK_RESPONSE_TYPE_REFERENCE);
    }

    @Override
    public Result<Pageable<OperationResponse>> convertFormats() throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        return convertFormats(ImmutableMap.of());
    }

    @Override
    public Result<Pageable<OperationResponse>> convertFormats(
        @NotNull final Map<Filter, String> filters
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        return convertFormats(filters, ImmutableList.of());
    }

    @Override
    public Result<Pageable<OperationResponse>> convertFormats(
        @NotNull final Map<Filter, String> filters, @NotNull final List<Include> includes
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        return convertFormats(filters, includes, null);
    }

    @Override
    public Result<Pageable<OperationResponse>> convertFormats(
        @NotNull final Map<Filter, String> filters, @NotNull final List<Include> includes, @Nullable final Boolean alternative
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        return requestExecutor.execute(getConvertFormatsHttpUriRequest(filters, includes, alternative), OPERATION_RESPONSE_PAGEABLE_TYPE_REFERENCE);
    }

    @Override
    public void close() throws IOException {
        requestExecutor.close();
    }
}
