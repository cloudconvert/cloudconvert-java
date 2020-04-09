package com.cloudconvert.test.unit;

import com.cloudconvert.client.CloudConvertClient;
import com.cloudconvert.client.mapper.ObjectMapperProvider;
import com.cloudconvert.client.setttings.SettingsProvider;
import com.cloudconvert.dto.request.CaptureWebsitesTaskRequest;
import com.cloudconvert.dto.request.ConvertFilesTaskRequest;
import com.cloudconvert.dto.request.ExecuteCommandsTaskRequest;
import com.cloudconvert.dto.request.MergeFilesTaskRequest;
import com.cloudconvert.dto.request.TaskRequest;
import com.cloudconvert.dto.response.JobResponse;
import com.cloudconvert.dto.response.Pageable;
import com.cloudconvert.dto.result.Result;
import com.cloudconvert.executor.RequestExecutor;
import com.cloudconvert.resource.AbstractResource;
import com.cloudconvert.resource.params.Filter;
import com.cloudconvert.resource.params.Include;
import com.cloudconvert.resource.params.Pagination;
import com.cloudconvert.test.framework.AbstractTest;
import com.cloudconvert.test.framework.UnitTest;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteSource;
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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Category(UnitTest.class)
@RunWith(MockitoJUnitRunner.class)
public class JobsUnitTest extends AbstractTest {

    private static final String JOB_ID = "job-id";

    @Mock
    private SettingsProvider settingsProvider;

    @Mock
    private RequestExecutor requestExecutor;

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private ObjectMapperProvider objectMapperProvider;

    @Captor
    private ArgumentCaptor<HttpUriRequest> httpUriRequestArgumentCaptor;

    private CloudConvertClient cloudConvertClient;

    @Before
    public void before() {
        when(settingsProvider.getApiKey()).thenReturn(API_KEY);
        when(settingsProvider.getApiUrl()).thenReturn(API_URL);

        cloudConvertClient = new CloudConvertClient(settingsProvider, objectMapperProvider, requestExecutor);
    }

