package com.cloudconvert.resource.async;

import com.cloudconvert.client.mapper.ObjectMapperProvider;
import com.cloudconvert.client.setttings.SettingsProvider;
import com.cloudconvert.dto.request.*;
import com.cloudconvert.dto.response.OperationResponse;
import com.cloudconvert.dto.response.Pageable;
import com.cloudconvert.dto.response.TaskResponse;
import com.cloudconvert.dto.result.AsyncResult;
import com.cloudconvert.exception.CloudConvertClientException;
import com.cloudconvert.exception.CloudConvertServerException;
import com.cloudconvert.executor.AsyncRequestExecutor;
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
public class AsyncTasksResource extends AbstractTasksResource<AsyncResult<TaskResponse>,
    AsyncResult<Pageable<TaskResponse>>, AsyncResult<Void>, AsyncResult<Pageable<OperationResponse>>> {

    private final AsyncRequestExecutor asyncRequestExecutor;

    public AsyncTasksResource(
        final SettingsProvider settingsProvider, final ObjectMapperProvider objectMapperProvider, final AsyncRequestExecutor asyncRequestExecutor,
        final AsyncConvertFilesResource asyncConvertFilesResource, final AsyncOptimizeFilesResource asyncOptimizeFilesResource,
        final AsyncCaptureWebsitesResource asyncCaptureWebsitesResource, final AsyncMergeFilesResource asyncMergeFilesResource,
        final AsyncCreateArchivesResource asyncCreateArchivesResource, final AsyncExecuteCommandsResource asyncExecuteCommandsResource,
        final AsyncCreateThumbnailsResource asyncCreateThumbnailsResource, final AsyncGetMetadataResource asyncGetMetadataResource,
        final AsyncWriteMetadataResource asyncWriteMetadataResource, final AsyncAddWatermarkResource asyncAddWatermarkResource
    ) {
        super(settingsProvider, objectMapperProvider, asyncConvertFilesResource, asyncOptimizeFilesResource, asyncCaptureWebsitesResource,
            asyncMergeFilesResource, asyncCreateArchivesResource, asyncExecuteCommandsResource, asyncCreateThumbnailsResource, asyncGetMetadataResource, asyncWriteMetadataResource, asyncAddWatermarkResource);

        this.asyncRequestExecutor = asyncRequestExecutor;
    }

    public AsyncResult<TaskResponse> show(
        @NotNull final String taskId
    ) throws IOException, URISyntaxException {
        return show(taskId, ImmutableList.of());
    }

    public AsyncResult<TaskResponse> show(
        @NotNull final String taskId, @NotNull final List<Include> includes
    ) throws IOException, URISyntaxException {
        return asyncRequestExecutor.execute(getShowHttpUriRequest(taskId, includes), TASK_RESPONSE_TYPE_REFERENCE);
    }

    public AsyncResult<TaskResponse> wait(
        @NotNull final String taskId
    ) throws IOException, URISyntaxException {
        return asyncRequestExecutor.execute(getWaitHttpUriRequest(taskId), TASK_RESPONSE_TYPE_REFERENCE);
    }

    public AsyncResult<Pageable<TaskResponse>> list() throws IOException, URISyntaxException {
        return list(ImmutableMap.of());
    }

    public AsyncResult<Pageable<TaskResponse>> list(
        @NotNull final Map<Filter, String> filters
    ) throws IOException, URISyntaxException {
        return list(filters, ImmutableList.of());
    }

    @Override
    public AsyncResult<Pageable<TaskResponse>> list(
        @NotNull final Map<Filter, String> filters, @NotNull final List<Include> includes
    ) throws IOException, URISyntaxException {
        return list(filters, includes, null);
    }

    @Override
    public AsyncResult<Pageable<TaskResponse>> list(
        @NotNull final Map<Filter, String> filters, @NotNull final List<Include> includes, @Nullable final Pagination pagination
    ) throws IOException, URISyntaxException {
        return asyncRequestExecutor.execute(getListHttpUriRequest(filters, includes, pagination), TASK_RESPONSE_PAGEABLE_TYPE_REFERENCE);
    }

    public AsyncResult<TaskResponse> cancel(
        @NotNull final String taskId
    ) throws IOException, URISyntaxException {
        return asyncRequestExecutor.execute(getCancelHttpUriRequest(taskId), TASK_RESPONSE_TYPE_REFERENCE);
    }

    public AsyncResult<TaskResponse> retry(
        @NotNull final String taskId
    ) throws IOException, URISyntaxException {
        return asyncRequestExecutor.execute(getRetryHttpUriRequest(taskId), TASK_RESPONSE_TYPE_REFERENCE);
    }

    public AsyncResult<Void> delete(
        @NotNull final String taskId
    ) throws IOException, URISyntaxException {
        return asyncRequestExecutor.execute(getDeleteHttpUriRequest(taskId), VOID_TYPE_REFERENCE);
    }

    public AsyncResult<Pageable<OperationResponse>> operations() throws IOException, URISyntaxException {
        return operations(ImmutableMap.of());
    }

    public AsyncResult<Pageable<OperationResponse>> operations(
        @NotNull final Map<Filter, String> filters
    ) throws IOException, URISyntaxException {
        return operations(filters, ImmutableList.of());
    }

    public AsyncResult<Pageable<OperationResponse>> operations(
        @NotNull final Map<Filter, String> filters, @NotNull final List<Include> includes
    ) throws IOException, URISyntaxException {
        return operations(filters, includes, null);
    }

    public AsyncResult<Pageable<OperationResponse>> operations(
        @NotNull final Map<Filter, String> filters, @NotNull final List<Include> includes, @Nullable final Boolean alternative
    ) throws IOException, URISyntaxException {
        return asyncRequestExecutor.execute(getOperationsHttpUriRequest(filters, includes, alternative), OPERATION_RESPONSE_PAGEABLE_TYPE_REFERENCE);
    }

    @Override
    public AsyncResult<TaskResponse> convert(
        @NotNull final ConvertFilesTaskRequest convertFilesTaskRequest
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        return getAbstractConvertFilesResource().convert(convertFilesTaskRequest);
    }

    @Override
    public AsyncResult<Pageable<OperationResponse>> convertFormats() throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        return getAbstractConvertFilesResource().convertFormats();
    }

    @Override
    public AsyncResult<Pageable<OperationResponse>> convertFormats(
        @NotNull final Map<Filter, String> filters
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        return getAbstractConvertFilesResource().convertFormats(filters);
    }

    @Override
    public AsyncResult<Pageable<OperationResponse>> convertFormats(
        @NotNull final Map<Filter, String> filters, @NotNull final List<Include> includes
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        return getAbstractConvertFilesResource().convertFormats(filters, includes);
    }

    @Override
    public AsyncResult<Pageable<OperationResponse>> convertFormats(
        @NotNull final Map<Filter, String> filters, @NotNull final List<Include> includes, @Nullable final Boolean alternative
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        return getAbstractConvertFilesResource().convertFormats(filters, includes, alternative);
    }

    @Override
    public AsyncResult<TaskResponse> optimize(
        @NotNull final OptimizeFilesTaskRequest optimizeFilesTaskRequest
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        return getAbstractOptimizeFilesResource().optimize(optimizeFilesTaskRequest);
    }

    @Override
    public AsyncResult<TaskResponse> capture(
        @NotNull final CaptureWebsitesTaskRequest captureWebsitesTaskRequest
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        return getAbstractCaptureWebsitesResource().capture(captureWebsitesTaskRequest);
    }

    @Override
    public AsyncResult<TaskResponse> merge(
        @NotNull final MergeFilesTaskRequest mergeFilesTaskRequest
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        return getAbstractMergeFilesResource().merge(mergeFilesTaskRequest);
    }

    @Override
    public AsyncResult<TaskResponse> archive(
        @NotNull final CreateArchivesTaskRequest createArchivesTaskRequest
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        return getAbstractCreateArchivesResource().archive(createArchivesTaskRequest);
    }

    @Override
    public AsyncResult<TaskResponse> command(
        @NotNull final ExecuteCommandsTaskRequest executeCommandsTaskRequest
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        return getAbstractExecuteCommandsResource().command(executeCommandsTaskRequest);
    }

    @Override
    public AsyncResult<TaskResponse> thumbnail(
        @NotNull final CreateThumbnailsTaskRequest createThumbnailsTaskRequest
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        return getAbstractCreateThumbnailsResource().thumbnail(createThumbnailsTaskRequest);
    }

    @Override
    public AsyncResult<TaskResponse> metadata(
        @NotNull final GetMetadataTaskRequest getMetadataTaskRequest
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        return getAbstractGetMetadataResource().metadata(getMetadataTaskRequest);
    }

    @Override
    public AsyncResult<TaskResponse> writeMetadata(
        @NotNull final WriteMetadataTaskRequest writeMetadataTaskRequest
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        return getAbstractWriteMetadataResource().writeMetadata(writeMetadataTaskRequest);
    }

    @Override
    public AsyncResult<TaskResponse> watermark(
            @NotNull final AddWatermarkTaskRequest addWatermarkTaskRequest
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        return getAbstractAddWatermarkResource().watermark(addWatermarkTaskRequest);
    }

    @Override
    public void close() throws IOException {
        asyncRequestExecutor.close();
        super.close();
    }
}
