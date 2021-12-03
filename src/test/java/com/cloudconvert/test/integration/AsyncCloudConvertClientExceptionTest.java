package com.cloudconvert.test.integration;

import com.cloudconvert.client.AsyncCloudConvertClient;
import com.cloudconvert.client.setttings.PropertyFileSettingsProvider;
import com.cloudconvert.dto.request.UploadImportRequest;
import com.cloudconvert.exception.CloudConvertClientException;
import com.cloudconvert.test.framework.AbstractTest;
import com.cloudconvert.test.framework.IntegrationTest;
import com.google.common.collect.ImmutableMap;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

@Category(IntegrationTest.class)
@RunWith(JUnit4.class)
public class AsyncCloudConvertClientExceptionTest extends AbstractTest {

    private static final String JPG_TEST_FILE_1 = "image-test-file-1.jpg";

    private AsyncCloudConvertClient asyncCloudConvertUnauthorizedClient;
    private AsyncCloudConvertClient asyncCloudConvertAuthorizedClient;

    private InputStream jpgTest1InputStream;

    @Before
    public void before() throws Exception {
        asyncCloudConvertUnauthorizedClient = new AsyncCloudConvertClient(new PropertyFileSettingsProvider("invalid-application.properties"));
        asyncCloudConvertAuthorizedClient = new AsyncCloudConvertClient(new PropertyFileSettingsProvider("application.properties"));

        jpgTest1InputStream = AsyncCloudConvertClientExceptionTest.class.getClassLoader().getResourceAsStream(JPG_TEST_FILE_1);
    }

    @Test(timeout = TIMEOUT)
    public void unauthorized() throws Exception {
        final CloudConvertClientException cloudConvertClientException = catchThrowableOfType(
            () -> asyncCloudConvertUnauthorizedClient.importUsing().upload(new UploadImportRequest(), jpgTest1InputStream).get(), CloudConvertClientException.class);

        assertThat(cloudConvertClientException.getStatus().getCode()).isEqualTo(HttpStatus.SC_UNAUTHORIZED);
        assertThat(cloudConvertClientException.getStatus().getReason()).isEqualTo("Unauthorized");
        assertThat(cloudConvertClientException.getHeaders()).containsKey("Content-Type");
        assertThat(cloudConvertClientException.getBody().getCode()).isEqualTo("UNAUTHENTICATED");
        assertThat(cloudConvertClientException.getBody().getMessage()).isEqualTo("Unauthenticated.");
    }

    @Test(timeout = TIMEOUT)
    public void unprocessableEntity() throws Exception {
        final CloudConvertClientException cloudConvertClientException = catchThrowableOfType(
            () -> asyncCloudConvertAuthorizedClient.jobs().create(ImmutableMap.of()).get(), CloudConvertClientException.class);

        assertThat(cloudConvertClientException.getStatus().getCode()).isEqualTo(HttpStatus.SC_UNPROCESSABLE_ENTITY);
        assertThat(cloudConvertClientException.getStatus().getReason()).isEqualTo("Unprocessable Entity");
        assertThat(cloudConvertClientException.getHeaders()).containsKey("Content-Type");
        assertThat(cloudConvertClientException.getBody().getCode()).isEqualTo("INVALID_DATA");
        assertThat(cloudConvertClientException.getBody().getMessage()).isEqualTo("The given data was invalid.");
        assertThat(cloudConvertClientException.getBody().getErrors()).containsKey("tasks");
    }

    @After
    public void after() throws Exception {
        jpgTest1InputStream.close();
        asyncCloudConvertUnauthorizedClient.close();
        asyncCloudConvertAuthorizedClient.close();
    }
}
