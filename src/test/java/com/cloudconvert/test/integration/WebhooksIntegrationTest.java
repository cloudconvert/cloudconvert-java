package com.cloudconvert.test.integration;

import com.cloudconvert.client.CloudConvertClient;
import com.cloudconvert.dto.Event;
import com.cloudconvert.dto.request.WebhookRequest;
import com.cloudconvert.dto.response.Pageable;
import com.cloudconvert.dto.response.UserResponseData;
import com.cloudconvert.dto.response.WebhookResponse;
import com.cloudconvert.dto.response.WebhookResponseData;
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
public class WebhooksIntegrationTest extends AbstractTest {

    private static final String WEBHOOK_PAYLOAD = "cloudconvert";
    private static final String WEBHOOK_SIGNATURE = "5c4c0691bce8a1a2af738b7073fe0627e792734813358c5f88a658819dd0a6d2";

    private CloudConvertClient cloudConvertClient;

    @Before
    public void before() throws Exception {
        cloudConvertClient = new CloudConvertClient();
    }

    @Test
    public void userLifecycle() throws Exception {
        final Result<UserResponseData> userResponseDataResult = cloudConvertClient.users().me();
        assertThat(userResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_OK);
        assertThat(userResponseDataResult.getBody()).isNotNull();
    }

    @Test
    public void webhooksLifecycle() throws Exception {
        // Create
        final WebhookRequest webhookRequest = new WebhookRequest().setUrl("http://some-url.com")
            .setEvents(ImmutableList.of(Event.JOB_CREATED, Event.JOB_FAILED, Event.JOB_FINISHED));
        final Result<WebhookResponseData> createWebhookResponseDataResult = cloudConvertClient.webhooks().create(webhookRequest);
        assertThat(createWebhookResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);
        assertThat(createWebhookResponseDataResult.getBody()).isNotNull();

        final WebhookResponse createWebhookResponse = createWebhookResponseDataResult.getBody().getData();
        assertThat(createWebhookResponse.getSigningSecret()).isNotNull();

        // List
        final Result<Pageable<WebhookResponse>> webhookResponsePageable = cloudConvertClient.webhooks().list();
        assertThat(webhookResponsePageable.getStatus()).isEqualTo(HttpStatus.SC_OK);

        // Delete
        final Result<Void> deleteWebhookResponseDataResult = cloudConvertClient.webhooks().delete(createWebhookResponse.getId());
        assertThat(deleteWebhookResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_NO_CONTENT);

        // Verify
        assertThat(cloudConvertClient.webhooks().verify(WEBHOOK_PAYLOAD, WEBHOOK_SIGNATURE)).isTrue();
    }

    @After
    public void after() throws Exception {
        cloudConvertClient.close();
    }
}
