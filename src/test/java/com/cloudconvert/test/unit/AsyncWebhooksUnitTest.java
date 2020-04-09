package com.cloudconvert.test.unit;

import com.cloudconvert.client.AsyncCloudConvertClient;
import com.cloudconvert.client.mapper.ObjectMapperProvider;
import com.cloudconvert.client.setttings.SettingsProvider;
import com.cloudconvert.dto.request.WebhookRequest;
import com.cloudconvert.dto.response.Pageable;
import com.cloudconvert.dto.response.UserResponse;
import com.cloudconvert.dto.response.WebhookResponse;
import com.cloudconvert.dto.result.AsyncResult;
import com.cloudconvert.dto.result.FutureAsyncResult;
import com.cloudconvert.executor.AsyncRequestExecutor;
import com.cloudconvert.resource.AbstractResource;
import com.cloudconvert.resource.params.Filter;
import com.cloudconvert.resource.params.Pagination;
import com.cloudconvert.test.framework.AbstractTest;
import com.cloudconvert.test.framework.UnitTest;
import com.google.common.collect.ImmutableMap;
import com.pivovarit.function.ThrowingSupplier;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Category(UnitTest.class)
@RunWith(MockitoJUnitRunner.class)
public class AsyncWebhooksUnitTest extends AbstractTest {

    private static final String WEBHOOK_ID = "webhook-id";

    @Mock
    private SettingsProvider settingsProvider;

    @Mock
    private AsyncRequestExecutor asyncRequestExecutor;

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private ObjectMapperProvider objectMapperProvider;

    @Captor
    private ArgumentCaptor<HttpUriRequest> httpUriRequestArgumentCaptor;

    private AsyncCloudConvertClient asyncCloudConvertClient;

    @Before
    public void before() {
        when(settingsProvider.getApiKey()).thenReturn(API_KEY);
        when(settingsProvider.getApiUrl()).thenReturn(API_URL);

        asyncCloudConvertClient = new AsyncCloudConvertClient(settingsProvider, objectMapperProvider, asyncRequestExecutor);
    }

