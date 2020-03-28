package com.cloudconvert.resource.sync;

import com.cloudconvert.client.api.key.ApiKeyProvider;
import com.cloudconvert.client.api.url.ApiUrlProvider;
import com.cloudconvert.client.mapper.ObjectMapperProvider;
import com.cloudconvert.dto.request.WebhookRequest;
import com.cloudconvert.dto.response.Pageable;
import com.cloudconvert.dto.response.WebhookResponse;
import com.cloudconvert.dto.response.WebhookResponseData;
import com.cloudconvert.dto.result.Result;
import com.cloudconvert.executor.RequestExecutor;
import com.cloudconvert.resource.AbstractUsersResource;
import com.cloudconvert.resource.AbstractWebhooksResource;
import com.cloudconvert.resource.params.Filter;
import com.cloudconvert.resource.params.Pagination;
import com.cloudconvert.resource.params.converter.FiltersToNameValuePairsConverter;
import com.cloudconvert.resource.params.converter.PaginationToNameValuePairsConverter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

public class WebhookResource extends AbstractWebhooksResource<
    Result<WebhookResponseData>, Result<Pageable<WebhookResponse>>, Result<Void>> {

    private final RequestExecutor requestExecutor;

    private final FiltersToNameValuePairsConverter filtersToNameValuePairsConverter;
    private final PaginationToNameValuePairsConverter paginationToNameValuePairsConverter;

    public WebhookResource(
        final ApiUrlProvider apiUrlProvider, final ApiKeyProvider apiKeyProvider,
        final ObjectMapperProvider objectMapperProvider, final RequestExecutor requestExecutor
    ) {
        super(apiUrlProvider, apiKeyProvider, objectMapperProvider);

        this.requestExecutor = requestExecutor;

        this.filtersToNameValuePairsConverter = new FiltersToNameValuePairsConverter();
        this.paginationToNameValuePairsConverter = new PaginationToNameValuePairsConverter();
    }

    @Override
    public Result<WebhookResponseData> create(
        @NotNull final WebhookRequest webhookRequest
    ) throws IOException, URISyntaxException {
        final URI uri = getUri(ImmutableList.of(PATH_SEGMENT_WEBHOOKS));
        final HttpEntity httpEntity = getHttpEntity(webhookRequest);
        final HttpUriRequest httpUriRequest = getHttpUriRequest(HttpPost.class, uri, httpEntity);

        return requestExecutor.execute(httpUriRequest, WEBHOOKS_RESPONSE_DATA_TYPE_REFERENCE);
    }

    @Override
    public Result<Pageable<WebhookResponse>> list() throws IOException, URISyntaxException {
        return list(ImmutableMap.of());
    }

    @Override
    public Result<Pageable<WebhookResponse>> list(
        @NotNull final Map<Filter, String> filters
    ) throws IOException, URISyntaxException {
        return list(filters, null);
    }

    @Override
    public Result<Pageable<WebhookResponse>> list(
        @NotNull final Map<Filter, String> filters, @Nullable final Pagination pagination
    ) throws IOException, URISyntaxException {
        final List<NameValuePair> nameValuePairs = ImmutableList.<NameValuePair>builder()
            .addAll(filtersToNameValuePairsConverter.convert(filters))
            .addAll(paginationToNameValuePairsConverter.convert(pagination)).build();

        final URI uri = getUri(ImmutableList.of(AbstractUsersResource.PATH_SEGMENT_USERS,
            AbstractUsersResource.PATH_SEGMENT_ME, PATH_SEGMENT_WEBHOOKS), nameValuePairs);

        return requestExecutor.execute(getHttpUriRequest(HttpGet.class, uri), WEBHOOKS_RESPONSE_PAGEABLE_TYPE_REFERENCE);
    }

    @Override
    public Result<Void> delete(
        @NotNull final String webhookId
    ) throws IOException, URISyntaxException {
        final URI uri = getUri(ImmutableList.of(PATH_SEGMENT_WEBHOOKS, webhookId));
        final HttpUriRequest httpUriRequest = getHttpUriRequest(HttpDelete.class, uri);

        return requestExecutor.execute(httpUriRequest, VOID_TYPE_REFERENCE);
    }

    @Override
    public void close() throws IOException {
        requestExecutor.close();
    }
}
