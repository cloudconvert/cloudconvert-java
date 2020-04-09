package com.cloudconvert.resource.sync;

import com.cloudconvert.client.mapper.ObjectMapperProvider;
import com.cloudconvert.client.setttings.SettingsProvider;
import com.cloudconvert.dto.request.CaptureWebsitesTaskRequest;
import com.cloudconvert.dto.request.ConvertFilesTaskRequest;
import com.cloudconvert.dto.request.CreateArchivesTaskRequest;
import com.cloudconvert.dto.request.ExecuteCommandsTaskRequest;
import com.cloudconvert.dto.request.MergeFilesTaskRequest;
import com.cloudconvert.dto.request.OptimizeFilesTaskRequest;
import com.cloudconvert.dto.response.OperationResponse;
import com.cloudconvert.dto.response.Pageable;
import com.cloudconvert.dto.response.TaskResponse;
import com.cloudconvert.dto.result.Result;
import com.cloudconvert.executor.RequestExecutor;
import com.cloudconvert.resource.AbstractTasksResource;
import com.cloudconvert.resource.params.Filter;
import com.cloudconvert.resource.params.Include;
import com.cloudconvert.resource.params.Pagination;
import com.cloudconvert.resource.params.converter.AlternativeToNameValuePairsConverter;
import com.cloudconvert.resource.params.converter.FiltersToNameValuePairsConverter;
import com.cloudconvert.resource.params.converter.IncludesToNameValuePairsConverter;
import com.cloudconvert.resource.params.converter.PaginationToNameValuePairsConverter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