    @Test
    public void jobs_create() throws Exception {
        final Map<String, TaskRequest> tasks = ImmutableMap.of(
            "capture-website", new CaptureWebsitesTaskRequest().setUrl("capture-website-task-url"),
            "convert-files", new ConvertFilesTaskRequest().setInput("convert-files-task-input"),
            "execute-commands", new ExecuteCommandsTaskRequest().setInput("execute-commands-task-input"),
            "merge-files", new MergeFilesTaskRequest().setInput("merge-files-task-input")
        );
        final Result<JobResponse> jobResponseResult = Result.<JobResponse>builder().build();
        when(requestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.JOB_RESPONSE_TYPE_REFERENCE))).thenReturn(jobResponseResult);

        assertThat(cloudConvertClient.jobs().create(tasks)).isEqualTo(jobResponseResult);
        verify(requestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.JOB_RESPONSE_TYPE_REFERENCE));

        final HttpUriRequest httpUriRequest = httpUriRequestArgumentCaptor.getValue();

        assertThat(httpUriRequest).isNotNull();
        assertThat(httpUriRequest.getMethod()).isEqualTo(HttpPost.METHOD_NAME);
        assertThat(httpUriRequest.getURI().toString()).isEqualTo(API_URL + "/" + AbstractResource.V2 + "/jobs");
        assertThat(httpUriRequest).isInstanceOfSatisfying(HttpEntityEnclosingRequestBase.class, httpEntityEnclosingRequestBase -> {
            final ByteSource byteSource = new ByteSource() {
                @Override
                public InputStream openStream() throws IOException {
                    return httpEntityEnclosingRequestBase.getEntity().getContent();
                }
            };

            final String request = ThrowingSupplier.unchecked(() -> byteSource.asCharSource(Charsets.UTF_8).read()).get();
            assertThat(request).isEqualTo("{\"tasks\":{\"merge-files\":{\"input\":[\"merge-files-task-input\"],\"operation\":\"merge\"},\"capture-website\":" +
                "{\"url\":\"capture-website-task-url\",\"operation\":\"capture-website\"},\"execute-commands\":{\"input\":[\"execute-commands-task-input\"]," +
                "\"operation\":\"command\"},\"convert-files\":{\"input\":[\"convert-files-task-input\"],\"operation\":\"convert\"}},\"tag\":\"\"}");
        });
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_AUTHORIZATION)).hasSize(1).allSatisfy(header ->
            assertThat(VALUE_AUTHORIZATION).isEqualTo(header.getValue()));
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_USER_AGENT)).hasSize(1).allSatisfy(header ->
            assertThat(AbstractResource.VALUE_USER_AGENT).isEqualTo(header.getValue()));
    }

    @Test
    public void jobs_show() throws Exception {
        final Result<JobResponse> jobResponseResult = Result.<JobResponse>builder().build();
        when(requestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.JOB_RESPONSE_TYPE_REFERENCE))).thenReturn(jobResponseResult);

        assertThat(cloudConvertClient.jobs().show(JOB_ID)).isEqualTo(jobResponseResult);
        verify(requestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.JOB_RESPONSE_TYPE_REFERENCE));

        final HttpUriRequest httpUriRequest = httpUriRequestArgumentCaptor.getValue();

        assertThat(httpUriRequest).isNotNull();
        assertThat(httpUriRequest.getMethod()).isEqualTo(HttpGet.METHOD_NAME);
        assertThat(httpUriRequest.getURI().toString()).isEqualTo(API_URL + "/" + AbstractResource.V2 + "/jobs/" + JOB_ID);
        assertThat(httpUriRequest).isInstanceOf(HttpRequestBase.class);
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_AUTHORIZATION)).hasSize(1).allSatisfy(header ->
            assertThat(VALUE_AUTHORIZATION).isEqualTo(header.getValue()));
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_USER_AGENT)).hasSize(1).allSatisfy(header ->
            assertThat(AbstractResource.VALUE_USER_AGENT).isEqualTo(header.getValue()));
    }

    @Test
    public void jobs_wait() throws Exception {
        final Result<JobResponse> jobResponseResult = Result.<JobResponse>builder().build();
        when(requestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.JOB_RESPONSE_TYPE_REFERENCE))).thenReturn(jobResponseResult);

        assertThat(cloudConvertClient.jobs().wait(JOB_ID)).isEqualTo(jobResponseResult);
        verify(requestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.JOB_RESPONSE_TYPE_REFERENCE));

        final HttpUriRequest httpUriRequest = httpUriRequestArgumentCaptor.getValue();

        assertThat(httpUriRequest).isNotNull();
        assertThat(httpUriRequest.getMethod()).isEqualTo(HttpGet.METHOD_NAME);
        assertThat(httpUriRequest.getURI().toString()).isEqualTo(API_URL + "/" + AbstractResource.V2 + "/jobs/" + JOB_ID + "/wait");
        assertThat(httpUriRequest).isInstanceOf(HttpRequestBase.class);
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_AUTHORIZATION)).hasSize(1).allSatisfy(header ->
            assertThat(VALUE_AUTHORIZATION).isEqualTo(header.getValue()));
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_USER_AGENT)).hasSize(1).allSatisfy(header ->
            assertThat(AbstractResource.VALUE_USER_AGENT).isEqualTo(header.getValue()));
    }

    @Test
    public void jobs_list_noQueryString() throws Exception {
        final Result<Pageable<JobResponse>> jobResponsePageableResult = Result.<Pageable<JobResponse>>builder().build();
        when(requestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.JOB_RESPONSE_PAGEABLE_TYPE_REFERENCE))).thenReturn(jobResponsePageableResult);

        assertThat(cloudConvertClient.jobs().list()).isEqualTo(jobResponsePageableResult);
        verify(requestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.JOB_RESPONSE_PAGEABLE_TYPE_REFERENCE));

        final HttpUriRequest httpUriRequest = httpUriRequestArgumentCaptor.getValue();

        assertThat(httpUriRequest).isNotNull();
        assertThat(httpUriRequest.getMethod()).isEqualTo(HttpGet.METHOD_NAME);
        assertThat(httpUriRequest.getURI().toString()).isEqualTo(API_URL + "/" + AbstractResource.V2 + "/jobs");
        assertThat(httpUriRequest).isInstanceOf(HttpRequestBase.class);
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_AUTHORIZATION)).hasSize(1).allSatisfy(header ->
            assertThat(VALUE_AUTHORIZATION).isEqualTo(header.getValue()));
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_USER_AGENT)).hasSize(1).allSatisfy(header ->
            assertThat(AbstractResource.VALUE_USER_AGENT).isEqualTo(header.getValue()));
    }

    @Test
    public void jobs_list_queryString() throws Exception {
        final Result<Pageable<JobResponse>> jobResponsePageableResult = Result.<Pageable<JobResponse>>builder().build();
        when(requestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.JOB_RESPONSE_PAGEABLE_TYPE_REFERENCE))).thenReturn(jobResponsePageableResult);

        final Map<Filter, String> filters = ImmutableMap.of(Filter.STATUS, "status", Filter.TAG, "tag");
        final List<Include> includes = ImmutableList.of(Include.RETRIES, Include.DEPENDS_ON_TASKS);
        final Pagination pagination = new Pagination(300, 3);

        assertThat(cloudConvertClient.jobs().list(filters, includes, pagination)).isEqualTo(jobResponsePageableResult);
        verify(requestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.JOB_RESPONSE_PAGEABLE_TYPE_REFERENCE));

        final HttpUriRequest httpUriRequest = httpUriRequestArgumentCaptor.getValue();

        assertThat(httpUriRequest).isNotNull();
        assertThat(httpUriRequest.getMethod()).isEqualTo(HttpGet.METHOD_NAME);
        assertThat(httpUriRequest.getURI().toString()).isEqualTo(API_URL + "/" + AbstractResource.V2 + "/jobs"
            + "?filter%5Bstatus%5D=status&filter%5Btag%5D=tag&include=retries%2Cdepends_on_tasks&per_page=300&page=3");
        assertThat(httpUriRequest).isInstanceOf(HttpRequestBase.class);
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_AUTHORIZATION)).hasSize(1).allSatisfy(header ->
            assertThat(VALUE_AUTHORIZATION).isEqualTo(header.getValue()));
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_USER_AGENT)).hasSize(1).allSatisfy(header ->
            assertThat(AbstractResource.VALUE_USER_AGENT).isEqualTo(header.getValue()));
    }

    @Test
    public void jobs_delete() throws Exception {
        final Result<Void> voidResult = Result.<Void>builder().build();
        when(requestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.VOID_TYPE_REFERENCE))).thenReturn(voidResult);

        assertThat(cloudConvertClient.jobs().delete(JOB_ID)).isEqualTo(voidResult);
        verify(requestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.VOID_TYPE_REFERENCE));

        final HttpUriRequest httpUriRequest = httpUriRequestArgumentCaptor.getValue();

        assertThat(httpUriRequest).isNotNull();
        assertThat(httpUriRequest.getMethod()).isEqualTo(HttpDelete.METHOD_NAME);
        assertThat(httpUriRequest.getURI().toString()).isEqualTo(API_URL + "/" + AbstractResource.V2 + "/jobs/" + JOB_ID);
        assertThat(httpUriRequest).isInstanceOf(HttpRequestBase.class);
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_AUTHORIZATION)).hasSize(1).allSatisfy(header ->
            assertThat(VALUE_AUTHORIZATION).isEqualTo(header.getValue()));
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_USER_AGENT)).hasSize(1).allSatisfy(header ->
            assertThat(AbstractResource.VALUE_USER_AGENT).isEqualTo(header.getValue()));
    }

    @After
    public void after() throws Exception {
        cloudConvertClient.close();
    }
}
