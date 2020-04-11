package com.cloudconvert.resource;

import com.cloudconvert.client.mapper.ObjectMapperProvider;
import com.cloudconvert.client.setttings.SettingsProvider;
import com.cloudconvert.dto.request.WebhookRequest;
import com.cloudconvert.dto.response.Pageable;
import com.cloudconvert.dto.response.WebhookResponse;
import com.cloudconvert.dto.result.AbstractResult;
import com.cloudconvert.resource.params.Filter;
import com.cloudconvert.resource.params.Pagination;
import com.cloudconvert.resource.params.converter.FiltersToNameValuePairsConverter;
import com.cloudconvert.resource.params.converter.PaginationToNameValuePairsConverter;
import com.google.common.collect.ImmutableList;
import org.apache.commons.codec.binary.Hex;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

public abstract class AbstractWebhooksResource<WRAR extends AbstractResult<WebhookResponse>,
    WRPAR extends AbstractResult<Pageable<WebhookResponse>>, VAR extends AbstractResult<Void>> extends AbstractResource {

    public static final String HMAC_SHA256 = "HmacSHA256";

    public static final String PATH_SEGMENT_WEBHOOKS = "webhooks";

    private final FiltersToNameValuePairsConverter filtersToNameValuePairsConverter;
    private final PaginationToNameValuePairsConverter paginationToNameValuePairsConverter;

    public AbstractWebhooksResource(
        final SettingsProvider settingsProvider, final ObjectMapperProvider objectMapperProvider
    ) {
        super(settingsProvider, objectMapperProvider);

        this.filtersToNameValuePairsConverter = new FiltersToNameValuePairsConverter();
        this.paginationToNameValuePairsConverter = new PaginationToNameValuePairsConverter();
    }

    /**
     * Create a webhook. Requires the webhook.write scope.
     *
     * @param webhookRequest {@link WebhookRequest}
     * @return {@link WRAR}
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract WRAR create(
        @NotNull final WebhookRequest webhookRequest
    ) throws IOException, URISyntaxException;

    protected HttpUriRequest getCreateHttpUriRequest(
        @NotNull final WebhookRequest webhookRequest
    ) throws IOException, URISyntaxException {
        final URI uri = getUri(ImmutableList.of(PATH_SEGMENT_WEBHOOKS));
        final HttpEntity httpEntity = getHttpEntity(webhookRequest);
        return getHttpUriRequest(HttpPost.class, uri, httpEntity);
    }

    /**
     * List all webhooks. Requires the webhook.read scope.
     *
     * @return {@link WRPAR}
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract WRPAR list() throws IOException, URISyntaxException;

    /**
     * List all webhooks. Requires the webhook.read scope.
     *
     * @param filters (optional) Filters:
     *                - url - The result will be filtered to include only webhooks with a specific URL.
     * @return {@link WRPAR}
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract WRPAR list(
        @NotNull final Map<Filter, String> filters
    ) throws IOException, URISyntaxException;

    /**
     * List all webhooks. Requires the webhook.read scope.
     *
     * @param filters    (optional) Filters:
     *                   - url - The result will be filtered to include only webhooks with a specific URL.
     * @param pagination (optional) Pagination:
     *                   - per_page - Number of tasks per page, defaults to 100.
     *                   - page - The result page to show.
     * @return {@link WRPAR}
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract WRPAR list(
        @NotNull final Map<Filter, String> filters, @Nullable final Pagination pagination
    ) throws IOException, URISyntaxException;

    protected HttpUriRequest getListHttpUriRequest(
        @NotNull final Map<Filter, String> filters, @Nullable final Pagination pagination
    ) throws URISyntaxException {
        final List<NameValuePair> nameValuePairs = ImmutableList.<NameValuePair>builder()
            .addAll(filtersToNameValuePairsConverter.convert(filters))
            .addAll(paginationToNameValuePairsConverter.convert(pagination)).build();

        final URI uri = getUri(ImmutableList.of(AbstractUsersResource.PATH_SEGMENT_USERS,
            AbstractUsersResource.PATH_SEGMENT_ME, PATH_SEGMENT_WEBHOOKS), nameValuePairs);

        return getHttpUriRequest(HttpGet.class, uri);
    }

    /**
     * Delete a webhook. Requires the webhook.write scope.
     *
     * @param webhookId webhook id
     * @return VR
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract VAR delete(
        @NotNull final String webhookId
    ) throws IOException, URISyntaxException;

    protected HttpUriRequest getDeleteHttpUriRequest(
        @NotNull final String webhookId
    ) throws URISyntaxException {
        final URI uri = getUri(ImmutableList.of(PATH_SEGMENT_WEBHOOKS, webhookId));
        return getHttpUriRequest(HttpDelete.class, uri);
    }

    /**
     * Verify webhook signature
     *
     * @param payload   payload
     * @param signature signature
     * @return
     */
    public boolean verify(
        @NotNull final String payload, @NotNull final String signature
    ) throws InvalidKeyException, NoSuchAlgorithmException {
        final Mac mac = Mac.getInstance(HMAC_SHA256);
        final SecretKeySpec secretKeySpec = new SecretKeySpec(getSettingsProvider().getWebhookSigningSecret().getBytes(), HMAC_SHA256);
        mac.init(secretKeySpec);

        return signature.equals(Hex.encodeHexString(mac.doFinal(payload.getBytes())));
    }
}
