package com.cloudconvert.client;

import com.cloudconvert.client.api.key.ApiKeyProvider;
import com.cloudconvert.client.api.url.ApiUrlProvider;
import com.cloudconvert.client.mapper.ObjectMapperProvider;
import com.cloudconvert.dto.request.AzureBlobExportRequest;
import com.cloudconvert.dto.request.AzureBlobImportRequest;
import com.cloudconvert.dto.request.CaptureWebsitesTaskRequest;
import com.cloudconvert.dto.request.ConvertFilesTaskRequest;
import com.cloudconvert.dto.request.CreateArchivesTaskRequest;
import com.cloudconvert.dto.request.ExecuteCommandsTaskRequest;
import com.cloudconvert.dto.request.GoogleCloudStorageExportRequest;
import com.cloudconvert.dto.request.GoogleCloudStorageImportRequest;
import com.cloudconvert.dto.request.MergeFilesTaskRequest;
import com.cloudconvert.dto.request.OpenStackExportRequest;
import com.cloudconvert.dto.request.OpenStackImportRequest;
import com.cloudconvert.dto.request.OptimizeFilesTaskRequest;
import com.cloudconvert.dto.request.S3ExportRequest;
import com.cloudconvert.dto.request.S3ImportRequest;
import com.cloudconvert.dto.request.SftpExportRequest;
import com.cloudconvert.dto.request.SftpImportRequest;
import com.cloudconvert.dto.request.TaskRequest;
import com.cloudconvert.dto.request.UploadImportRequest;
import com.cloudconvert.dto.request.UrlExportRequest;
import com.cloudconvert.dto.request.UrlImportRequest;
import com.cloudconvert.dto.request.WebhookRequest;
import com.cloudconvert.dto.response.JobResponse;
import com.cloudconvert.dto.response.JobResponseData;
import com.cloudconvert.dto.response.OperationResponse;
import com.cloudconvert.dto.response.Pageable;
import com.cloudconvert.dto.response.TaskResponse;
import com.cloudconvert.dto.response.TaskResponseData;
import com.cloudconvert.dto.response.UserResponseData;
import com.cloudconvert.dto.response.WebhookResponse;
import com.cloudconvert.dto.response.WebhookResponseData;
import com.cloudconvert.dto.result.AsyncResult;
import com.cloudconvert.dto.result.CompletedAsyncResult;
import com.cloudconvert.dto.result.FutureAsyncResult;
import com.cloudconvert.dto.result.Result;
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
import org.apache.http.HttpStatus;
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
public class AsyncCloudConvertClientTest extends AbstractTest {

    private static final String API_URL = "https://api.sandbox.cloudconvert.com";
    private static final String API_KEY = "api-key";

    private static final String VALUE_AUTHORIZATION = AbstractResource.BEARER + " " + API_KEY;

    private static final String JOB_ID = "job-id";
    private static final String TASK_ID = "task-id";
    private static final String WEBHOOK_ID = "webhook-id";

    @Mock
    private ApiUrlProvider apiUrlProvider;

    @Mock
    private ApiKeyProvider apiKeyProvider;

    @Mock
    private AsyncRequestExecutor asyncRequestExecutor;

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private ObjectMapperProvider objectMapperProvider;

    @Mock
    private InputStream inputStream;

    @Captor
    private ArgumentCaptor<HttpUriRequest> httpUriRequestArgumentCaptor;

    private AsyncCloudConvertClient asyncCloudConvertClient;

    @Before
    public void before() throws Exception {
        when(apiUrlProvider.provide()).thenReturn(API_URL);
        when(apiKeyProvider.provide()).thenReturn(API_KEY);

        asyncCloudConvertClient = new AsyncCloudConvertClient(apiUrlProvider, apiKeyProvider, objectMapperProvider, asyncRequestExecutor);
    }

