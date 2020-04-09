package com.cloudconvert.resource.sync;

import com.cloudconvert.client.mapper.ObjectMapperProvider;
import com.cloudconvert.client.setttings.SettingsProvider;
import com.cloudconvert.dto.request.ConvertFilesTaskRequest;
import com.cloudconvert.dto.response.OperationResponse;
import com.cloudconvert.dto.response.Pageable;
import com.cloudconvert.dto.response.TaskResponse;
import com.cloudconvert.dto.result.Result;
import com.cloudconvert.executor.RequestExecutor;
import com.cloudconvert.resource.AbstractConvertFilesResource;
import com.cloudconvert.resource.params.Filter;
import com.cloudconvert.resource.params.Include;
import com.cloudconvert.resource.params.converter.AlternativeToNameValuePairsConverter;
import com.cloudconvert.resource.params.converter.FiltersToNameValuePairsConverter;
import com.cloudconvert.resource.params.converter.IncludesToNameValuePairsConverter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
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
public class ConvertFilesResource extends AbstractConvertFilesResource<
    Result<TaskResponse>, Result<Pageable<OperationResponse>>> {

    private final RequestExecutor requestExecutor;

    private final IncludesToNameValuePairsConverter includesToNameValuePairsConverter;
    private final FiltersToNameValuePairsConverter filtersToNameValuePairsConverter;
    private final AlternativeToNameValuePairsConverter alternativeToNameValuePairsConverter;

    public ConvertFilesResource(
        final SettingsProvider settingsProvider,
        final ObjectMapperProvider objectMapperProvider, final RequestExecutor requestExecutor
    ) {
        super(settingsProvider, objectMapperProvider);

        this.requestExecutor = requestExecutor;

        this.includesToNameValuePairsConverter = new IncludesToNameValuePairsConverter();
        this.filtersToNameValuePairsConverter = new FiltersToNameValuePairsConverter();
        this.alternativeToNameValuePairsConverter = new AlternativeToNameValuePairsConverter();
    }

    @Override
    public Result<TaskResponse> convert(
        @NotNull final ConvertFilesTaskRequest convertFilesTaskRequest
    ) throws IOException, URISyntaxException {
        final URI uri = getUri(ImmutableList.of(PATH_SEGMENT_CONVERT));
        final HttpEntity httpEntity = getHttpEntity(convertFilesTaskRequest);

        return requestExecutor.execute(getHttpUriRequest(HttpPost.class, uri, httpEntity), TASK_RESPONSE_TYPE_REFERENCE);
    }

    @Override
    public Result<Pageable<OperationResponse>> convertFormats() throws IOException, URISyntaxException {
        return convertFormats(ImmutableMap.of());
    }

    @Override
    public Result<Pageable<OperationResponse>> convertFormats(
        @NotNull final Map<Filter, String> filters
    ) throws IOException, URISyntaxException {
        return convertFormats(filters, ImmutableList.of());
    }

    @Override
    public Result<Pageable<OperationResponse>> convertFormats(
        @NotNull final Map<Filter, String> filters, @NotNull final List<Include> includes
    ) throws IOException, URISyntaxException {
        return convertFormats(filters, includes, null);
    }

    @Override
    public Result<Pageable<OperationResponse>> convertFormats(
        @NotNull final Map<Filter, String> filters, @NotNull final List<Include> includes, @Nullable final Boolean alternative
    ) throws IOException, URISyntaxException {
        final List<NameValuePair> nameValuePairs = ImmutableList.<NameValuePair>builder().addAll(filtersToNameValuePairsConverter.convert(filters))
            .addAll(includesToNameValuePairsConverter.convert(includes)).addAll(alternativeToNameValuePairsConverter.convert(alternative)).build();

        final URI uri = getUri(ImmutableList.of(PATH_SEGMENT_CONVERT, PATH_SEGMENT_FORMATS), nameValuePairs);

        return requestExecutor.execute(getHttpUriRequest(HttpGet.class, uri), OPERATION_RESPONSE_PAGEABLE_TYPE_REFERENCE);
    }

    @Override
    public void close() throws IOException {
        requestExecutor.close();
    }
}
