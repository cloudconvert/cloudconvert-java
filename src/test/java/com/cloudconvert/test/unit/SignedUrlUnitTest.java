package com.cloudconvert.test.unit;

import com.cloudconvert.client.CloudConvertClient;
import com.cloudconvert.client.mapper.ObjectMapperProvider;
import com.cloudconvert.client.setttings.SettingsProvider;
import com.cloudconvert.dto.request.ConvertFilesTaskRequest;
import com.cloudconvert.dto.request.TaskRequest;
import com.cloudconvert.dto.request.UrlExportRequest;
import com.cloudconvert.dto.request.UrlImportRequest;
import com.cloudconvert.executor.RequestExecutor;
import com.cloudconvert.test.framework.AbstractTest;
import com.cloudconvert.test.framework.UnitTest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableMap;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

@Category(UnitTest.class)
@RunWith(MockitoJUnitRunner.class)
public class SignedUrlUnitTest extends AbstractTest {


    @Mock
    private SettingsProvider settingsProvider;

    @Mock
    private RequestExecutor requestExecutor;

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private ObjectMapperProvider objectMapperProvider;

    private CloudConvertClient cloudConvertClient;

    @Before
    public void before() {


        cloudConvertClient = new CloudConvertClient(settingsProvider, objectMapperProvider, requestExecutor);
    }

    @Test
    public void signSignedUrl() throws NoSuchAlgorithmException, InvalidKeyException, JsonProcessingException {

        final Map<String, TaskRequest> tasks = ImmutableMap.of(
                "import-my-file", new UrlImportRequest().setUrl("import-url"),
                "convert-my-file", new ConvertFilesTaskRequest()
                        .setInput("import-my-file")
                        .set("width", 100)
                        .set("height", 100),
                "export-my-file", new UrlExportRequest().setInput("convert-my-file")
        );

        final String base = "https://s.cloudconvert.com/b3d85428-584e-4639-bc11-76b7dee9c109";
        final String signingSecret = "NT8dpJkttEyfSk3qlRgUJtvTkx64vhyX";
        final String cacheKey = "mykey";

        final String url = cloudConvertClient.signedUrls().sign(base, signingSecret, tasks, cacheKey);

        assertThat(url).startsWith(base);
        assertThat(url).contains("?job=");
        assertThat(url).contains("&cache_key=mykey");
        assertThat(url).contains("&s=b04d8cf7d65ec56c839443c69dd2bb75e8792e006441019957c0d9824319612a");

    }


    @After
    public void after() throws Exception {
        cloudConvertClient.close();
    }
}