    @Test
    public void users_me() throws Exception {
        final AsyncResult<UserResponse> userResponseAsyncResult = FutureAsyncResult.<UserResponse>builder().build();
        when(asyncRequestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.USER_RESPONSE_TYPE_REFERENCE))).thenReturn(userResponseAsyncResult);

        assertThat(asyncCloudConvertClient.users().me()).isEqualTo(userResponseAsyncResult);
        verify(asyncRequestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.USER_RESPONSE_TYPE_REFERENCE));

        final HttpUriRequest httpUriRequest = httpUriRequestArgumentCaptor.getValue();

        assertThat(httpUriRequest).isNotNull();
        assertThat(httpUriRequest.getMethod()).isEqualTo(HttpGet.METHOD_NAME);
        assertThat(httpUriRequest.getURI().toString()).isEqualTo(API_URL + "/" + AbstractResource.V2 + "/users/me");
        assertThat(httpUriRequest).isInstanceOf(HttpRequestBase.class);
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_AUTHORIZATION)).hasSize(1)
            .allSatisfy(header -> assertThat(VALUE_AUTHORIZATION).isEqualTo(header.getValue()));
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_USER_AGENT)).hasSize(1)
            .allSatisfy(header -> assertThat(AbstractResource.VALUE_USER_AGENT).isEqualTo(header.getValue()));
    }

    @Test
    public void webhooks_create() throws Exception {
        final WebhookRequest expectedWebhookRequest = new WebhookRequest().setUrl("webhooks-url");
        final AsyncResult<WebhookResponse> webhookResponseAsyncResult = FutureAsyncResult.<WebhookResponse>builder().build();
        when(asyncRequestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.WEBHOOKS_RESPONSE_TYPE_REFERENCE))).thenReturn(webhookResponseAsyncResult);

        assertThat(asyncCloudConvertClient.webhooks().create(expectedWebhookRequest)).isEqualTo(webhookResponseAsyncResult);
        verify(asyncRequestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.WEBHOOKS_RESPONSE_TYPE_REFERENCE));

        final HttpUriRequest httpUriRequest = httpUriRequestArgumentCaptor.getValue();

        assertThat(httpUriRequest).isNotNull();
        assertThat(httpUriRequest.getMethod()).isEqualTo(HttpPost.METHOD_NAME);
        assertThat(httpUriRequest.getURI().toString()).isEqualTo(API_URL + "/" + AbstractResource.V2 + "/webhooks");
        assertThat(httpUriRequest).isInstanceOfSatisfying(HttpEntityEnclosingRequestBase.class, httpEntityEnclosingRequestBase -> {
            final WebhookRequest actualWebhookRequest = ThrowingSupplier.unchecked(() -> objectMapperProvider.provide()
                .readValue(httpEntityEnclosingRequestBase.getEntity().getContent(), WebhookRequest.class)).get();

            assertThat(actualWebhookRequest.getUrl()).isEqualTo(expectedWebhookRequest.getUrl());
        });
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_AUTHORIZATION)).hasSize(1)
            .allSatisfy(header -> assertThat(VALUE_AUTHORIZATION).isEqualTo(header.getValue()));
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_USER_AGENT)).hasSize(1)
            .allSatisfy(header -> assertThat(AbstractResource.VALUE_USER_AGENT).isEqualTo(header.getValue()));
    }

    @Test
    public void webhooks_list_noQueryString() throws Exception {
        final AsyncResult<Pageable<WebhookResponse>> webhookResponsePageableAsyncResult = FutureAsyncResult.<Pageable<WebhookResponse>>builder().build();
        when(asyncRequestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.WEBHOOKS_RESPONSE_PAGEABLE_TYPE_REFERENCE))).thenReturn(webhookResponsePageableAsyncResult);

        assertThat(asyncCloudConvertClient.webhooks().list()).isEqualTo(webhookResponsePageableAsyncResult);
        verify(asyncRequestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.WEBHOOKS_RESPONSE_PAGEABLE_TYPE_REFERENCE));

        final HttpUriRequest httpUriRequest = httpUriRequestArgumentCaptor.getValue();

        assertThat(httpUriRequest).isNotNull();
        assertThat(httpUriRequest.getMethod()).isEqualTo(HttpGet.METHOD_NAME);
        assertThat(httpUriRequest.getURI().toString()).isEqualTo(API_URL + "/" + AbstractResource.V2 + "/users/me/webhooks");
        assertThat(httpUriRequest).isInstanceOf(HttpRequestBase.class);
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_AUTHORIZATION)).hasSize(1).allSatisfy(header ->
            assertThat(VALUE_AUTHORIZATION).isEqualTo(header.getValue()));
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_USER_AGENT)).hasSize(1).allSatisfy(header ->
            assertThat(AbstractResource.VALUE_USER_AGENT).isEqualTo(header.getValue()));
    }

    @Test
    public void webhooks_list_queryString() throws Exception {
        final AsyncResult<Pageable<WebhookResponse>> webhookResponsePageableAsyncResult = FutureAsyncResult.<Pageable<WebhookResponse>>builder().build();
        when(asyncRequestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.WEBHOOKS_RESPONSE_PAGEABLE_TYPE_REFERENCE))).thenReturn(webhookResponsePageableAsyncResult);

        final Map<Filter, String> filters = ImmutableMap.of(Filter.URL, "url");
        final Pagination pagination = new Pagination(300, 3);

        assertThat(asyncCloudConvertClient.webhooks().list(filters, pagination)).isEqualTo(webhookResponsePageableAsyncResult);
        verify(asyncRequestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.WEBHOOKS_RESPONSE_PAGEABLE_TYPE_REFERENCE));

        final HttpUriRequest httpUriRequest = httpUriRequestArgumentCaptor.getValue();

        assertThat(httpUriRequest).isNotNull();
        assertThat(httpUriRequest.getMethod()).isEqualTo(HttpGet.METHOD_NAME);
        assertThat(httpUriRequest.getURI().toString()).isEqualTo(API_URL + "/" + AbstractResource.V2 + "/users/me/webhooks?filter%5Burl%5D=url&per_page=300&page=3");
        assertThat(httpUriRequest).isInstanceOf(HttpRequestBase.class);
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_AUTHORIZATION)).hasSize(1).allSatisfy(header ->
            assertThat(VALUE_AUTHORIZATION).isEqualTo(header.getValue()));
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_USER_AGENT)).hasSize(1).allSatisfy(header ->
            assertThat(AbstractResource.VALUE_USER_AGENT).isEqualTo(header.getValue()));
    }

    @Test
    public void webhooks_delete() throws Exception {
        final AsyncResult<Void> voidAsyncResult = FutureAsyncResult.<Void>builder().build();
        when(asyncRequestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.VOID_TYPE_REFERENCE))).thenReturn(voidAsyncResult);

        assertThat(asyncCloudConvertClient.webhooks().delete(WEBHOOK_ID)).isEqualTo(voidAsyncResult);
        verify(asyncRequestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.VOID_TYPE_REFERENCE));

        final HttpUriRequest httpUriRequest = httpUriRequestArgumentCaptor.getValue();

        assertThat(httpUriRequest).isNotNull();
        assertThat(httpUriRequest.getMethod()).isEqualTo(HttpDelete.METHOD_NAME);
        assertThat(httpUriRequest.getURI().toString()).isEqualTo(API_URL + "/" + AbstractResource.V2 + "/webhooks/" + WEBHOOK_ID);
        assertThat(httpUriRequest).isInstanceOf(HttpRequestBase.class);
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_AUTHORIZATION)).hasSize(1).allSatisfy(header ->
            assertThat(VALUE_AUTHORIZATION).isEqualTo(header.getValue()));
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_USER_AGENT)).hasSize(1).allSatisfy(header ->
            assertThat(AbstractResource.VALUE_USER_AGENT).isEqualTo(header.getValue()));
    }

    @After
    public void after() throws Exception {
        asyncCloudConvertClient.close();
    }
}
