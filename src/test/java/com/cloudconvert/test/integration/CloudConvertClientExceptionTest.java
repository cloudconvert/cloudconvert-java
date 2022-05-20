package com.cloudconvert.test.integration;

import com.cloudconvert.client.CloudConvertClient;
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
public class CloudConvertClientExceptionTest extends AbstractTest {

    private static final String JPG_TEST_FILE_1 = "image-test-file-1.jpg";

    private CloudConvertClient cloudConvertUnauthorizedClient;
    private CloudConvertClient cloudConvertAuthorizedClient;

    private InputStream jpgTest1InputStream;

    @Before
    public void before() throws Exception {
        cloudConvertUnauthorizedClient = new CloudConvertClient(new PropertyFileSettingsProvider("invalid-application.properties"));
        cloudConvertAuthorizedClient = new CloudConvertClient(new PropertyFileSettingsProvider("application.properties"));

        jpgTest1InputStream = CloudConvertClientExceptionTest.class.getClassLoader().getResourceAsStream(JPG_TEST_FILE_1);
    }

    @Test(timeout = TIMEOUT)
    public void unauthorized() throws Exception {
        final CloudConvertClientException cloudConvertClientException = catchThrowableOfType(
            () -> cloudConvertUnauthorizedClient.importUsing().upload(new UploadImportRequest(), jpgTest1InputStream), CloudConvertClientException.class);

        assertThat(cloudConvertClientException.getStatus().getCode()).isEqualTo(HttpStatus.SC_UNAUTHORIZED);
        assertThat(cloudConvertClientException.getStatus().getReason()).isEqualTo("Unauthorized");
        assertThat(cloudConvertClientException.getHeaders()).containsKey("Content-Type");
        assertThat(cloudConvertClientException.getBody().getCode()).isEqualTo("UNAUTHENTICATED");
        assertThat(cloudConvertClientException.getBody().getMessage()).isEqualTo("Unauthenticated.");
    }

    @Test(timeout = TIMEOUT)
    public void unprocessableEntity() throws Exception {
        final CloudConvertClientException cloudConvertClientException = catchThrowableOfType(
            () -> cloudConvertAuthorizedClient.jobs().create(ImmutableMap.of()), CloudConvertClientException.class);

        assertThat(cloudConvertClientException.getStatus().getCode()).isEqualTo(HttpStatus.SC_UNPROCESSABLE_ENTITY);
        assertThat(cloudConvertClientException.getStatus().getReason()).contains("Unprocessable");
        assertThat(cloudConvertClientException.getHeaders()).containsKey("Content-Type");
        assertThat(cloudConvertClientException.getBody().getCode()).isEqualTo("INVALID_DATA");
        assertThat(cloudConvertClientException.getBody().getErrors()).containsKey("tasks");
    }

    @After
    public void after() throws Exception {
        jpgTest1InputStream.close();
        cloudConvertUnauthorizedClient.close();
        cloudConvertAuthorizedClient.close();
    }
}
