package com.cloudconvert.test.integration;

import com.cloudconvert.client.AsyncCloudConvertClient;
import com.cloudconvert.dto.response.UserResponse;
import com.cloudconvert.dto.result.Result;
import com.cloudconvert.test.framework.AbstractTest;
import com.cloudconvert.test.framework.IntegrationTest;
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

    private AsyncCloudConvertClient asyncCloudConvertClient;

    @Before
    public void before() throws Exception {
        asyncCloudConvertClient = new AsyncCloudConvertClient();
    }

    @Test(timeout = TIMEOUT)
    public void userLifecycle() throws Exception {
        final Result<UserResponse> userResponseResult = asyncCloudConvertClient.users().me().get();
        assertThat(userResponseResult.getStatus().getCode()).isEqualTo(HttpStatus.SC_OK);
        assertThat(userResponseResult.getBody()).isNotNull();
    }

    @After
    public void after() throws Exception {
        asyncCloudConvertClient.close();
    }
}
