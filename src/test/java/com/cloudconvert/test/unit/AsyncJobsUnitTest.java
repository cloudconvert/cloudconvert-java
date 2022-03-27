package com.cloudconvert.test.unit;

import com.cloudconvert.client.AsyncCloudConvertClient;
import com.cloudconvert.client.mapper.ObjectMapperProvider;
import com.cloudconvert.client.setttings.SettingsProvider;
import com.cloudconvert.dto.request.CaptureWebsitesTaskRequest;
import com.cloudconvert.dto.request.ConvertFilesTaskRequest;
import com.cloudconvert.dto.request.ExecuteCommandsTaskRequest;
import com.cloudconvert.dto.request.MergeFilesTaskRequest;
import com.cloudconvert.dto.request.TaskRequest;
import com.cloudconvert.dto.response.JobResponse;
import com.cloudconvert.dto.response.Pageable;
import com.cloudconvert.dto.result.AsyncResult;
import com.cloudconvert.dto.result.FutureAsyncResult;
import com.cloudconvert.executor.AsyncRequestExecutor;
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
public class AsyncJobsUnitTest extends AbstractTest {

    private static final String JOB_ID = "job-id";

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
        when(settingsProvider.getSyncApiUrl()).thenReturn(API_SYNC_URL);

        asyncCloudConvertClient = new AsyncCloudConvertClient(settingsProvider, objectMapperProvider, asyncRequestExecutor);
    }

    @Test
    public void jobs_create() throws Exception {
        final Map<String, TaskRequest> tasks = ImmutableMap.of(
            "capture-website", new CaptureWebsitesTaskRequest().setUrl("capture-website-task-url").setOutputFormat("pdf").set("zoom", 1.3),
            "convert-files", new ConvertFilesTaskRequest().setInput("convert-files-task-input").set("width", 100).set("height", 100),
            "execute-commands", new ExecuteCommandsTaskRequest().setInput("execute-commands-task-input"),
            "merge-files", new MergeFilesTaskRequest().setInput("merge-files-task-input")
        );
        final AsyncResult<JobResponse> jobResponseAsyncResult = FutureAsyncResult.<JobResponse>builder().build();
        when(asyncRequestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.JOB_RESPONSE_TYPE_REFERENCE))).thenReturn(jobResponseAsyncResult);

        assertThat(asyncCloudConvertClient.jobs().create(tasks)).isEqualTo(jobResponseAsyncResult);
        verify(asyncRequestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.JOB_RESPONSE_TYPE_REFERENCE));

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
            assertThat(request).isEqualTo("{\"tasks\":{\"merge-files\":{\"input\":[\"merge-files-task-input\"],\"operation\":\"merge\"}," +
                "\"capture-website\":{\"url\":\"capture-website-task-url\",\"output_format\":\"pdf\",\"operation\":\"capture-website\"," +
                "\"zoom\":1.3},\"execute-commands\":{\"input\":[\"execute-commands-task-input\"],\"operation\":\"command\"},\"convert-files\":" +
                "{\"input\":[\"convert-files-task-input\"],\"operation\":\"convert\",\"width\":100,\"height\":100}},\"tag\":\"\"}");
        });
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_AUTHORIZATION)).hasSize(1).allSatisfy(header ->
            assertThat(VALUE_AUTHORIZATION).isEqualTo(header.getValue()));
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_USER_AGENT)).hasSize(1).allSatisfy(header ->
            assertThat(AbstractResource.VALUE_USER_AGENT).isEqualTo(header.getValue()));
    }

    @Test
    public void jobs_show() throws Exception {
        final AsyncResult<JobResponse> jobResponseAsyncResult = FutureAsyncResult.<JobResponse>builder().build();
        when(asyncRequestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.JOB_RESPONSE_TYPE_REFERENCE))).thenReturn(jobResponseAsyncResult);

        assertThat(asyncCloudConvertClient.jobs().show(JOB_ID)).isEqualTo(jobResponseAsyncResult);
        verify(asyncRequestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.JOB_RESPONSE_TYPE_REFERENCE));

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
        final AsyncResult<JobResponse> jobResponseAsyncResult = FutureAsyncResult.<JobResponse>builder().build();
        when(asyncRequestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.JOB_RESPONSE_TYPE_REFERENCE))).thenReturn(jobResponseAsyncResult);

        assertThat(asyncCloudConvertClient.jobs().wait(JOB_ID)).isEqualTo(jobResponseAsyncResult);
        verify(asyncRequestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.JOB_RESPONSE_TYPE_REFERENCE));

        final HttpUriRequest httpUriRequest = httpUriRequestArgumentCaptor.getValue();

        assertThat(httpUriRequest).isNotNull();
        assertThat(httpUriRequest.getMethod()).isEqualTo(HttpGet.METHOD_NAME);
        assertThat(httpUriRequest.getURI().toString()).isEqualTo(API_SYNC_URL + "/" + AbstractResource.V2 + "/jobs/" + JOB_ID);
        assertThat(httpUriRequest).isInstanceOf(HttpRequestBase.class);
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_AUTHORIZATION)).hasSize(1).allSatisfy(header ->
            assertThat(VALUE_AUTHORIZATION).isEqualTo(header.getValue()));
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_USER_AGENT)).hasSize(1).allSatisfy(header ->
            assertThat(AbstractResource.VALUE_USER_AGENT).isEqualTo(header.getValue()));
    }

    @Test
    public void jobs_list_noQueryString() throws Exception {
        final AsyncResult<Pageable<JobResponse>> jobResponsePageableAsyncResult = FutureAsyncResult.<Pageable<JobResponse>>builder().build();
        when(asyncRequestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.JOB_RESPONSE_PAGEABLE_TYPE_REFERENCE))).thenReturn(jobResponsePageableAsyncResult);

        assertThat(asyncCloudConvertClient.jobs().list()).isEqualTo(jobResponsePageableAsyncResult);
        verify(asyncRequestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.JOB_RESPONSE_PAGEABLE_TYPE_REFERENCE));

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
        final AsyncResult<Pageable<JobResponse>> jobResponsePageableAsyncResult = FutureAsyncResult.<Pageable<JobResponse>>builder().build();
        when(asyncRequestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.JOB_RESPONSE_PAGEABLE_TYPE_REFERENCE))).thenReturn(jobResponsePageableAsyncResult);

        final Map<Filter, String> filters = ImmutableMap.of(Filter.STATUS, "status", Filter.TAG, "tag");
        final List<Include> includes = ImmutableList.of(Include.RETRIES, Include.DEPENDS_ON_TASKS);
        final Pagination pagination = new Pagination(300, 3);

        assertThat(asyncCloudConvertClient.jobs().list(filters, includes, pagination)).isEqualTo(jobResponsePageableAsyncResult);
        verify(asyncRequestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.JOB_RESPONSE_PAGEABLE_TYPE_REFERENCE));

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
        final AsyncResult<Void> voidAsyncResult = FutureAsyncResult.<Void>builder().build();
        when(asyncRequestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.VOID_TYPE_REFERENCE))).thenReturn(voidAsyncResult);

        assertThat(asyncCloudConvertClient.jobs().delete(JOB_ID)).isEqualTo(voidAsyncResult);
        verify(asyncRequestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.VOID_TYPE_REFERENCE));

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
        asyncCloudConvertClient.close();
    }
}
