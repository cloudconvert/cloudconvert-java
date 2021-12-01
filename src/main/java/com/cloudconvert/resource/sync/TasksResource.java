package com.cloudconvert.resource.sync;

import com.cloudconvert.client.mapper.ObjectMapperProvider;
import com.cloudconvert.client.setttings.SettingsProvider;
import com.cloudconvert.dto.request.CaptureWebsitesTaskRequest;
import com.cloudconvert.dto.request.ConvertFilesTaskRequest;
import com.cloudconvert.dto.request.CreateArchivesTaskRequest;
import com.cloudconvert.dto.request.CreateThumbnailsTaskRequest;
import com.cloudconvert.dto.request.ExecuteCommandsTaskRequest;
import com.cloudconvert.dto.request.GetMetadataTaskRequest;
import com.cloudconvert.dto.request.MergeFilesTaskRequest;
import com.cloudconvert.dto.request.OptimizeFilesTaskRequest;
import com.cloudconvert.dto.request.WriteMetadataTaskRequest;
import com.cloudconvert.dto.response.OperationResponse;
import com.cloudconvert.dto.response.Pageable;
import com.cloudconvert.dto.response.TaskResponse;
import com.cloudconvert.dto.result.Result;
import com.cloudconvert.exception.CloudConvertClientException;
import com.cloudconvert.exception.CloudConvertServerException;
import com.cloudconvert.executor.RequestExecutor;
import com.cloudconvert.resource.AbstractTasksResource;
import com.cloudconvert.resource.params.Filter;
import com.cloudconvert.resource.params.Include;
import com.cloudconvert.resource.params.Pagination;
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
public class TasksResource extends AbstractTasksResource<Result<TaskResponse>,
    Result<Pageable<TaskResponse>>, Result<Void>, Result<Pageable<OperationResponse>>> {

    private final RequestExecutor requestExecutor;

    public TasksResource(
        final SettingsProvider settingsProvider,
        final ObjectMapperProvider objectMapperProvider, final RequestExecutor requestExecutor,
        final ConvertFilesResource convertFilesResource, final OptimizeFilesResource optimizeFilesResource,
        final CaptureWebsitesResource captureWebsitesResource, final MergeFilesResource mergeFilesResource,
        final CreateArchivesResource createArchivesResource, final ExecuteCommandsResource executeCommandsResource,
        final CreateThumbnailsResource createThumbnailsResource, final GetMetadataResource getMetadataResource,
        final WriteMetadataResource writeMetadataResource
    ) {
        super(settingsProvider, objectMapperProvider, convertFilesResource, optimizeFilesResource, captureWebsitesResource,
            mergeFilesResource, createArchivesResource, executeCommandsResource, createThumbnailsResource, getMetadataResource, writeMetadataResource);

        this.requestExecutor = requestExecutor;
    }

    public Result<TaskResponse> show(
        @NotNull final String taskId
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        return show(taskId, ImmutableList.of());
    }

    public Result<TaskResponse> show(
        @NotNull final String taskId, @NotNull final List<Include> includes
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        return requestExecutor.execute(getShowHttpUriRequest(taskId, includes), TASK_RESPONSE_TYPE_REFERENCE);
    }

    public Result<TaskResponse> wait(
        @NotNull final String taskId
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        return requestExecutor.execute(getWaitHttpUriRequest(taskId), TASK_RESPONSE_TYPE_REFERENCE);
    }

    public Result<Pageable<TaskResponse>> list() throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        return list(ImmutableMap.of());
    }

    public Result<Pageable<TaskResponse>> list(
        @NotNull final Map<Filter, String> filters
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        return list(filters, ImmutableList.of());
    }

    @Override
    public Result<Pageable<TaskResponse>> list(
        @NotNull final Map<Filter, String> filters, @NotNull final List<Include> includes
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        return list(filters, includes, null);
    }

    @Override
    public Result<Pageable<TaskResponse>> list(
        @NotNull final Map<Filter, String> filters, @NotNull final List<Include> includes, @Nullable final Pagination pagination
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        return requestExecutor.execute(getListHttpUriRequest(filters, includes, pagination), TASK_RESPONSE_PAGEABLE_TYPE_REFERENCE);
    }

    public Result<TaskResponse> cancel(
        @NotNull final String taskId
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        return requestExecutor.execute(getCancelHttpUriRequest(taskId), TASK_RESPONSE_TYPE_REFERENCE);
    }

    public Result<TaskResponse> retry(
        @NotNull final String taskId
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        return requestExecutor.execute(getRetryHttpUriRequest(taskId), TASK_RESPONSE_TYPE_REFERENCE);
    }

    public Result<Void> delete(
        @NotNull final String taskId
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        return requestExecutor.execute(getDeleteHttpUriRequest(taskId), VOID_TYPE_REFERENCE);
    }

    public Result<Pageable<OperationResponse>> operations() throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        return operations(ImmutableMap.of());
    }

    public Result<Pageable<OperationResponse>> operations(
        @NotNull final Map<Filter, String> filters
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        return operations(filters, ImmutableList.of());
    }

    public Result<Pageable<OperationResponse>> operations(
        @NotNull final Map<Filter, String> filters, @NotNull final List<Include> includes
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        return operations(filters, includes, null);
    }

    public Result<Pageable<OperationResponse>> operations(
        @NotNull final Map<Filter, String> filters, @NotNull final List<Include> includes, @Nullable final Boolean alternative
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        return requestExecutor.execute(getOperationsHttpUriRequest(filters, includes, alternative), OPERATION_RESPONSE_PAGEABLE_TYPE_REFERENCE);
    }

    @Override
    public Result<TaskResponse> convert(
        @NotNull final ConvertFilesTaskRequest convertFilesTaskRequest
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        return getAbstractConvertFilesResource().convert(convertFilesTaskRequest);
    }

    @Override
    public Result<Pageable<OperationResponse>> convertFormats() throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        return getAbstractConvertFilesResource().convertFormats();
    }

    @Override
    public Result<Pageable<OperationResponse>> convertFormats(
        @NotNull final Map<Filter, String> filters
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        return getAbstractConvertFilesResource().convertFormats(filters);
    }

    @Override
    public Result<Pageable<OperationResponse>> convertFormats(
        @NotNull final Map<Filter, String> filters, @NotNull final List<Include> includes
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        return getAbstractConvertFilesResource().convertFormats(filters, includes);
    }

    @Override
    public Result<Pageable<OperationResponse>> convertFormats(
        @NotNull final Map<Filter, String> filters, @NotNull final List<Include> includes, @Nullable final Boolean alternative
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        return getAbstractConvertFilesResource().convertFormats(filters, includes, alternative);
    }

    @Override
    public Result<TaskResponse> optimize(
        @NotNull final OptimizeFilesTaskRequest optimizeFilesTaskRequest
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        return getAbstractOptimizeFilesResource().optimize(optimizeFilesTaskRequest);
    }

    @Override
    public Result<TaskResponse> capture(
        @NotNull final CaptureWebsitesTaskRequest captureWebsitesTaskRequest
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        return getAbstractCaptureWebsitesResource().capture(captureWebsitesTaskRequest);
    }

    @Override
    public Result<TaskResponse> merge(
        @NotNull final MergeFilesTaskRequest mergeFilesTaskRequest
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        return getAbstractMergeFilesResource().merge(mergeFilesTaskRequest);
    }

    @Override
    public Result<TaskResponse> archive(
        @NotNull final CreateArchivesTaskRequest createArchivesTaskRequest
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        return getAbstractCreateArchivesResource().archive(createArchivesTaskRequest);
    }

    @Override
    public Result<TaskResponse> command(
        @NotNull final ExecuteCommandsTaskRequest executeCommandsTaskRequest
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        return getAbstractExecuteCommandsResource().command(executeCommandsTaskRequest);
    }

    @Override
    public Result<TaskResponse> thumbnail(@NotNull CreateThumbnailsTaskRequest createThumbnailsTaskRequest
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        return getAbstractCreateThumbnailsResource().thumbnail(createThumbnailsTaskRequest);
    }

    @Override
    public Result<TaskResponse> metadata(@NotNull GetMetadataTaskRequest getMetadataTaskRequest
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        return getAbstractGetMetadataResource().metadata(getMetadataTaskRequest);
    }

    @Override
    public Result<TaskResponse> writeMetadata(@NotNull WriteMetadataTaskRequest writeMetadataTaskRequest
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        return getAbstractWriteMetadataResource().writeMetadata(writeMetadataTaskRequest);
    }

    @Override
    public void close() throws IOException {
        requestExecutor.close();
        super.close();
    }
}
