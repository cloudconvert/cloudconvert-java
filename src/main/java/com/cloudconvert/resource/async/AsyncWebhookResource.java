package com.cloudconvert.resource.async;

import com.cloudconvert.client.mapper.ObjectMapperProvider;
import com.cloudconvert.client.setttings.SettingsProvider;
import com.cloudconvert.dto.request.WebhookRequest;
import com.cloudconvert.dto.response.Pageable;
import com.cloudconvert.dto.response.WebhookResponse;
import com.cloudconvert.dto.result.AsyncResult;
import com.cloudconvert.executor.AsyncRequestExecutor;
import com.cloudconvert.resource.AbstractWebhooksResource;
import com.cloudconvert.resource.params.Filter;
import com.cloudconvert.resource.params.Pagination;
import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

public class AsyncWebhookResource extends AbstractWebhooksResource<
    AsyncResult<WebhookResponse>, AsyncResult<Pageable<WebhookResponse>>, AsyncResult<Void>> {

    private final AsyncRequestExecutor asyncRequestExecutor;

    public AsyncWebhookResource(
        final SettingsProvider settingsProvider,
        final ObjectMapperProvider objectMapperProvider, final AsyncRequestExecutor asyncRequestExecutor
    ) {
        super(settingsProvider, objectMapperProvider);

        this.asyncRequestExecutor = asyncRequestExecutor;
    }

    @Override
    public AsyncResult<WebhookResponse> create(
        @NotNull final WebhookRequest webhookRequest
    ) throws IOException, URISyntaxException {
        return asyncRequestExecutor.execute(getCreateHttpUriRequest(webhookRequest), WEBHOOKS_RESPONSE_TYPE_REFERENCE);
    }

    @Override
    public AsyncResult<Pageable<WebhookResponse>> list() throws IOException, URISyntaxException {
        return list(ImmutableMap.of());
    }

    @Override
    public AsyncResult<Pageable<WebhookResponse>> list(
        @NotNull final Map<Filter, String> filters
    ) throws IOException, URISyntaxException {
        return list(filters, null);
    }

    @Override
    public AsyncResult<Pageable<WebhookResponse>> list(
        @NotNull final Map<Filter, String> filters, @Nullable final Pagination pagination
    ) throws IOException, URISyntaxException {
        return asyncRequestExecutor.execute(getListHttpUriRequest(filters, pagination), WEBHOOKS_RESPONSE_PAGEABLE_TYPE_REFERENCE);
    }

    @Override
    public AsyncResult<Void> delete(
        @NotNull final String webhookId
    ) throws IOException, URISyntaxException {
        return asyncRequestExecutor.execute(getDeleteHttpUriRequest(webhookId), VOID_TYPE_REFERENCE);
    }

    @Override
    public void close() throws IOException {
        asyncRequestExecutor.close();
    }
}