@Slf4j
public class TasksResource extends AbstractTasksResource<Result<TaskResponse>,
    Result<Pageable<TaskResponse>>, Result<Void>, Result<Pageable<OperationResponse>>> {

    private final RequestExecutor requestExecutor;

    private final IncludesToNameValuePairsConverter includesToNameValuePairsConverter;
    private final FiltersToNameValuePairsConverter filtersToNameValuePairsConverter;
    private final AlternativeToNameValuePairsConverter alternativeToNameValuePairsConverter;
    private final PaginationToNameValuePairsConverter paginationToNameValuePairsConverter;

    public TasksResource(
        final SettingsProvider settingsProvider,
        final ObjectMapperProvider objectMapperProvider, final RequestExecutor requestExecutor,
        final ConvertFilesResource convertFilesResource, final OptimizeFilesResource optimizeFilesResource,
        final CaptureWebsitesResource captureWebsitesResource, final MergeFilesResource mergeFilesResource,
        final CreateArchivesResource createArchivesResource, final ExecuteCommandsResource executeCommandsResource
    ) {
        super(settingsProvider, objectMapperProvider, convertFilesResource, optimizeFilesResource, captureWebsitesResource,
            mergeFilesResource, createArchivesResource, executeCommandsResource);

        this.requestExecutor = requestExecutor;

        this.includesToNameValuePairsConverter = new IncludesToNameValuePairsConverter();
        this.filtersToNameValuePairsConverter = new FiltersToNameValuePairsConverter();
        this.alternativeToNameValuePairsConverter = new AlternativeToNameValuePairsConverter();
        this.paginationToNameValuePairsConverter = new PaginationToNameValuePairsConverter();
    }

    public Result<TaskResponse> show(
        @NotNull final String taskId
    ) throws IOException, URISyntaxException {
        return show(taskId, ImmutableList.of());
    }

    public Result<TaskResponse> show(
        @NotNull final String taskId, @NotNull final List<Include> includes
    ) throws IOException, URISyntaxException {
        final List<NameValuePair> nameValuePairs = ImmutableList.<NameValuePair>builder().addAll(includesToNameValuePairsConverter.convert(includes)).build();

        final URI uri = getUri(ImmutableList.of(PATH_SEGMENT_TASKS, taskId), nameValuePairs);

        return requestExecutor.execute(getHttpUriRequest(HttpGet.class, uri), TASK_RESPONSE_TYPE_REFERENCE);
    }

    public Result<TaskResponse> wait(
        @NotNull final String taskId
    ) throws IOException, URISyntaxException {
        final URI uri = getUri(ImmutableList.of(PATH_SEGMENT_TASKS, taskId, PATH_SEGMENT_WAIT));

        return requestExecutor.execute(getHttpUriRequest(HttpGet.class, uri), TASK_RESPONSE_TYPE_REFERENCE);
    }

    public Result<Pageable<TaskResponse>> list() throws IOException, URISyntaxException {
        return list(ImmutableMap.of());
    }

    public Result<Pageable<TaskResponse>> list(
        @NotNull final Map<Filter, String> filters
    ) throws IOException, URISyntaxException {
        return list(filters, ImmutableList.of());
    }

    @Override
    public Result<Pageable<TaskResponse>> list(
        @NotNull final Map<Filter, String> filters, @NotNull final List<Include> includes
    ) throws IOException, URISyntaxException {
        return list(filters, includes, null);
    }

    @Override
    public Result<Pageable<TaskResponse>> list(
        @NotNull final Map<Filter, String> filters, @NotNull final List<Include> includes, @Nullable final Pagination pagination
    ) throws IOException, URISyntaxException {
        final List<NameValuePair> nameValuePairs = ImmutableList.<NameValuePair>builder().addAll(filtersToNameValuePairsConverter.convert(filters))
            .addAll(includesToNameValuePairsConverter.convert(includes)).addAll(paginationToNameValuePairsConverter.convert(pagination)).build();

        final URI uri = getUri(ImmutableList.of(PATH_SEGMENT_TASKS), nameValuePairs);

        return requestExecutor.execute(getHttpUriRequest(HttpGet.class, uri), TASK_RESPONSE_PAGEABLE_TYPE_REFERENCE);
    }

    public Result<TaskResponse> cancel(
        @NotNull final String taskId
    ) throws IOException, URISyntaxException {
        final URI uri = getUri(ImmutableList.of(PATH_SEGMENT_TASKS, taskId, PATH_SEGMENT_CANCEL));

        return requestExecutor.execute(getHttpUriRequest(HttpPost.class, uri), TASK_RESPONSE_TYPE_REFERENCE);
    }

    public Result<TaskResponse> retry(
        @NotNull final String taskId
    ) throws IOException, URISyntaxException {
        final URI uri = getUri(ImmutableList.of(PATH_SEGMENT_TASKS, taskId, PATH_SEGMENT_RETRY));

        return requestExecutor.execute(getHttpUriRequest(HttpPost.class, uri), TASK_RESPONSE_TYPE_REFERENCE);
    }

    public Result<Void> delete(
        @NotNull final String taskId
    ) throws IOException, URISyntaxException {
        final URI uri = getUri(ImmutableList.of(PATH_SEGMENT_TASKS, taskId));

        return requestExecutor.execute(getHttpUriRequest(HttpDelete.class, uri), VOID_TYPE_REFERENCE);
    }

    public Result<Pageable<OperationResponse>> operations() throws IOException, URISyntaxException {
        return operations(ImmutableMap.of());
    }

    public Result<Pageable<OperationResponse>> operations(
        @NotNull final Map<Filter, String> filters
    ) throws IOException, URISyntaxException {
        return operations(filters, ImmutableList.of());
    }

    public Result<Pageable<OperationResponse>> operations(
        @NotNull final Map<Filter, String> filters, @NotNull final List<Include> includes
    ) throws IOException, URISyntaxException {
        return operations(filters, includes, null);
    }

    public Result<Pageable<OperationResponse>> operations(
        @NotNull final Map<Filter, String> filters, @NotNull final List<Include> includes, @Nullable final Boolean alternative
    ) throws IOException, URISyntaxException {
        final List<NameValuePair> nameValuePairs = ImmutableList.<NameValuePair>builder().addAll(filtersToNameValuePairsConverter.convert(filters))
            .addAll(includesToNameValuePairsConverter.convert(includes)).addAll(alternativeToNameValuePairsConverter.convert(alternative)).build();

        final URI uri = getUri(ImmutableList.of(PATH_SEGMENT_OPERATIONS), nameValuePairs);

        return requestExecutor.execute(getHttpUriRequest(HttpGet.class, uri), OPERATION_RESPONSE_PAGEABLE_TYPE_REFERENCE);
    }

    @Override
    public Result<TaskResponse> convert(
        @NotNull final ConvertFilesTaskRequest convertFilesTaskRequest
    ) throws IOException, URISyntaxException {
        return getAbstractConvertFilesResource().convert(convertFilesTaskRequest);
    }

    @Override
    public Result<Pageable<OperationResponse>> convertFormats() throws IOException, URISyntaxException {
        return getAbstractConvertFilesResource().convertFormats();
    }

    @Override
    public Result<Pageable<OperationResponse>> convertFormats(
        @NotNull final Map<Filter, String> filters
    ) throws IOException, URISyntaxException {
        return getAbstractConvertFilesResource().convertFormats(filters);
    }

    @Override
    public Result<Pageable<OperationResponse>> convertFormats(
        @NotNull final Map<Filter, String> filters, @NotNull final List<Include> includes
    ) throws IOException, URISyntaxException {
        return getAbstractConvertFilesResource().convertFormats(filters, includes);
    }

    @Override
    public Result<Pageable<OperationResponse>> convertFormats(
        @NotNull final Map<Filter, String> filters, @NotNull final List<Include> includes, @Nullable final Boolean alternative
    ) throws IOException, URISyntaxException {
        return getAbstractConvertFilesResource().convertFormats(filters, includes, alternative);
    }

    @Override
    public Result<TaskResponse> optimize(
        @NotNull final OptimizeFilesTaskRequest optimizeFilesTaskRequest
    ) throws IOException, URISyntaxException {
        return getAbstractOptimizeFilesResource().optimize(optimizeFilesTaskRequest);
    }

    @Override
    public Result<TaskResponse> capture(
        @NotNull final CaptureWebsitesTaskRequest captureWebsitesTaskRequest
    ) throws IOException, URISyntaxException {
        return getAbstractCaptureWebsitesResource().capture(captureWebsitesTaskRequest);
    }

    @Override
    public Result<TaskResponse> merge(
        @NotNull final MergeFilesTaskRequest mergeFilesTaskRequest
    ) throws IOException, URISyntaxException {
        return getAbstractMergeFilesResource().merge(mergeFilesTaskRequest);
    }

    @Override
    public Result<TaskResponse> archive(
        @NotNull final CreateArchivesTaskRequest createArchivesTaskRequest
    ) throws IOException, URISyntaxException {
        return getAbstractCreateArchivesResource().archive(createArchivesTaskRequest);
    }

    @Override
    public Result<TaskResponse> command(
        @NotNull final ExecuteCommandsTaskRequest executeCommandsTaskRequest
    ) throws IOException, URISyntaxException {
        return getAbstractExecuteCommandsResource().command(executeCommandsTaskRequest);
    }

    @Override
    public void close() throws IOException {
        requestExecutor.close();
        super.close();
    }
}
