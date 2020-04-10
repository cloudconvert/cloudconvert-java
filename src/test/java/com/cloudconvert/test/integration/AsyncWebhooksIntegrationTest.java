package com.cloudconvert.test.integration;

import com.cloudconvert.client.AsyncCloudConvertClient;
import com.cloudconvert.dto.Event;
import com.cloudconvert.dto.request.WebhookRequest;
import com.cloudconvert.dto.response.Pageable;
import com.cloudconvert.dto.response.UserResponse;
import com.cloudconvert.dto.response.WebhookResponse;
import com.cloudconvert.dto.result.Result;
import com.cloudconvert.test.framework.AbstractTest;
import com.cloudconvert.test.framework.IntegrationTest;
import com.google.common.collect.ImmutableList;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.assertj.core.api.Assertions.assertThat;

@Category(IntegrationTest.class)
@RunWith(JUnit4.class)
public class AsyncWebhooksIntegrationTest extends AbstractTest {

    private static final String WEBHOOK_PAYLOAD = "cloudconvert";
    private static final String WEBHOOK_SIGNATURE = "5c4c0691bce8a1a2af738b7073fe0627e792734813358c5f88a658819dd0a6d2";

    private AsyncCloudConvertClient asyncCloudConvertClient;

    @Before
    public void before() throws Exception {
        asyncCloudConvertClient = new AsyncCloudConvertClient();
    }

    @Test(timeout = TIMEOUT)
    public void userLifecycle() throws Exception {
        final Result<UserResponse> userResponseResult = asyncCloudConvertClient.users().me().get();
        assertThat(userResponseResult.getStatus()).isEqualTo(HttpStatus.SC_OK);
        assertThat(userResponseResult.getBody()).isNotNull();
    }

    @Test(timeout = TIMEOUT)
    public void webhooksLifecycle() throws Exception {
        // Create
        final WebhookRequest webhookRequest = new WebhookRequest().setUrl("http://some-url.com")
            .setEvents(ImmutableList.of(Event.JOB_CREATED, Event.JOB_FAILED, Event.JOB_FINISHED));
        final Result<WebhookResponse> createWebhookResponseResult = asyncCloudConvertClient.webhooks().create(webhookRequest).get();
        assertThat(createWebhookResponseResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);
        assertThat(createWebhookResponseResult.getBody()).isNotNull();

        final WebhookResponse createWebhookResponse = createWebhookResponseResult.getBody();
        assertThat(createWebhookResponse.getSigningSecret()).isNotNull();

        // List
        final Result<Pageable<WebhookResponse>> webhookResponsePageable = asyncCloudConvertClient.webhooks().list().get();
        assertThat(webhookResponsePageable.getStatus()).isEqualTo(HttpStatus.SC_OK);

        // Delete
        final Result<Void> deleteWebhookResponseResult = asyncCloudConvertClient.webhooks().delete(createWebhookResponse.getId()).get();
        assertThat(deleteWebhookResponseResult.getStatus()).isEqualTo(HttpStatus.SC_NO_CONTENT);

        // Verify
        assertThat(asyncCloudConvertClient.webhooks().verify(WEBHOOK_PAYLOAD, WEBHOOK_SIGNATURE)).isTrue();
    }

    @After
    public void after() throws Exception {
        asyncCloudConvertClient.close();
    }
}
