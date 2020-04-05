package com.cloudconvert.resource;

import com.cloudconvert.client.api.key.ApiKeyProvider;
import com.cloudconvert.client.api.url.ApiUrlProvider;
import com.cloudconvert.client.mapper.ObjectMapperProvider;
import com.cloudconvert.dto.request.WebhookRequest;
import com.cloudconvert.dto.response.Pageable;
import com.cloudconvert.dto.response.WebhookResponse;
import com.cloudconvert.dto.response.WebhookResponseData;
import com.cloudconvert.dto.result.AbstractResult;
import com.cloudconvert.resource.params.Filter;
import com.cloudconvert.resource.params.Pagination;
import org.apache.commons.codec.binary.Hex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

public abstract class AbstractWebhooksResource<WRDAR extends AbstractResult<WebhookResponseData>,
    WRPAR extends AbstractResult<Pageable<WebhookResponse>>, VAR extends AbstractResult<Void>> extends AbstractResource {

    public static final String HMAC_SHA256 = "HmacSHA256";

    public static final String PATH_SEGMENT_WEBHOOKS = "webhooks";

    public AbstractWebhooksResource(
        final ApiUrlProvider apiUrlProvider, final ApiKeyProvider apiKeyProvider, final ObjectMapperProvider objectMapperProvider
    ) {
        super(apiUrlProvider, apiKeyProvider, objectMapperProvider);
    }

    /**
     * Create a webhook. Requires the webhook.write scope.
     *
     * @param webhookRequest {@link WebhookRequest}
     * @return {@link WRDAR}
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract WRDAR create(
        @NotNull final WebhookRequest webhookRequest
    ) throws IOException, URISyntaxException;

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

    /**
     * Verify webhook signature
     *
     * @param payload   payload
     * @param signature signature
     * @param secret    secret
     * @return
     */
    public boolean verify(
        @NotNull final String payload, @NotNull final String signature, @NotNull final String secret
    ) throws InvalidKeyException, NoSuchAlgorithmException {
        final Mac mac = Mac.getInstance(HMAC_SHA256);
        final SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(), HMAC_SHA256);
        mac.init(secretKeySpec);

        return signature.equals(Hex.encodeHexString(mac.doFinal(payload.getBytes())));
    }
}
