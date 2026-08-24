package com.cloudconvert.test.integration;

import com.cloudconvert.client.CloudConvertClient;
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
public class WebhooksIntegrationTest extends AbstractTest {

    private CloudConvertClient cloudConvertClient;

    @Before
    public void before() throws Exception {
        cloudConvertClient = new CloudConvertClient();
    }

    @Test(timeout = TIMEOUT)
    public void userLifecycle() throws Exception {
        final Result<UserResponse> userResponseResult = cloudConvertClient.users().me();
        assertThat(userResponseResult.getStatus().getCode()).isEqualTo(HttpStatus.SC_OK);
        assertThat(userResponseResult.getBody()).isNotNull();
    }

    @After
    public void after() throws Exception {
        cloudConvertClient.close();
    }
}
