package com.cloudconvert.resource.async;

import com.cloudconvert.client.api.key.ApiKeyProvider;
import com.cloudconvert.client.api.url.ApiUrlProvider;
import com.cloudconvert.client.mapper.ObjectMapperProvider;
import com.cloudconvert.dto.request.ConvertFilesTaskRequest;
import com.cloudconvert.dto.response.OperationResponse;
import com.cloudconvert.dto.response.Pageable;
import com.cloudconvert.dto.response.TaskResponseData;
import com.cloudconvert.dto.result.AsyncResult;
import com.cloudconvert.executor.AsyncRequestExecutor;
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
public class AsyncConvertFilesResource extends AbstractConvertFilesResource<
    AsyncResult<TaskResponseData>, AsyncResult<Pageable<OperationResponse>>> {

    private final AsyncRequestExecutor asyncRequestExecutor;

    private final IncludesToNameValuePairsConverter includesToNameValuePairsConverter;
    private final FiltersToNameValuePairsConverter filtersToNameValuePairsConverter;
    private final AlternativeToNameValuePairsConverter alternativeToNameValuePairsConverter;

    public AsyncConvertFilesResource(
        final ApiUrlProvider apiUrlProvider, final ApiKeyProvider apiKeyProvider,
        final ObjectMapperProvider objectMapperProvider, final AsyncRequestExecutor asyncRequestExecutor
    ) {
        super(apiUrlProvider, apiKeyProvider, objectMapperProvider);

        this.asyncRequestExecutor = asyncRequestExecutor;

        this.includesToNameValuePairsConverter = new IncludesToNameValuePairsConverter();
        this.filtersToNameValuePairsConverter = new FiltersToNameValuePairsConverter();
        this.alternativeToNameValuePairsConverter = new AlternativeToNameValuePairsConverter();
    }

    @Override
    public AsyncResult<TaskResponseData> convert(
        @NotNull final ConvertFilesTaskRequest convertFilesTaskRequest
    ) throws IOException, URISyntaxException {
        final URI uri = getUri(ImmutableList.of(PATH_SEGMENT_CONVERT));
        final HttpEntity httpEntity = getHttpEntity(convertFilesTaskRequest);

        return asyncRequestExecutor.execute(getHttpUriRequest(HttpPost.class, uri, httpEntity), TASK_RESPONSE_DATA_TYPE_REFERENCE);
    }

    @Override
    public AsyncResult<Pageable<OperationResponse>> convertFormats() throws IOException, URISyntaxException {
        return convertFormats(ImmutableMap.of());
    }

    @Override
    public AsyncResult<Pageable<OperationResponse>> convertFormats(
        @NotNull final Map<Filter, String> filters
    ) throws IOException, URISyntaxException {
        return convertFormats(filters, ImmutableList.of());
    }

    @Override
    public AsyncResult<Pageable<OperationResponse>> convertFormats(
        @NotNull final Map<Filter, String> filters, @NotNull final List<Include> includes
    ) throws IOException, URISyntaxException {
        return convertFormats(filters, includes, null);
    }

    @Override
    public AsyncResult<Pageable<OperationResponse>> convertFormats(
        @NotNull final Map<Filter, String> filters, @NotNull final List<Include> includes, @Nullable final Boolean alternative
    ) throws IOException, URISyntaxException {
        final List<NameValuePair> nameValuePairs = ImmutableList.<NameValuePair>builder().addAll(filtersToNameValuePairsConverter.convert(filters))
            .addAll(includesToNameValuePairsConverter.convert(includes)).addAll(alternativeToNameValuePairsConverter.convert(alternative)).build();

        final URI uri = getUri(ImmutableList.of(PATH_SEGMENT_CONVERT, PATH_SEGMENT_FORMATS), nameValuePairs);

        return asyncRequestExecutor.execute(getHttpUriRequest(HttpGet.class, uri), OPERATION_RESPONSE_PAGEABLE_TYPE_REFERENCE);
    }

    @Override
    public void close() throws IOException {
        asyncRequestExecutor.close();
    }
}
