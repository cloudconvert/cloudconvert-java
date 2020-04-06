package com.cloudconvert.resource.sync;

import com.cloudconvert.client.mapper.ObjectMapperProvider;
import com.cloudconvert.client.setttings.SettingsProvider;
import com.cloudconvert.dto.request.TaskRequest;
import com.cloudconvert.dto.response.JobResponse;
import com.cloudconvert.dto.response.JobResponseData;
import com.cloudconvert.dto.response.Pageable;
import com.cloudconvert.dto.result.Result;
import com.cloudconvert.executor.RequestExecutor;
import com.cloudconvert.resource.AbstractJobsResource;
import com.cloudconvert.resource.params.Filter;
import com.cloudconvert.resource.params.Include;
import com.cloudconvert.resource.params.Pagination;
import com.cloudconvert.resource.params.converter.FiltersToNameValuePairsConverter;
import com.cloudconvert.resource.params.converter.IncludesToNameValuePairsConverter;
import com.cloudconvert.resource.params.converter.PaginationToNameValuePairsConverter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class JobsResource extends AbstractJobsResource<
    Result<JobResponseData>, Result<Pageable<JobResponse>>, Result<Void>> {

    private final RequestExecutor requestExecutor;
    private final ObjectMapperProvider objectMapperProvider;

    private final IncludesToNameValuePairsConverter includesToNameValuePairsConverter;
    private final FiltersToNameValuePairsConverter filtersToNameValuePairsConverter;
    private final PaginationToNameValuePairsConverter paginationToNameValuePairsConverter;

    public JobsResource(
        final SettingsProvider settingsProvider,
        final ObjectMapperProvider objectMapperProvider, final RequestExecutor requestExecutor
    ) {
        super(settingsProvider, objectMapperProvider);

        this.requestExecutor = requestExecutor;
        this.objectMapperProvider = objectMapperProvider;

        this.includesToNameValuePairsConverter = new IncludesToNameValuePairsConverter();
        this.filtersToNameValuePairsConverter = new FiltersToNameValuePairsConverter();
        this.paginationToNameValuePairsConverter = new PaginationToNameValuePairsConverter();
    }

    @Override
    public Result<JobResponseData> create(
        @NotNull final Map<String, TaskRequest> tasks
    ) throws IOException, URISyntaxException {
        return create(tasks, "");
    }

    @Override
    public Result<JobResponseData> create(
        @NotNull final Map<String, TaskRequest> tasks, @NotNull final String tag
    ) throws IOException, URISyntaxException {
        final URI uri = getUri(ImmutableList.of(PATH_SEGMENT_JOBS));

        final Map<String, Object> tasksAsJson = tasks.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> objectMapperProvider.provide().convertValue(entry.getValue(), MAP_STRING_TO_OBJECT_TYPE_REFERENCE)));

        final HttpEntity httpEntity = new ByteArrayEntity(objectMapperProvider.provide()
            .writeValueAsBytes(ImmutableMap.of("tasks", tasksAsJson, "tag", tag)), ContentType.APPLICATION_JSON);

        return requestExecutor.execute(getHttpUriRequest(HttpPost.class, uri, httpEntity), JOB_RESPONSE_DATA_TYPE_REFERENCE);
    }

    @Override
    public Result<JobResponseData> show(
        @NotNull final String jobId
    ) throws IOException, URISyntaxException {
        final URI uri = getUri(ImmutableList.of(PATH_SEGMENT_JOBS, jobId));

        return requestExecutor.execute(getHttpUriRequest(HttpGet.class, uri), JOB_RESPONSE_DATA_TYPE_REFERENCE);
    }

    @Override
    public Result<JobResponseData> wait(
        @NotNull final String jobId
    ) throws IOException, URISyntaxException {
        final URI uri = getUri(ImmutableList.of(PATH_SEGMENT_JOBS, jobId, PATH_SEGMENT_WAIT));

        return requestExecutor.execute(getHttpUriRequest(HttpGet.class, uri), JOB_RESPONSE_DATA_TYPE_REFERENCE);
    }

    @Override
    public Result<Pageable<JobResponse>> list() throws IOException, URISyntaxException {
        return list(ImmutableMap.of());
    }

    @Override
    public Result<Pageable<JobResponse>> list(
        @NotNull final Map<Filter, String> filters
    ) throws IOException, URISyntaxException {
        return list(filters, ImmutableList.of());
    }

    @Override
    public Result<Pageable<JobResponse>> list(
        @NotNull final Map<Filter, String> filters, @NotNull final List<Include> includes
    ) throws IOException, URISyntaxException {
        return list(filters, includes, null);
    }

    @Override
    public Result<Pageable<JobResponse>> list(
        @NotNull final Map<Filter, String> filters, @NotNull final List<Include> includes, @Nullable final Pagination pagination
    ) throws IOException, URISyntaxException {
        final List<NameValuePair> nameValuePairs = ImmutableList.<NameValuePair>builder().addAll(filtersToNameValuePairsConverter.convert(filters))
            .addAll(includesToNameValuePairsConverter.convert(includes)).addAll(paginationToNameValuePairsConverter.convert(pagination)).build();

        final URI uri = getUri(ImmutableList.of(PATH_SEGMENT_JOBS), nameValuePairs);

        return requestExecutor.execute(getHttpUriRequest(HttpGet.class, uri), JOB_RESPONSE_PAGEABLE_TYPE_REFERENCE);
    }

    @Override
    public Result<Void> delete(
        @NotNull final String jobId
    ) throws IOException, URISyntaxException {
        final URI uri = getUri(ImmutableList.of(PATH_SEGMENT_JOBS, jobId));

        return requestExecutor.execute(getHttpUriRequest(HttpDelete.class, uri), VOID_TYPE_REFERENCE);
    }

    @Override
    public void close() throws IOException {
        requestExecutor.close();
    }
}
