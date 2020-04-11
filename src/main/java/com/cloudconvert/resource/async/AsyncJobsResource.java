package com.cloudconvert.resource.async;

import com.cloudconvert.client.mapper.ObjectMapperProvider;
import com.cloudconvert.client.setttings.SettingsProvider;
import com.cloudconvert.dto.request.TaskRequest;
import com.cloudconvert.dto.response.JobResponse;
import com.cloudconvert.dto.response.Pageable;
import com.cloudconvert.dto.result.AsyncResult;
import com.cloudconvert.executor.AsyncRequestExecutor;
import com.cloudconvert.resource.AbstractJobsResource;
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
public class AsyncJobsResource extends AbstractJobsResource<
    AsyncResult<JobResponse>, AsyncResult<Pageable<JobResponse>>, AsyncResult<Void>> {

    private final AsyncRequestExecutor asyncRequestExecutor;


    public AsyncJobsResource(
        final SettingsProvider settingsProvider,
        final ObjectMapperProvider objectMapperProvider, final AsyncRequestExecutor asyncRequestExecutor
    ) {
        super(settingsProvider, objectMapperProvider);

        this.asyncRequestExecutor = asyncRequestExecutor;
    }

    @Override
    public AsyncResult<JobResponse> create(
        @NotNull final Map<String, TaskRequest> tasks
    ) throws IOException, URISyntaxException {
        return create(tasks, "");
    }

    @Override
    public AsyncResult<JobResponse> create(
        @NotNull final Map<String, TaskRequest> tasks, @NotNull final String tag
    ) throws IOException, URISyntaxException {
        return asyncRequestExecutor.execute(getCreateHttpUriRequest(tasks, tag), JOB_RESPONSE_TYPE_REFERENCE);
    }

    @Override
    public AsyncResult<JobResponse> show(
        @NotNull final String jobId
    ) throws IOException, URISyntaxException {
        return asyncRequestExecutor.execute(getShowHttpUriRequest(jobId), JOB_RESPONSE_TYPE_REFERENCE);
    }

    @Override
    public AsyncResult<JobResponse> wait(
        @NotNull final String jobId
    ) throws IOException, URISyntaxException {
        return asyncRequestExecutor.execute(getWaitHttpUriRequest(jobId), JOB_RESPONSE_TYPE_REFERENCE);
    }

    @Override
    public AsyncResult<Pageable<JobResponse>> list() throws IOException, URISyntaxException {
        return list(ImmutableMap.of());
    }

    @Override
    public AsyncResult<Pageable<JobResponse>> list(
        @NotNull final Map<Filter, String> filters
    ) throws IOException, URISyntaxException {
        return list(filters, ImmutableList.of());
    }

    @Override
    public AsyncResult<Pageable<JobResponse>> list(
        @NotNull final Map<Filter, String> filters, @NotNull final List<Include> includes
    ) throws IOException, URISyntaxException {
        return list(filters, includes, null);
    }

    @Override
    public AsyncResult<Pageable<JobResponse>> list(
        @NotNull final Map<Filter, String> filters, @NotNull final List<Include> includes, @Nullable final Pagination pagination
    ) throws IOException, URISyntaxException {
        return asyncRequestExecutor.execute(getListHttpUriRequest(filters, includes, pagination), JOB_RESPONSE_PAGEABLE_TYPE_REFERENCE);
    }

    @Override
    public AsyncResult<Void> delete(
        @NotNull final String jobId
    ) throws IOException, URISyntaxException {
        return asyncRequestExecutor.execute(getDeleteHttpUriRequest(jobId), VOID_TYPE_REFERENCE);
    }

    @Override
    public void close() throws IOException {
        asyncRequestExecutor.close();
    }
}