    @Test
    public void tasks_show_noIncludes() throws Exception {
        final AsyncResult<TaskResponseData> taskResponseDataAsyncResult = FutureAsyncResult.<TaskResponseData>builder().build();
        when(asyncRequestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.TASK_RESPONSE_DATA_TYPE_REFERENCE))).thenReturn(taskResponseDataAsyncResult);

        assertThat(asyncCloudConvertClient.tasks().show(TASK_ID)).isEqualTo(taskResponseDataAsyncResult);
        verify(asyncRequestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.TASK_RESPONSE_DATA_TYPE_REFERENCE));

        final HttpUriRequest httpUriRequest = httpUriRequestArgumentCaptor.getValue();

        assertThat(httpUriRequest).isNotNull();
        assertThat(httpUriRequest.getMethod()).isEqualTo(HttpGet.METHOD_NAME);
        assertThat(httpUriRequest.getURI().toString()).isEqualTo(API_URL + "/" + AbstractResource.V2 + "/tasks/" + TASK_ID);
        assertThat(httpUriRequest).isInstanceOf(HttpRequestBase.class);
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_AUTHORIZATION)).hasSize(1).allSatisfy(header ->
            assertThat(VALUE_AUTHORIZATION).isEqualTo(header.getValue()));
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_USER_AGENT)).hasSize(1).allSatisfy(header ->
            assertThat(AbstractResource.VALUE_USER_AGENT).isEqualTo(header.getValue()));
    }

    @Test
    public void tasks_show_includes() throws Exception {
        final AsyncResult<TaskResponseData> taskResponseDataAsyncResult = FutureAsyncResult.<TaskResponseData>builder().build();
        when(asyncRequestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.TASK_RESPONSE_DATA_TYPE_REFERENCE))).thenReturn(taskResponseDataAsyncResult);

        final List<Include> includes = ImmutableList.of(Include.RETRIES, Include.DEPENDS_ON_TASKS, Include.PAYLOAD);

        assertThat(asyncCloudConvertClient.tasks().show(TASK_ID, includes)).isEqualTo(taskResponseDataAsyncResult);
        verify(asyncRequestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.TASK_RESPONSE_DATA_TYPE_REFERENCE));

        final HttpUriRequest httpUriRequest = httpUriRequestArgumentCaptor.getValue();

        assertThat(httpUriRequest).isNotNull();
        assertThat(httpUriRequest.getMethod()).isEqualTo(HttpGet.METHOD_NAME);
        assertThat(httpUriRequest.getURI().toString()).isEqualTo(API_URL + "/" + AbstractResource.V2 + "/tasks/" + TASK_ID
            + "?include=retries%2Cdepends_on_tasks%2Cpayload");
        assertThat(httpUriRequest).isInstanceOf(HttpRequestBase.class);
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_AUTHORIZATION)).hasSize(1).allSatisfy(header ->
            assertThat(VALUE_AUTHORIZATION).isEqualTo(header.getValue()));
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_USER_AGENT)).hasSize(1).allSatisfy(header ->
            assertThat(AbstractResource.VALUE_USER_AGENT).isEqualTo(header.getValue()));
    }

    @Test
    public void tasks_wait() throws Exception {
        final AsyncResult<TaskResponseData> taskResponseDataAsyncResult = FutureAsyncResult.<TaskResponseData>builder().build();
        when(asyncRequestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.TASK_RESPONSE_DATA_TYPE_REFERENCE))).thenReturn(taskResponseDataAsyncResult);

        assertThat(asyncCloudConvertClient.tasks().wait(TASK_ID)).isEqualTo(taskResponseDataAsyncResult);
        verify(asyncRequestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.TASK_RESPONSE_DATA_TYPE_REFERENCE));

        final HttpUriRequest httpUriRequest = httpUriRequestArgumentCaptor.getValue();

        assertThat(httpUriRequest).isNotNull();
        assertThat(httpUriRequest.getMethod()).isEqualTo(HttpGet.METHOD_NAME);
        assertThat(httpUriRequest.getURI().toString()).isEqualTo(API_URL + "/" + AbstractResource.V2 + "/tasks/" + TASK_ID + "/wait");
        assertThat(httpUriRequest).isInstanceOf(HttpRequestBase.class);
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_AUTHORIZATION)).hasSize(1).allSatisfy(header ->
            assertThat(VALUE_AUTHORIZATION).isEqualTo(header.getValue()));
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_USER_AGENT)).hasSize(1).allSatisfy(header ->
            assertThat(AbstractResource.VALUE_USER_AGENT).isEqualTo(header.getValue()));
    }

    @Test
    public void tasks_list_noQueryString() throws Exception {
        final AsyncResult<Pageable<TaskResponse>> taskResponsePageableAsyncResult = FutureAsyncResult.<Pageable<TaskResponse>>builder().build();
        when(asyncRequestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.TASK_RESPONSE_PAGEABLE_TYPE_REFERENCE))).thenReturn(taskResponsePageableAsyncResult);

        assertThat(asyncCloudConvertClient.tasks().list()).isEqualTo(taskResponsePageableAsyncResult);
        verify(asyncRequestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.TASK_RESPONSE_PAGEABLE_TYPE_REFERENCE));

        final HttpUriRequest httpUriRequest = httpUriRequestArgumentCaptor.getValue();

        assertThat(httpUriRequest).isNotNull();
        assertThat(httpUriRequest.getMethod()).isEqualTo(HttpGet.METHOD_NAME);
        assertThat(httpUriRequest.getURI().toString()).isEqualTo(API_URL + "/" + AbstractResource.V2 + "/tasks");
        assertThat(httpUriRequest).isInstanceOf(HttpRequestBase.class);
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_AUTHORIZATION)).hasSize(1).allSatisfy(header ->
            assertThat(VALUE_AUTHORIZATION).isEqualTo(header.getValue()));
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_USER_AGENT)).hasSize(1).allSatisfy(header ->
            assertThat(AbstractResource.VALUE_USER_AGENT).isEqualTo(header.getValue()));
    }

    @Test
    public void tasks_list_queryString() throws Exception {
        final AsyncResult<Pageable<TaskResponse>> taskResponsePageableAsyncResult = FutureAsyncResult.<Pageable<TaskResponse>>builder().build();
        when(asyncRequestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.TASK_RESPONSE_PAGEABLE_TYPE_REFERENCE))).thenReturn(taskResponsePageableAsyncResult);

        final Map<Filter, String> filters = ImmutableMap.of(Filter.JOB_ID, "job-id", Filter.STATUS, "status");
        final List<Include> includes = ImmutableList.of(Include.RETRIES, Include.DEPENDS_ON_TASKS);
        final Pagination pagination = new Pagination(300, 3);

        assertThat(asyncCloudConvertClient.tasks().list(filters, includes, pagination)).isEqualTo(taskResponsePageableAsyncResult);
        verify(asyncRequestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.TASK_RESPONSE_PAGEABLE_TYPE_REFERENCE));

        final HttpUriRequest httpUriRequest = httpUriRequestArgumentCaptor.getValue();

        assertThat(httpUriRequest).isNotNull();
        assertThat(httpUriRequest.getMethod()).isEqualTo(HttpGet.METHOD_NAME);
        assertThat(httpUriRequest.getURI().toString()).isEqualTo(API_URL + "/" + AbstractResource.V2 + "/tasks"
            + "?filter%5Bjob_id%5D=job-id&filter%5Bstatus%5D=status&include=retries%2Cdepends_on_tasks&per_page=300&page=3");
        assertThat(httpUriRequest).isInstanceOf(HttpRequestBase.class);
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_AUTHORIZATION)).hasSize(1).allSatisfy(header ->
            assertThat(VALUE_AUTHORIZATION).isEqualTo(header.getValue()));
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_USER_AGENT)).hasSize(1).allSatisfy(header ->
            assertThat(AbstractResource.VALUE_USER_AGENT).isEqualTo(header.getValue()));
    }

    @Test
    public void tasks_cancel() throws Exception {
        final AsyncResult<TaskResponseData> taskResponseDataAsyncResult = FutureAsyncResult.<TaskResponseData>builder().build();
        when(asyncRequestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.TASK_RESPONSE_DATA_TYPE_REFERENCE))).thenReturn(taskResponseDataAsyncResult);

        assertThat(asyncCloudConvertClient.tasks().cancel(TASK_ID)).isEqualTo(taskResponseDataAsyncResult);
        verify(asyncRequestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.TASK_RESPONSE_DATA_TYPE_REFERENCE));

        final HttpUriRequest httpUriRequest = httpUriRequestArgumentCaptor.getValue();

        assertThat(httpUriRequest).isNotNull();
        assertThat(httpUriRequest.getMethod()).isEqualTo(HttpPost.METHOD_NAME);
        assertThat(httpUriRequest.getURI().toString()).isEqualTo(API_URL + "/" + AbstractResource.V2 + "/tasks/" + TASK_ID + "/cancel");
        assertThat(httpUriRequest).isInstanceOf(HttpRequestBase.class);
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_AUTHORIZATION)).hasSize(1).allSatisfy(header ->
            assertThat(VALUE_AUTHORIZATION).isEqualTo(header.getValue()));
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_USER_AGENT)).hasSize(1).allSatisfy(header ->
            assertThat(AbstractResource.VALUE_USER_AGENT).isEqualTo(header.getValue()));
    }

    @Test
    public void tasks_retry() throws Exception {
        final AsyncResult<TaskResponseData> taskResponseDataAsyncResult = FutureAsyncResult.<TaskResponseData>builder().build();
        when(asyncRequestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.TASK_RESPONSE_DATA_TYPE_REFERENCE))).thenReturn(taskResponseDataAsyncResult);

        assertThat(asyncCloudConvertClient.tasks().retry(TASK_ID)).isEqualTo(taskResponseDataAsyncResult);
        verify(asyncRequestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.TASK_RESPONSE_DATA_TYPE_REFERENCE));

        final HttpUriRequest httpUriRequest = httpUriRequestArgumentCaptor.getValue();

        assertThat(httpUriRequest).isNotNull();
        assertThat(httpUriRequest.getMethod()).isEqualTo(HttpPost.METHOD_NAME);
        assertThat(httpUriRequest.getURI().toString()).isEqualTo(API_URL + "/" + AbstractResource.V2 + "/tasks/" + TASK_ID + "/retry");
        assertThat(httpUriRequest).isInstanceOf(HttpRequestBase.class);
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_AUTHORIZATION)).hasSize(1).allSatisfy(header ->
            assertThat(VALUE_AUTHORIZATION).isEqualTo(header.getValue()));
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_USER_AGENT)).hasSize(1).allSatisfy(header ->
            assertThat(AbstractResource.VALUE_USER_AGENT).isEqualTo(header.getValue()));
    }

    @Test
    public void tasks_delete() throws Exception {
        final AsyncResult<Void> voidAsyncResult = FutureAsyncResult.<Void>builder().build();
        when(asyncRequestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.VOID_TYPE_REFERENCE))).thenReturn(voidAsyncResult);

        assertThat(asyncCloudConvertClient.tasks().delete(TASK_ID)).isEqualTo(voidAsyncResult);
        verify(asyncRequestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.VOID_TYPE_REFERENCE));

        final HttpUriRequest httpUriRequest = httpUriRequestArgumentCaptor.getValue();

        assertThat(httpUriRequest).isNotNull();
        assertThat(httpUriRequest.getMethod()).isEqualTo(HttpDelete.METHOD_NAME);
        assertThat(httpUriRequest.getURI().toString()).isEqualTo(API_URL + "/" + AbstractResource.V2 + "/tasks/" + TASK_ID);
        assertThat(httpUriRequest).isInstanceOf(HttpRequestBase.class);
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_AUTHORIZATION)).hasSize(1).allSatisfy(header ->
            assertThat(VALUE_AUTHORIZATION).isEqualTo(header.getValue()));
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_USER_AGENT)).hasSize(1).allSatisfy(header ->
            assertThat(AbstractResource.VALUE_USER_AGENT).isEqualTo(header.getValue()));
    }

    @Test
    public void tasks_operations_noQueryString() throws Exception {
        final AsyncResult<Pageable<OperationResponse>> pageableOperationResponseAsyncResult = FutureAsyncResult.<Pageable<OperationResponse>>builder().build();
        when(asyncRequestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.OPERATION_RESPONSE_PAGEABLE_TYPE_REFERENCE)))
            .thenReturn(pageableOperationResponseAsyncResult);

        assertThat(asyncCloudConvertClient.tasks().operations()).isEqualTo(pageableOperationResponseAsyncResult);
        verify(asyncRequestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.OPERATION_RESPONSE_PAGEABLE_TYPE_REFERENCE));

        final HttpUriRequest httpUriRequest = httpUriRequestArgumentCaptor.getValue();

        assertThat(httpUriRequest).isNotNull();
        assertThat(httpUriRequest.getMethod()).isEqualTo(HttpGet.METHOD_NAME);
        assertThat(httpUriRequest.getURI().toString()).isEqualTo(API_URL + "/" + AbstractResource.V2 + "/operations");
        assertThat(httpUriRequest).isInstanceOf(HttpRequestBase.class);
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_AUTHORIZATION)).hasSize(1).allSatisfy(header ->
            assertThat(VALUE_AUTHORIZATION).isEqualTo(header.getValue()));
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_USER_AGENT)).hasSize(1).allSatisfy(header ->
            assertThat(AbstractResource.VALUE_USER_AGENT).isEqualTo(header.getValue()));
    }

    @Test
    public void tasks_operations_queryString() throws Exception {
        final AsyncResult<Pageable<OperationResponse>> operationResponsePageableAsyncResult = FutureAsyncResult.<Pageable<OperationResponse>>builder().build();
        when(asyncRequestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.OPERATION_RESPONSE_PAGEABLE_TYPE_REFERENCE)))
            .thenReturn(operationResponsePageableAsyncResult);

        final Map<Filter, String> filters = ImmutableMap.of(Filter.OPERATION, "operation", Filter.INPUT_FORMAT, "input-format",
            Filter.OUTPUT_FORMAT, "output-format", Filter.ENGINE, "engine", Filter.ENGINE_VERSION, "engine-version");
        final List<Include> includes = ImmutableList.of(Include.RETRIES, Include.DEPENDS_ON_TASKS);
        final Boolean alternative = Boolean.TRUE;

        assertThat(asyncCloudConvertClient.tasks().operations(filters, includes, alternative)).isEqualTo(operationResponsePageableAsyncResult);
        verify(asyncRequestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.OPERATION_RESPONSE_PAGEABLE_TYPE_REFERENCE));

        final HttpUriRequest httpUriRequest = httpUriRequestArgumentCaptor.getValue();

        assertThat(httpUriRequest).isNotNull();
        assertThat(httpUriRequest.getMethod()).isEqualTo(HttpGet.METHOD_NAME);
        assertThat(httpUriRequest.getURI().toString()).isEqualTo(API_URL + "/" + AbstractResource.V2 + "/operations"
            + "?filter%5Boperation%5D=operation&filter%5Binput_format%5D=input-format&filter%5Boutput_format%5D=output-format"
            + "&filter%5Bengine%5D=engine&filter%5Bengine_version%5D=engine-version&include=retries%2Cdepends_on_tasks&alternatives=true");

        assertThat(httpUriRequest).isInstanceOf(HttpRequestBase.class);
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_AUTHORIZATION)).hasSize(1).allSatisfy(header ->
            assertThat(VALUE_AUTHORIZATION).isEqualTo(header.getValue()));
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_USER_AGENT)).hasSize(1).allSatisfy(header ->
            assertThat(AbstractResource.VALUE_USER_AGENT).isEqualTo(header.getValue()));
    }

    @Test
    public void jobs_create() throws Exception {
        final Map<String, TaskRequest> tasks = ImmutableMap.of(
            "capture-website", new CaptureWebsitesTaskRequest().setUrl("capture-website-task-url"),
            "convert-files", new ConvertFilesTaskRequest().setInput("convert-files-task-input"),
            "execute-commands", new ExecuteCommandsTaskRequest().setInput("execute-commands-task-input"),
            "merge-files", new MergeFilesTaskRequest().setInput("merge-files-task-input")
        );
        final AsyncResult<JobResponseData> jobResponseDataAsyncResult = FutureAsyncResult.<JobResponseData>builder().build();
        when(asyncRequestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.JOB_RESPONSE_DATA_TYPE_REFERENCE))).thenReturn(jobResponseDataAsyncResult);

        assertThat(asyncCloudConvertClient.jobs().create(tasks)).isEqualTo(jobResponseDataAsyncResult);
        verify(asyncRequestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.JOB_RESPONSE_DATA_TYPE_REFERENCE));

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
        final AsyncResult<JobResponseData> jobResponseDataAsyncResult = FutureAsyncResult.<JobResponseData>builder().build();
        when(asyncRequestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.JOB_RESPONSE_DATA_TYPE_REFERENCE))).thenReturn(jobResponseDataAsyncResult);

        assertThat(asyncCloudConvertClient.jobs().show(JOB_ID)).isEqualTo(jobResponseDataAsyncResult);
        verify(asyncRequestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.JOB_RESPONSE_DATA_TYPE_REFERENCE));

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
        final AsyncResult<JobResponseData> jobResponseDataAsyncResult = FutureAsyncResult.<JobResponseData>builder().build();
        when(asyncRequestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.JOB_RESPONSE_DATA_TYPE_REFERENCE))).thenReturn(jobResponseDataAsyncResult);

        assertThat(asyncCloudConvertClient.jobs().wait(JOB_ID)).isEqualTo(jobResponseDataAsyncResult);
        verify(asyncRequestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.JOB_RESPONSE_DATA_TYPE_REFERENCE));

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

    @Test
    public void tasks_convert() throws Exception {
        final ConvertFilesTaskRequest expectedConvertFilesTaskRequest = new ConvertFilesTaskRequest().setInput("convert-files-task-input");
        final AsyncResult<TaskResponseData> taskResponseDataAsyncResult = FutureAsyncResult.<TaskResponseData>builder().build();
        when(asyncRequestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.TASK_RESPONSE_DATA_TYPE_REFERENCE))).thenReturn(taskResponseDataAsyncResult);

        assertThat(asyncCloudConvertClient.tasks().convert(expectedConvertFilesTaskRequest)).isEqualTo(taskResponseDataAsyncResult);
        verify(asyncRequestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.TASK_RESPONSE_DATA_TYPE_REFERENCE));

        final HttpUriRequest httpUriRequest = httpUriRequestArgumentCaptor.getValue();

        assertThat(httpUriRequest).isNotNull();
        assertThat(httpUriRequest.getMethod()).isEqualTo(HttpPost.METHOD_NAME);
        assertThat(httpUriRequest.getURI().toString()).isEqualTo(API_URL + "/" + AbstractResource.V2 + "/convert");
        assertThat(httpUriRequest).isInstanceOfSatisfying(HttpEntityEnclosingRequestBase.class, httpEntityEnclosingRequestBase -> {
            final ConvertFilesTaskRequest actualConvertFilesTaskRequest = ThrowingSupplier.unchecked(() -> objectMapperProvider.provide()
                .readValue(httpEntityEnclosingRequestBase.getEntity().getContent(), ConvertFilesTaskRequest.class)).get();
            assertThat(actualConvertFilesTaskRequest.getInput()).isEqualTo(expectedConvertFilesTaskRequest.getInput());
        });
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_AUTHORIZATION)).hasSize(1).allSatisfy(header ->
            assertThat(VALUE_AUTHORIZATION).isEqualTo(header.getValue()));
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_USER_AGENT)).hasSize(1).allSatisfy(header ->
            assertThat(AbstractResource.VALUE_USER_AGENT).isEqualTo(header.getValue()));
    }

    @Test
    public void tasks_convertFormats_noQueryString() throws Exception {
        final AsyncResult<Pageable<OperationResponse>> operationResponsePageableAsyncResult = FutureAsyncResult.<Pageable<OperationResponse>>builder().build();
        when(asyncRequestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.OPERATION_RESPONSE_PAGEABLE_TYPE_REFERENCE)))
            .thenReturn(operationResponsePageableAsyncResult);

        assertThat(asyncCloudConvertClient.tasks().convertFormats()).isEqualTo(operationResponsePageableAsyncResult);
        verify(asyncRequestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.OPERATION_RESPONSE_PAGEABLE_TYPE_REFERENCE));

        final HttpUriRequest httpUriRequest = httpUriRequestArgumentCaptor.getValue();

        assertThat(httpUriRequest).isNotNull();
        assertThat(httpUriRequest.getMethod()).isEqualTo(HttpGet.METHOD_NAME);
        assertThat(httpUriRequest.getURI().toString()).isEqualTo(API_URL + "/" + AbstractResource.V2 + "/convert/formats");
        assertThat(httpUriRequest).isInstanceOf(HttpRequestBase.class);
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_AUTHORIZATION)).hasSize(1).allSatisfy(header ->
            assertThat(VALUE_AUTHORIZATION).isEqualTo(header.getValue()));
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_USER_AGENT)).hasSize(1).allSatisfy(header ->
            assertThat(AbstractResource.VALUE_USER_AGENT).isEqualTo(header.getValue()));
    }

    @Test
    public void tasks_convertFormats_queryString() throws Exception {
        final AsyncResult<Pageable<OperationResponse>> operationResponsePageableAsyncResult = FutureAsyncResult.<Pageable<OperationResponse>>builder().build();
        when(asyncRequestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.OPERATION_RESPONSE_PAGEABLE_TYPE_REFERENCE)))
            .thenReturn(operationResponsePageableAsyncResult);

        final Map<Filter, String> filters = ImmutableMap.of(Filter.INPUT_FORMAT, "input-format",
            Filter.OUTPUT_FORMAT, "output-format", Filter.ENGINE, "engine", Filter.ENGINE_VERSION, "engine-version");
        final List<Include> includes = ImmutableList.of(Include.OPTIONS, Include.ENGINE_VERSIONS);
        final Boolean alternative = Boolean.TRUE;

        assertThat(asyncCloudConvertClient.tasks().convertFormats(filters, includes, alternative)).isEqualTo(operationResponsePageableAsyncResult);
        verify(asyncRequestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.OPERATION_RESPONSE_PAGEABLE_TYPE_REFERENCE));

        final HttpUriRequest httpUriRequest = httpUriRequestArgumentCaptor.getValue();

        assertThat(httpUriRequest).isNotNull();
        assertThat(httpUriRequest.getMethod()).isEqualTo(HttpGet.METHOD_NAME);
        assertThat(httpUriRequest.getURI().toString()).isEqualTo(API_URL + "/" + AbstractResource.V2 + "/convert/formats"
            + "?filter%5Binput_format%5D=input-format&filter%5Boutput_format%5D=output-format&filter%5Bengine%5D=engine"
            + "&filter%5Bengine_version%5D=engine-version&include=options%2Cengine_versions&alternatives=true");
        assertThat(httpUriRequest).isInstanceOf(HttpRequestBase.class);
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_AUTHORIZATION)).hasSize(1).allSatisfy(header ->
            assertThat(VALUE_AUTHORIZATION).isEqualTo(header.getValue()));
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_USER_AGENT)).hasSize(1).allSatisfy(header ->
            assertThat(AbstractResource.VALUE_USER_AGENT).isEqualTo(header.getValue()));
    }

    @Test
    public void tasks_optimize() throws Exception {
        final OptimizeFilesTaskRequest expectedOptimizeFilesTaskRequest = new OptimizeFilesTaskRequest().setInput("optimize-files-task-input");
        final AsyncResult<TaskResponseData> taskResponseDataAsyncResult = FutureAsyncResult.<TaskResponseData>builder().build();
        when(asyncRequestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.TASK_RESPONSE_DATA_TYPE_REFERENCE))).thenReturn(taskResponseDataAsyncResult);

        assertThat(asyncCloudConvertClient.tasks().optimize(expectedOptimizeFilesTaskRequest)).isEqualTo(taskResponseDataAsyncResult);
        verify(asyncRequestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.TASK_RESPONSE_DATA_TYPE_REFERENCE));

        final HttpUriRequest httpUriRequest = httpUriRequestArgumentCaptor.getValue();

        assertThat(httpUriRequest).isNotNull();
        assertThat(httpUriRequest.getMethod()).isEqualTo(HttpPost.METHOD_NAME);
        assertThat(httpUriRequest.getURI().toString()).isEqualTo(API_URL + "/" + AbstractResource.V2 + "/optimize");
        assertThat(httpUriRequest).isInstanceOfSatisfying(HttpEntityEnclosingRequestBase.class, httpEntityEnclosingRequestBase -> {
            final OptimizeFilesTaskRequest actualOptimizeFilesTaskRequest = ThrowingSupplier.unchecked(() -> objectMapperProvider.provide()
                .readValue(httpEntityEnclosingRequestBase.getEntity().getContent(), OptimizeFilesTaskRequest.class)).get();
            assertThat(actualOptimizeFilesTaskRequest.getInput()).isEqualTo(expectedOptimizeFilesTaskRequest.getInput());
        });
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_AUTHORIZATION)).hasSize(1).allSatisfy(header ->
            assertThat(VALUE_AUTHORIZATION).isEqualTo(header.getValue()));
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_USER_AGENT)).hasSize(1).allSatisfy(header ->
            assertThat(AbstractResource.VALUE_USER_AGENT).isEqualTo(header.getValue()));
    }

    @Test
    public void tasks_capture() throws Exception {
        final CaptureWebsitesTaskRequest expectedCaptureWebsitesTaskRequest = new CaptureWebsitesTaskRequest().setUrl("capture-websites-task-url");
        final AsyncResult<TaskResponseData> taskResponseDataAsyncResult = FutureAsyncResult.<TaskResponseData>builder().build();
        when(asyncRequestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.TASK_RESPONSE_DATA_TYPE_REFERENCE))).thenReturn(taskResponseDataAsyncResult);

        assertThat(asyncCloudConvertClient.tasks().capture(expectedCaptureWebsitesTaskRequest)).isEqualTo(taskResponseDataAsyncResult);
        verify(asyncRequestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.TASK_RESPONSE_DATA_TYPE_REFERENCE));

        final HttpUriRequest httpUriRequest = httpUriRequestArgumentCaptor.getValue();

        assertThat(httpUriRequest).isNotNull();
        assertThat(httpUriRequest.getMethod()).isEqualTo(HttpPost.METHOD_NAME);
        assertThat(httpUriRequest.getURI().toString()).isEqualTo(API_URL + "/" + AbstractResource.V2 + "/capture-website");
        assertThat(httpUriRequest).isInstanceOfSatisfying(HttpEntityEnclosingRequestBase.class, httpEntityEnclosingRequestBase -> {
            final CaptureWebsitesTaskRequest actualCaptureWebsitesTaskRequest = ThrowingSupplier.unchecked(() -> objectMapperProvider.provide()
                .readValue(httpEntityEnclosingRequestBase.getEntity().getContent(), CaptureWebsitesTaskRequest.class)).get();
            assertThat(actualCaptureWebsitesTaskRequest.getUrl()).isEqualTo(expectedCaptureWebsitesTaskRequest.getUrl());
        });
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_AUTHORIZATION)).hasSize(1).allSatisfy(header ->
            assertThat(VALUE_AUTHORIZATION).isEqualTo(header.getValue()));
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_USER_AGENT)).hasSize(1).allSatisfy(header ->
            assertThat(AbstractResource.VALUE_USER_AGENT).isEqualTo(header.getValue()));
    }

    @Test
    public void tasks_merge() throws Exception {
        final MergeFilesTaskRequest expectedMergeFilesTaskRequest = new MergeFilesTaskRequest().setInput("merge-files-task-input");
        final AsyncResult<TaskResponseData> taskResponseDataAsyncResult = FutureAsyncResult.<TaskResponseData>builder().build();
        when(asyncRequestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.TASK_RESPONSE_DATA_TYPE_REFERENCE))).thenReturn(taskResponseDataAsyncResult);

        assertThat(asyncCloudConvertClient.tasks().merge(expectedMergeFilesTaskRequest)).isEqualTo(taskResponseDataAsyncResult);
        verify(asyncRequestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.TASK_RESPONSE_DATA_TYPE_REFERENCE));

        final HttpUriRequest httpUriRequest = httpUriRequestArgumentCaptor.getValue();

        assertThat(httpUriRequest).isNotNull();
        assertThat(httpUriRequest.getMethod()).isEqualTo(HttpPost.METHOD_NAME);
        assertThat(httpUriRequest.getURI().toString()).isEqualTo(API_URL + "/" + AbstractResource.V2 + "/merge");
        assertThat(httpUriRequest).isInstanceOfSatisfying(HttpEntityEnclosingRequestBase.class, httpEntityEnclosingRequestBase -> {
            final MergeFilesTaskRequest actualMergeFilesTaskRequest = ThrowingSupplier.unchecked(() -> objectMapperProvider.provide()
                .readValue(httpEntityEnclosingRequestBase.getEntity().getContent(), MergeFilesTaskRequest.class)).get();
            assertThat(actualMergeFilesTaskRequest.getInput()).isEqualTo(expectedMergeFilesTaskRequest.getInput());
        });
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_AUTHORIZATION)).hasSize(1).allSatisfy(header ->
            assertThat(VALUE_AUTHORIZATION).isEqualTo(header.getValue()));
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_USER_AGENT)).hasSize(1).allSatisfy(header ->
            assertThat(AbstractResource.VALUE_USER_AGENT).isEqualTo(header.getValue()));
    }

    @Test
    public void tasks_archive() throws Exception {
        final CreateArchivesTaskRequest expectedCreateArchivesTaskRequest = new CreateArchivesTaskRequest().setInput("create-archives-task-input");
        final AsyncResult<TaskResponseData> taskResponseDataAsyncResult = FutureAsyncResult.<TaskResponseData>builder().build();
        when(asyncRequestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.TASK_RESPONSE_DATA_TYPE_REFERENCE))).thenReturn(taskResponseDataAsyncResult);

        assertThat(asyncCloudConvertClient.tasks().archive(expectedCreateArchivesTaskRequest)).isEqualTo(taskResponseDataAsyncResult);
        verify(asyncRequestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.TASK_RESPONSE_DATA_TYPE_REFERENCE));

        final HttpUriRequest httpUriRequest = httpUriRequestArgumentCaptor.getValue();

        assertThat(httpUriRequest).isNotNull();
        assertThat(httpUriRequest.getMethod()).isEqualTo(HttpPost.METHOD_NAME);
        assertThat(httpUriRequest.getURI().toString()).isEqualTo(API_URL + "/" + AbstractResource.V2 + "/archive");
        assertThat(httpUriRequest).isInstanceOfSatisfying(HttpEntityEnclosingRequestBase.class, httpEntityEnclosingRequestBase -> {
            final CreateArchivesTaskRequest actualCreateArchivesTaskRequest = ThrowingSupplier.unchecked(() -> objectMapperProvider.provide()
                .readValue(httpEntityEnclosingRequestBase.getEntity().getContent(), CreateArchivesTaskRequest.class)).get();
            assertThat(actualCreateArchivesTaskRequest.getInput()).isEqualTo(expectedCreateArchivesTaskRequest.getInput());
        });
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_AUTHORIZATION)).hasSize(1).allSatisfy(header ->
            assertThat(VALUE_AUTHORIZATION).isEqualTo(header.getValue()));
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_USER_AGENT)).hasSize(1).allSatisfy(header ->
            assertThat(AbstractResource.VALUE_USER_AGENT).isEqualTo(header.getValue()));
    }

    @Test
    public void tasks_command() throws Exception {
        final ExecuteCommandsTaskRequest expectedExecuteCommandsTaskRequest = new ExecuteCommandsTaskRequest().setInput("execute-commands-task-input");
        final AsyncResult<TaskResponseData> taskResponseDataAsyncResult = FutureAsyncResult.<TaskResponseData>builder().build();
        when(asyncRequestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.TASK_RESPONSE_DATA_TYPE_REFERENCE))).thenReturn(taskResponseDataAsyncResult);

        assertThat(asyncCloudConvertClient.tasks().command(expectedExecuteCommandsTaskRequest)).isEqualTo(taskResponseDataAsyncResult);
        verify(asyncRequestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.TASK_RESPONSE_DATA_TYPE_REFERENCE));

        final HttpUriRequest httpUriRequest = httpUriRequestArgumentCaptor.getValue();

        assertThat(httpUriRequest).isNotNull();
        assertThat(httpUriRequest.getMethod()).isEqualTo(HttpPost.METHOD_NAME);
        assertThat(httpUriRequest.getURI().toString()).isEqualTo(API_URL + "/" + AbstractResource.V2 + "/command");
        assertThat(httpUriRequest).isInstanceOfSatisfying(HttpEntityEnclosingRequestBase.class, httpEntityEnclosingRequestBase -> {
            final ExecuteCommandsTaskRequest actualExecuteCommandsTaskRequest = ThrowingSupplier.unchecked(() -> objectMapperProvider.provide()
                .readValue(httpEntityEnclosingRequestBase.getEntity().getContent(), ExecuteCommandsTaskRequest.class)).get();

            assertThat(actualExecuteCommandsTaskRequest.getInput()).isEqualTo(expectedExecuteCommandsTaskRequest.getInput());
        });
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_AUTHORIZATION)).hasSize(1).allSatisfy(header ->
            assertThat(VALUE_AUTHORIZATION).isEqualTo(header.getValue()));
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_USER_AGENT)).hasSize(1).allSatisfy(header ->
            assertThat(AbstractResource.VALUE_USER_AGENT).isEqualTo(header.getValue()));
    }

    @Test
    public void import_url() throws Exception {
        final UrlImportRequest expectedUrlImportRequest = new UrlImportRequest().setFilename("import-url-filename");
        final AsyncResult<TaskResponseData> taskResponseDataAsyncResult = FutureAsyncResult.<TaskResponseData>builder().build();
        when(asyncRequestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.TASK_RESPONSE_DATA_TYPE_REFERENCE))).thenReturn(taskResponseDataAsyncResult);

        assertThat(asyncCloudConvertClient.importUsing().url(expectedUrlImportRequest)).isEqualTo(taskResponseDataAsyncResult);
        verify(asyncRequestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.TASK_RESPONSE_DATA_TYPE_REFERENCE));

        final HttpUriRequest httpUriRequest = httpUriRequestArgumentCaptor.getValue();

        assertThat(httpUriRequest).isNotNull();
        assertThat(httpUriRequest.getMethod()).isEqualTo(HttpPost.METHOD_NAME);
        assertThat(httpUriRequest.getURI().toString()).isEqualTo(API_URL + "/" + AbstractResource.V2 + "/import/url");
        assertThat(httpUriRequest).isInstanceOfSatisfying(HttpEntityEnclosingRequestBase.class, httpEntityEnclosingRequestBase -> {
            final UrlImportRequest actualUrlImportRequest = ThrowingSupplier.unchecked(() -> objectMapperProvider.provide()
                .readValue(httpEntityEnclosingRequestBase.getEntity().getContent(), UrlImportRequest.class)).get();

            assertThat(actualUrlImportRequest.getFilename()).isEqualTo(expectedUrlImportRequest.getFilename());
        });
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_AUTHORIZATION)).hasSize(1).allSatisfy(header ->
            assertThat(VALUE_AUTHORIZATION).isEqualTo(header.getValue()));
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_USER_AGENT)).hasSize(1).allSatisfy(header ->
            assertThat(AbstractResource.VALUE_USER_AGENT).isEqualTo(header.getValue()));
    }

    @Test
    public void import_upload_noImmediateUpload() throws Exception {
        final UploadImportRequest expectedUploadImportRequest = new UploadImportRequest().setRedirect("import-upload-redirect");
        final AsyncResult<TaskResponseData> taskResponseDataAsyncResult = FutureAsyncResult.<TaskResponseData>builder().build();
        when(asyncRequestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.TASK_RESPONSE_DATA_TYPE_REFERENCE))).thenReturn(taskResponseDataAsyncResult);

        assertThat(asyncCloudConvertClient.importUsing().upload(expectedUploadImportRequest)).isEqualTo(taskResponseDataAsyncResult);
        verify(asyncRequestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.TASK_RESPONSE_DATA_TYPE_REFERENCE));

        final HttpUriRequest httpUriRequest = httpUriRequestArgumentCaptor.getValue();

        assertThat(httpUriRequest).isNotNull();
        assertThat(httpUriRequest.getMethod()).isEqualTo(HttpPost.METHOD_NAME);
        assertThat(httpUriRequest.getURI().toString()).isEqualTo(API_URL + "/" + AbstractResource.V2 + "/import/upload");
        assertThat(httpUriRequest).isInstanceOfSatisfying(HttpEntityEnclosingRequestBase.class, httpEntityEnclosingRequestBase -> {
            final UploadImportRequest actualUploadImportRequest = ThrowingSupplier.unchecked(() -> objectMapperProvider.provide()
                .readValue(httpEntityEnclosingRequestBase.getEntity().getContent(), UploadImportRequest.class)).get();

            assertThat(actualUploadImportRequest.getRedirect()).isEqualTo(expectedUploadImportRequest.getRedirect());
        });
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_AUTHORIZATION)).hasSize(1).allSatisfy(header ->
            assertThat(VALUE_AUTHORIZATION).isEqualTo(header.getValue()));
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_USER_AGENT)).hasSize(1).allSatisfy(header ->
            assertThat(AbstractResource.VALUE_USER_AGENT).isEqualTo(header.getValue()));
    }

    @Test
    public void import_upload_immediateUpload() throws Exception {
        final UploadImportRequest expectedUploadImportRequest = new UploadImportRequest().setRedirect("import-upload-redirect");
        final TaskResponse.Result.Form.Parameters parameters = new TaskResponse.Result.Form.Parameters().setExpires("expires")
            .setMaxFileCount("max-file-count").setMaxFileSize("max-file-size").setRedirect("redirect").setSignature("signature");
        final TaskResponse taskResponse = new TaskResponse().setId("import-upload-task-id").setResult(
            new TaskResponse.Result().setForm(new TaskResponse.Result.Form().setUrl("import-upload-task-result-form-url").setParameters(parameters)));
        final TaskResponseData taskResponseData = new TaskResponseData().setData(taskResponse);
        final AsyncResult<TaskResponseData> showTaskResponseDataAsyncResult = FutureAsyncResult.<TaskResponseData>builder().build();
        when(inputStream.read(any(byte[].class))).thenReturn(-1);
        when(asyncRequestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.TASK_RESPONSE_DATA_TYPE_REFERENCE)))
            .thenReturn(CompletedAsyncResult.<TaskResponseData>builder().result(
                Result.<TaskResponseData>builder().status(HttpStatus.SC_CREATED).body(taskResponseData).build()).build())
            .thenReturn(showTaskResponseDataAsyncResult);
        when(asyncRequestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.VOID_TYPE_REFERENCE))).thenReturn(
            CompletedAsyncResult.<Void>builder().result(Result.<Void>builder().status(HttpStatus.SC_CREATED).build()).build());

        assertThat(asyncCloudConvertClient.importUsing().upload(expectedUploadImportRequest, inputStream)).isEqualTo(showTaskResponseDataAsyncResult);
        verify(asyncRequestExecutor, times(2)).execute(any(HttpUriRequest.class), eq(AbstractResource.TASK_RESPONSE_DATA_TYPE_REFERENCE));
        verify(asyncRequestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.VOID_TYPE_REFERENCE));

        final HttpUriRequest httpUriRequest = httpUriRequestArgumentCaptor.getValue();

        assertThat(httpUriRequest).isNotNull();
        assertThat(httpUriRequest.getMethod()).isEqualTo(HttpPost.METHOD_NAME);
        assertThat(httpUriRequest.getURI().toString()).isEqualTo(taskResponse.getResult().getForm().getUrl());
        assertThat(httpUriRequest).isInstanceOf(HttpRequestBase.class);
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_AUTHORIZATION)).hasSize(1).allSatisfy(header ->
            assertThat(VALUE_AUTHORIZATION).isEqualTo(header.getValue()));
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_USER_AGENT)).hasSize(1).allSatisfy(header ->
            assertThat(AbstractResource.VALUE_USER_AGENT).isEqualTo(header.getValue()));
    }

    @Test
    public void import_s3() throws Exception {
        final S3ImportRequest expectedS3ImportRequest = new S3ImportRequest().setBucket("import-s3-bucket");
        final AsyncResult<TaskResponseData> taskResponseDataAsyncResult = FutureAsyncResult.<TaskResponseData>builder().build();
        when(asyncRequestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.TASK_RESPONSE_DATA_TYPE_REFERENCE))).thenReturn(taskResponseDataAsyncResult);

        assertThat(asyncCloudConvertClient.importUsing().s3(expectedS3ImportRequest)).isEqualTo(taskResponseDataAsyncResult);
        verify(asyncRequestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.TASK_RESPONSE_DATA_TYPE_REFERENCE));

        final HttpUriRequest httpUriRequest = httpUriRequestArgumentCaptor.getValue();

        assertThat(httpUriRequest).isNotNull();
        assertThat(httpUriRequest.getMethod()).isEqualTo(HttpPost.METHOD_NAME);
        assertThat(httpUriRequest.getURI().toString()).isEqualTo(API_URL + "/" + AbstractResource.V2 + "/import/s3");
        assertThat(httpUriRequest).isInstanceOfSatisfying(HttpEntityEnclosingRequestBase.class, httpEntityEnclosingRequestBase -> {
            final S3ImportRequest actualS3ImportRequest = ThrowingSupplier.unchecked(() -> objectMapperProvider.provide()
                .readValue(httpEntityEnclosingRequestBase.getEntity().getContent(), S3ImportRequest.class)).get();

            assertThat(actualS3ImportRequest.getBucket()).isEqualTo(expectedS3ImportRequest.getBucket());
        });
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_AUTHORIZATION)).hasSize(1).allSatisfy(header ->
            assertThat(VALUE_AUTHORIZATION).isEqualTo(header.getValue()));
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_USER_AGENT)).hasSize(1).allSatisfy(header ->
            assertThat(AbstractResource.VALUE_USER_AGENT).isEqualTo(header.getValue()));
    }

    @Test
    public void import_azureBlob() throws Exception {
        final AzureBlobImportRequest expectedAzureBlobImportRequest = new AzureBlobImportRequest().setStorageAccount("import-azure-blob-storage-account");
        final AsyncResult<TaskResponseData> taskResponseDataAsyncResult = FutureAsyncResult.<TaskResponseData>builder().build();
        when(asyncRequestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.TASK_RESPONSE_DATA_TYPE_REFERENCE))).thenReturn(taskResponseDataAsyncResult);

        assertThat(asyncCloudConvertClient.importUsing().azureBlob(expectedAzureBlobImportRequest)).isEqualTo(taskResponseDataAsyncResult);
        verify(asyncRequestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.TASK_RESPONSE_DATA_TYPE_REFERENCE));

        final HttpUriRequest httpUriRequest = httpUriRequestArgumentCaptor.getValue();

        assertThat(httpUriRequest).isNotNull();
        assertThat(httpUriRequest.getMethod()).isEqualTo(HttpPost.METHOD_NAME);
        assertThat(httpUriRequest.getURI().toString()).isEqualTo(API_URL + "/" + AbstractResource.V2 + "/import/azure/blob");
        assertThat(httpUriRequest).isInstanceOfSatisfying(HttpEntityEnclosingRequestBase.class, httpEntityEnclosingRequestBase -> {
            final AzureBlobImportRequest actualAzureBlobImportRequest = ThrowingSupplier.unchecked(() -> objectMapperProvider.provide()
                .readValue(httpEntityEnclosingRequestBase.getEntity().getContent(), AzureBlobImportRequest.class)).get();

            assertThat(actualAzureBlobImportRequest.getStorageAccount()).isEqualTo(expectedAzureBlobImportRequest.getStorageAccount());
        });
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_AUTHORIZATION)).hasSize(1).allSatisfy(header ->
            assertThat(VALUE_AUTHORIZATION).isEqualTo(header.getValue()));
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_USER_AGENT)).hasSize(1).allSatisfy(header ->
            assertThat(AbstractResource.VALUE_USER_AGENT).isEqualTo(header.getValue()));
    }

    @Test
    public void import_googleCloudStorage() throws Exception {
        final GoogleCloudStorageImportRequest expectedGoogleCloudStorageImportRequest = new GoogleCloudStorageImportRequest().setBucket("import-google-cloud-storage-bucket");
        final AsyncResult<TaskResponseData> taskResponseDataAsyncResult = FutureAsyncResult.<TaskResponseData>builder().build();
        when(asyncRequestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.TASK_RESPONSE_DATA_TYPE_REFERENCE))).thenReturn(taskResponseDataAsyncResult);

        assertThat(asyncCloudConvertClient.importUsing().googleCloudStorage(expectedGoogleCloudStorageImportRequest)).isEqualTo(taskResponseDataAsyncResult);
        verify(asyncRequestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.TASK_RESPONSE_DATA_TYPE_REFERENCE));

        final HttpUriRequest httpUriRequest = httpUriRequestArgumentCaptor.getValue();

        assertThat(httpUriRequest).isNotNull();
        assertThat(httpUriRequest.getMethod()).isEqualTo(HttpPost.METHOD_NAME);
        assertThat(httpUriRequest.getURI().toString()).isEqualTo(API_URL + "/" + AbstractResource.V2 + "/import/google-cloud-storage");
        assertThat(httpUriRequest).isInstanceOfSatisfying(HttpEntityEnclosingRequestBase.class, httpEntityEnclosingRequestBase -> {
            final GoogleCloudStorageImportRequest actualGoogleCloudStorageImportRequest = ThrowingSupplier.unchecked(() -> objectMapperProvider.provide()
                .readValue(httpEntityEnclosingRequestBase.getEntity().getContent(), GoogleCloudStorageImportRequest.class)).get();

            assertThat(actualGoogleCloudStorageImportRequest.getBucket()).isEqualTo(expectedGoogleCloudStorageImportRequest.getBucket());
        });
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_AUTHORIZATION)).hasSize(1).allSatisfy(header ->
            assertThat(VALUE_AUTHORIZATION).isEqualTo(header.getValue()));
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_USER_AGENT)).hasSize(1).allSatisfy(header ->
            assertThat(AbstractResource.VALUE_USER_AGENT).isEqualTo(header.getValue()));
    }

    @Test
    public void import_openStack() throws Exception {
        final OpenStackImportRequest expectedOpenStackImportRequest = new OpenStackImportRequest().setContainer("import-open-stack-container");
        final AsyncResult<TaskResponseData> taskResponseDataAsyncResult = FutureAsyncResult.<TaskResponseData>builder().build();
        when(asyncRequestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.TASK_RESPONSE_DATA_TYPE_REFERENCE))).thenReturn(taskResponseDataAsyncResult);

        assertThat(asyncCloudConvertClient.importUsing().openStack(expectedOpenStackImportRequest)).isEqualTo(taskResponseDataAsyncResult);
        verify(asyncRequestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.TASK_RESPONSE_DATA_TYPE_REFERENCE));

        final HttpUriRequest httpUriRequest = httpUriRequestArgumentCaptor.getValue();

        assertThat(httpUriRequest).isNotNull();
        assertThat(httpUriRequest.getMethod()).isEqualTo(HttpPost.METHOD_NAME);
        assertThat(httpUriRequest.getURI().toString()).isEqualTo(API_URL + "/" + AbstractResource.V2 + "/import/openstack");
        assertThat(httpUriRequest).isInstanceOfSatisfying(HttpEntityEnclosingRequestBase.class, httpEntityEnclosingRequestBase -> {
            final OpenStackImportRequest actualOpenStackImportRequest = ThrowingSupplier.unchecked(() -> objectMapperProvider.provide()
                .readValue(httpEntityEnclosingRequestBase.getEntity().getContent(), OpenStackImportRequest.class)).get();

            assertThat(actualOpenStackImportRequest.getContainer()).isEqualTo(expectedOpenStackImportRequest.getContainer());
        });
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_AUTHORIZATION)).hasSize(1).allSatisfy(header ->
            assertThat(VALUE_AUTHORIZATION).isEqualTo(header.getValue()));
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_USER_AGENT)).hasSize(1).allSatisfy(header ->
            assertThat(AbstractResource.VALUE_USER_AGENT).isEqualTo(header.getValue()));
    }

    @Test
    public void import_sftp() throws Exception {
        final SftpImportRequest expectedSftpImportRequest = new SftpImportRequest().setFilename("import-sftp-filename");
        final AsyncResult<TaskResponseData> taskResponseDataAsyncResult = FutureAsyncResult.<TaskResponseData>builder().build();
        when(asyncRequestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.TASK_RESPONSE_DATA_TYPE_REFERENCE))).thenReturn(taskResponseDataAsyncResult);

        assertThat(asyncCloudConvertClient.importUsing().sftp(expectedSftpImportRequest)).isEqualTo(taskResponseDataAsyncResult);
        verify(asyncRequestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.TASK_RESPONSE_DATA_TYPE_REFERENCE));

        final HttpUriRequest httpUriRequest = httpUriRequestArgumentCaptor.getValue();

        assertThat(httpUriRequest).isNotNull();
        assertThat(httpUriRequest.getMethod()).isEqualTo(HttpPost.METHOD_NAME);
        assertThat(httpUriRequest.getURI().toString()).isEqualTo(API_URL + "/" + AbstractResource.V2 + "/import/sftp");
        assertThat(httpUriRequest).isInstanceOfSatisfying(HttpEntityEnclosingRequestBase.class, httpEntityEnclosingRequestBase -> {
            final SftpImportRequest actualSftpImportRequest = ThrowingSupplier.unchecked(() -> objectMapperProvider.provide()
                .readValue(httpEntityEnclosingRequestBase.getEntity().getContent(), SftpImportRequest.class)).get();

            assertThat(actualSftpImportRequest.getFilename()).isEqualTo(actualSftpImportRequest.getFilename());
        });
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_AUTHORIZATION)).hasSize(1).allSatisfy(header ->
            assertThat(VALUE_AUTHORIZATION).isEqualTo(header.getValue()));
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_USER_AGENT)).hasSize(1).allSatisfy(header ->
            assertThat(AbstractResource.VALUE_USER_AGENT).isEqualTo(header.getValue()));
    }

    @Test
    public void export_url() throws Exception {
        final UrlExportRequest expectedUrlExportRequest = new UrlExportRequest().setInput("export-url-input");
        final AsyncResult<TaskResponseData> taskResponseDataAsyncResult = FutureAsyncResult.<TaskResponseData>builder().build();
        when(asyncRequestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.TASK_RESPONSE_DATA_TYPE_REFERENCE))).thenReturn(taskResponseDataAsyncResult);

        assertThat(asyncCloudConvertClient.exportUsing().url(expectedUrlExportRequest)).isEqualTo(taskResponseDataAsyncResult);
        verify(asyncRequestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.TASK_RESPONSE_DATA_TYPE_REFERENCE));

        final HttpUriRequest httpUriRequest = httpUriRequestArgumentCaptor.getValue();

        assertThat(httpUriRequest).isNotNull();
        assertThat(httpUriRequest.getMethod()).isEqualTo(HttpPost.METHOD_NAME);
        assertThat(httpUriRequest.getURI().toString()).isEqualTo(API_URL + "/" + AbstractResource.V2 + "/export/url");
        assertThat(httpUriRequest).isInstanceOfSatisfying(HttpEntityEnclosingRequestBase.class, httpEntityEnclosingRequestBase -> {
            final UrlExportRequest actualUrlExportRequest = ThrowingSupplier.unchecked(() -> objectMapperProvider.provide()
                .readValue(httpEntityEnclosingRequestBase.getEntity().getContent(), UrlExportRequest.class)).get();

            assertThat(actualUrlExportRequest.getInput()).isEqualTo(expectedUrlExportRequest.getInput());
        });
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_AUTHORIZATION)).hasSize(1).allSatisfy(header ->
            assertThat(VALUE_AUTHORIZATION).isEqualTo(header.getValue()));
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_USER_AGENT)).hasSize(1).allSatisfy(header ->
            assertThat(AbstractResource.VALUE_USER_AGENT).isEqualTo(header.getValue()));
    }

    @Test
    public void export_s3() throws Exception {
        final S3ExportRequest expectedS3ExportRequest = new S3ExportRequest().setInput("export-s3-input");
        final AsyncResult<TaskResponseData> taskResponseDataAsyncResult = FutureAsyncResult.<TaskResponseData>builder().build();
        when(asyncRequestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.TASK_RESPONSE_DATA_TYPE_REFERENCE))).thenReturn(taskResponseDataAsyncResult);

        assertThat(asyncCloudConvertClient.exportUsing().s3(expectedS3ExportRequest)).isEqualTo(taskResponseDataAsyncResult);
        verify(asyncRequestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.TASK_RESPONSE_DATA_TYPE_REFERENCE));

        final HttpUriRequest httpUriRequest = httpUriRequestArgumentCaptor.getValue();

        assertThat(httpUriRequest).isNotNull();
        assertThat(httpUriRequest.getMethod()).isEqualTo(HttpPost.METHOD_NAME);
        assertThat(httpUriRequest.getURI().toString()).isEqualTo(API_URL + "/" + AbstractResource.V2 + "/export/s3");
        assertThat(httpUriRequest).isInstanceOfSatisfying(HttpEntityEnclosingRequestBase.class, httpEntityEnclosingRequestBase -> {
            final S3ExportRequest actualS3ExportRequest = ThrowingSupplier.unchecked(() -> objectMapperProvider.provide()
                .readValue(httpEntityEnclosingRequestBase.getEntity().getContent(), S3ExportRequest.class)).get();

            assertThat(actualS3ExportRequest.getInput()).isEqualTo(expectedS3ExportRequest.getInput());
        });
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_AUTHORIZATION)).hasSize(1).allSatisfy(header ->
            assertThat(VALUE_AUTHORIZATION).isEqualTo(header.getValue()));
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_USER_AGENT)).hasSize(1).allSatisfy(header ->
            assertThat(AbstractResource.VALUE_USER_AGENT).isEqualTo(header.getValue()));
    }

    @Test
    public void export_azureBlob() throws Exception {
        final AzureBlobExportRequest expectedAzureBlobExportRequest = new AzureBlobExportRequest().setInput("export-azure-blob-input");
        final AsyncResult<TaskResponseData> taskResponseDataAsyncResult = FutureAsyncResult.<TaskResponseData>builder().build();
        when(asyncRequestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.TASK_RESPONSE_DATA_TYPE_REFERENCE))).thenReturn(taskResponseDataAsyncResult);

        assertThat(asyncCloudConvertClient.exportUsing().azureBlob(expectedAzureBlobExportRequest)).isEqualTo(taskResponseDataAsyncResult);
        verify(asyncRequestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.TASK_RESPONSE_DATA_TYPE_REFERENCE));

        final HttpUriRequest httpUriRequest = httpUriRequestArgumentCaptor.getValue();

        assertThat(httpUriRequest).isNotNull();
        assertThat(httpUriRequest.getMethod()).isEqualTo(HttpPost.METHOD_NAME);
        assertThat(httpUriRequest.getURI().toString()).isEqualTo(API_URL + "/" + AbstractResource.V2 + "/export/azure/blob");
        assertThat(httpUriRequest).isInstanceOfSatisfying(HttpEntityEnclosingRequestBase.class, httpEntityEnclosingRequestBase -> {
            final AzureBlobExportRequest actualAzureBlobExportRequest = ThrowingSupplier.unchecked(() -> objectMapperProvider.provide()
                .readValue(httpEntityEnclosingRequestBase.getEntity().getContent(), AzureBlobExportRequest.class)).get();

            assertThat(actualAzureBlobExportRequest.getInput()).isEqualTo(expectedAzureBlobExportRequest.getInput());
        });
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_AUTHORIZATION)).hasSize(1).allSatisfy(header ->
            assertThat(VALUE_AUTHORIZATION).isEqualTo(header.getValue()));
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_USER_AGENT)).hasSize(1).allSatisfy(header ->
            assertThat(AbstractResource.VALUE_USER_AGENT).isEqualTo(header.getValue()));
    }

    @Test
    public void export_googleCloudStorage() throws Exception {
        final GoogleCloudStorageExportRequest expectedGoogleCloudStorageExportRequest = new GoogleCloudStorageExportRequest().setBucket("export-google-cloud-storage-bucket");
        final AsyncResult<TaskResponseData> taskResponseDataAsyncResult = FutureAsyncResult.<TaskResponseData>builder().build();
        when(asyncRequestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.TASK_RESPONSE_DATA_TYPE_REFERENCE))).thenReturn(taskResponseDataAsyncResult);

        assertThat(asyncCloudConvertClient.exportUsing().googleCloudStorage(expectedGoogleCloudStorageExportRequest)).isEqualTo(taskResponseDataAsyncResult);
        verify(asyncRequestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.TASK_RESPONSE_DATA_TYPE_REFERENCE));

        final HttpUriRequest httpUriRequest = httpUriRequestArgumentCaptor.getValue();

        assertThat(httpUriRequest).isNotNull();
        assertThat(httpUriRequest.getMethod()).isEqualTo(HttpPost.METHOD_NAME);
        assertThat(httpUriRequest.getURI().toString()).isEqualTo(API_URL + "/" + AbstractResource.V2 + "/export/google-cloud-storage");
        assertThat(httpUriRequest).isInstanceOfSatisfying(HttpEntityEnclosingRequestBase.class, httpEntityEnclosingRequestBase -> {
            final GoogleCloudStorageExportRequest actualGoogleCloudStorageExportRequest = ThrowingSupplier.unchecked(() -> objectMapperProvider.provide()
                .readValue(httpEntityEnclosingRequestBase.getEntity().getContent(), GoogleCloudStorageExportRequest.class)).get();

            assertThat(actualGoogleCloudStorageExportRequest.getBucket()).isEqualTo(expectedGoogleCloudStorageExportRequest.getBucket());
        });
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_AUTHORIZATION)).hasSize(1).allSatisfy(header ->
            assertThat(VALUE_AUTHORIZATION).isEqualTo(header.getValue()));
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_USER_AGENT)).hasSize(1).allSatisfy(header ->
            assertThat(AbstractResource.VALUE_USER_AGENT).isEqualTo(header.getValue()));
    }

    @Test
    public void export_openStack() throws Exception {
        final OpenStackExportRequest expectedOpenStackExportRequest = new OpenStackExportRequest().setContainer("export-open-stack-container");
        final AsyncResult<TaskResponseData> taskResponseDataAsyncResult = FutureAsyncResult.<TaskResponseData>builder().build();
        when(asyncRequestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.TASK_RESPONSE_DATA_TYPE_REFERENCE))).thenReturn(taskResponseDataAsyncResult);

        assertThat(asyncCloudConvertClient.exportUsing().openStack(expectedOpenStackExportRequest)).isEqualTo(taskResponseDataAsyncResult);
        verify(asyncRequestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.TASK_RESPONSE_DATA_TYPE_REFERENCE));

        final HttpUriRequest httpUriRequest = httpUriRequestArgumentCaptor.getValue();

        assertThat(httpUriRequest).isNotNull();
        assertThat(httpUriRequest.getMethod()).isEqualTo(HttpPost.METHOD_NAME);
        assertThat(httpUriRequest.getURI().toString()).isEqualTo(API_URL + "/" + AbstractResource.V2 + "/export/openstack");
        assertThat(httpUriRequest).isInstanceOfSatisfying(HttpEntityEnclosingRequestBase.class, httpEntityEnclosingRequestBase -> {
            final OpenStackExportRequest actualOpenStackExportRequest = ThrowingSupplier.unchecked(() -> objectMapperProvider.provide()
                .readValue(httpEntityEnclosingRequestBase.getEntity().getContent(), OpenStackExportRequest.class)).get();

            assertThat(actualOpenStackExportRequest.getContainer()).isEqualTo(expectedOpenStackExportRequest.getContainer());
        });
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_AUTHORIZATION)).hasSize(1).allSatisfy(header ->
            assertThat(VALUE_AUTHORIZATION).isEqualTo(header.getValue()));
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_USER_AGENT)).hasSize(1).allSatisfy(header ->
            assertThat(AbstractResource.VALUE_USER_AGENT).isEqualTo(header.getValue()));
    }

    @Test
    public void export_sftp() throws Exception {
        final SftpExportRequest expectedSftpExportRequest = new SftpExportRequest().setInput("export-sftp-input");
        final AsyncResult<TaskResponseData> taskResponseDataAsyncResult = FutureAsyncResult.<TaskResponseData>builder().build();
        when(asyncRequestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.TASK_RESPONSE_DATA_TYPE_REFERENCE))).thenReturn(taskResponseDataAsyncResult);

        assertThat(asyncCloudConvertClient.exportUsing().sftp(expectedSftpExportRequest)).isEqualTo(taskResponseDataAsyncResult);
        verify(asyncRequestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.TASK_RESPONSE_DATA_TYPE_REFERENCE));

        final HttpUriRequest httpUriRequest = httpUriRequestArgumentCaptor.getValue();

        assertThat(httpUriRequest).isNotNull();
        assertThat(httpUriRequest.getMethod()).isEqualTo(HttpPost.METHOD_NAME);
        assertThat(httpUriRequest.getURI().toString()).isEqualTo(API_URL + "/" + AbstractResource.V2 + "/export/sftp");
        assertThat(httpUriRequest).isInstanceOfSatisfying(HttpEntityEnclosingRequestBase.class, httpEntityEnclosingRequestBase -> {
            final SftpExportRequest actualSftpExportRequest = ThrowingSupplier.unchecked(() -> objectMapperProvider.provide()
                .readValue(httpEntityEnclosingRequestBase.getEntity().getContent(), SftpExportRequest.class)).get();

            assertThat(actualSftpExportRequest.getInput()).isEqualTo(expectedSftpExportRequest.getInput());
        });
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_AUTHORIZATION)).hasSize(1).allSatisfy(header ->
            assertThat(VALUE_AUTHORIZATION).isEqualTo(header.getValue()));
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_USER_AGENT)).hasSize(1).allSatisfy(header ->
            assertThat(AbstractResource.VALUE_USER_AGENT).isEqualTo(header.getValue()));
    }

    @Test
    public void users_me() throws Exception {
        final AsyncResult<UserResponseData> userResponseDataAsyncResult = FutureAsyncResult.<UserResponseData>builder().build();
        when(asyncRequestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.USER_RESPONSE_DATA_TYPE_REFERENCE))).thenReturn(userResponseDataAsyncResult);

        assertThat(asyncCloudConvertClient.users().me()).isEqualTo(userResponseDataAsyncResult);
        verify(asyncRequestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.USER_RESPONSE_DATA_TYPE_REFERENCE));

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
        final AsyncResult<WebhookResponseData> webhookResponseDataAsyncResult = FutureAsyncResult.<WebhookResponseData>builder().build();
        when(asyncRequestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.WEBHOOKS_RESPONSE_DATA_TYPE_REFERENCE))).thenReturn(webhookResponseDataAsyncResult);

        assertThat(asyncCloudConvertClient.webhooks().create(expectedWebhookRequest)).isEqualTo(webhookResponseDataAsyncResult);
        verify(asyncRequestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.WEBHOOKS_RESPONSE_DATA_TYPE_REFERENCE));

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
