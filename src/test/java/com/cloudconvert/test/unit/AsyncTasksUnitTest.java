package com.cloudconvert.test.unit;

import com.cloudconvert.client.AsyncCloudConvertClient;
import com.cloudconvert.client.mapper.ObjectMapperProvider;
import com.cloudconvert.client.setttings.SettingsProvider;
import com.cloudconvert.dto.request.CaptureWebsitesTaskRequest;
import com.cloudconvert.dto.request.ConvertFilesTaskRequest;
import com.cloudconvert.dto.request.CreateArchivesTaskRequest;
import com.cloudconvert.dto.request.ExecuteCommandsTaskRequest;
import com.cloudconvert.dto.request.MergeFilesTaskRequest;
import com.cloudconvert.dto.request.OptimizeFilesTaskRequest;
import com.cloudconvert.dto.response.OperationResponse;
import com.cloudconvert.dto.response.Pageable;
import com.cloudconvert.dto.response.TaskResponse;
import com.cloudconvert.dto.response.TaskResponseData;
import com.cloudconvert.dto.result.AsyncResult;
import com.cloudconvert.dto.result.FutureAsyncResult;
import com.cloudconvert.executor.AsyncRequestExecutor;
import com.cloudconvert.resource.AbstractResource;
import com.cloudconvert.resource.params.Filter;
import com.cloudconvert.resource.params.Include;
import com.cloudconvert.resource.params.Pagination;
import com.cloudconvert.test.framework.AbstractTest;
import com.cloudconvert.test.framework.UnitTest;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
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
public class AsyncTasksUnitTest extends AbstractTest {

    private static final String TASK_ID = "task-id";

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

        asyncCloudConvertClient = new AsyncCloudConvertClient(settingsProvider, objectMapperProvider, asyncRequestExecutor);
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

    @After
    public void after() throws Exception {
        asyncCloudConvertClient.close();
    }
}
