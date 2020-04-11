package com.cloudconvert.resource.sync;

import com.cloudconvert.client.mapper.ObjectMapperProvider;
import com.cloudconvert.client.setttings.SettingsProvider;
import com.cloudconvert.dto.request.WebhookRequest;
import com.cloudconvert.dto.response.Pageable;
import com.cloudconvert.dto.response.WebhookResponse;
import com.cloudconvert.dto.result.Result;
import com.cloudconvert.executor.RequestExecutor;
import com.cloudconvert.resource.AbstractWebhooksResource;
import com.cloudconvert.resource.params.Filter;
import com.cloudconvert.resource.params.Pagination;
import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

public class WebhookResource extends AbstractWebhooksResource<
    Result<WebhookResponse>, Result<Pageable<WebhookResponse>>, Result<Void>> {

    private final RequestExecutor requestExecutor;

    public WebhookResource(
        final SettingsProvider settingsProvider,
        final ObjectMapperProvider objectMapperProvider, final RequestExecutor requestExecutor
    ) {
        super(settingsProvider, objectMapperProvider);

        this.requestExecutor = requestExecutor;
    }

    @Override
    public Result<WebhookResponse> create(
        @NotNull final WebhookRequest webhookRequest
    ) throws IOException, URISyntaxException {
        return requestExecutor.execute(getCreateHttpUriRequest(webhookRequest), WEBHOOKS_RESPONSE_TYPE_REFERENCE);
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
        return requestExecutor.execute(getListHttpUriRequest(filters, pagination), WEBHOOKS_RESPONSE_PAGEABLE_TYPE_REFERENCE);
    }

    @Override
    public Result<Void> delete(
        @NotNull final String webhookId
    ) throws IOException, URISyntaxException {
        return requestExecutor.execute(getDeleteHttpUriRequest(webhookId), VOID_TYPE_REFERENCE);
    }

    @Override
    public void close() throws IOException {
        requestExecutor.close();
    }
}
