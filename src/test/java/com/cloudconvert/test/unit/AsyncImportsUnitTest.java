package com.cloudconvert.test.unit;

import com.cloudconvert.client.AsyncCloudConvertClient;
import com.cloudconvert.client.mapper.ObjectMapperProvider;
import com.cloudconvert.client.setttings.SettingsProvider;
import com.cloudconvert.dto.request.AzureBlobImportRequest;
import com.cloudconvert.dto.request.GoogleCloudStorageImportRequest;
import com.cloudconvert.dto.request.OpenStackImportRequest;
import com.cloudconvert.dto.request.S3ImportRequest;
import com.cloudconvert.dto.request.SftpImportRequest;
import com.cloudconvert.dto.request.UploadImportRequest;
import com.cloudconvert.dto.request.UrlImportRequest;
import com.cloudconvert.dto.response.TaskResponse;
import com.cloudconvert.dto.result.AsyncResult;
import com.cloudconvert.dto.result.CompletedAsyncResult;
import com.cloudconvert.dto.result.FutureAsyncResult;
import com.cloudconvert.dto.result.Result;
import com.cloudconvert.executor.AsyncRequestExecutor;
import com.cloudconvert.resource.AbstractResource;
import com.cloudconvert.test.framework.AbstractTest;
import com.cloudconvert.test.framework.UnitTest;
import com.google.common.collect.ImmutableMap;
import com.pivovarit.function.ThrowingSupplier;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
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

import java.io.InputStream;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Category(UnitTest.class)
@RunWith(MockitoJUnitRunner.class)
public class AsyncImportsUnitTest extends AbstractTest {

    @Mock
    private SettingsProvider settingsProvider;

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
    public void before() {
        when(settingsProvider.getApiKey()).thenReturn(API_KEY);
        when(settingsProvider.getApiUrl()).thenReturn(API_URL);

        asyncCloudConvertClient = new AsyncCloudConvertClient(settingsProvider, objectMapperProvider, asyncRequestExecutor);
    }

    @Test
    public void import_url() throws Exception {
        final UrlImportRequest expectedUrlImportRequest = new UrlImportRequest().setFilename("import-url-filename");
        final AsyncResult<TaskResponse> TaskResponseAsyncResult = FutureAsyncResult.<TaskResponse>builder().build();
        when(asyncRequestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.TASK_RESPONSE_TYPE_REFERENCE))).thenReturn(TaskResponseAsyncResult);

        assertThat(asyncCloudConvertClient.importUsing().url(expectedUrlImportRequest)).isEqualTo(TaskResponseAsyncResult);
        verify(asyncRequestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.TASK_RESPONSE_TYPE_REFERENCE));

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
        final AsyncResult<TaskResponse> TaskResponseAsyncResult = FutureAsyncResult.<TaskResponse>builder().build();
        when(asyncRequestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.TASK_RESPONSE_TYPE_REFERENCE))).thenReturn(TaskResponseAsyncResult);

        assertThat(asyncCloudConvertClient.importUsing().upload(expectedUploadImportRequest)).isEqualTo(TaskResponseAsyncResult);
        verify(asyncRequestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.TASK_RESPONSE_TYPE_REFERENCE));

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
        final Map<String, String> parameters = ImmutableMap.of("expires", "expires", "max-file-count", "max-file-count",
            "max-file-size", "max-file-size", "redirect", "redirect", "signature", "signature");
        final TaskResponse taskResponse = new TaskResponse().setId("import-upload-task-id").setResult(
            new TaskResponse.Result().setForm(new TaskResponse.Result.Form().setUrl("import-upload-task-result-form-url").setParameters(parameters)));
        final AsyncResult<TaskResponse> showTaskResponseAsyncResult = FutureAsyncResult.<TaskResponse>builder().build();
        when(asyncRequestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.TASK_RESPONSE_TYPE_REFERENCE)))
            .thenReturn(CompletedAsyncResult.<TaskResponse>builder().result(
                Result.<TaskResponse>builder().status(HttpStatus.SC_CREATED).body(taskResponse).build()).build())
            .thenReturn(showTaskResponseAsyncResult);
        when(asyncRequestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.VOID_TYPE_REFERENCE))).thenReturn(
            CompletedAsyncResult.<Void>builder().result(Result.<Void>builder().status(HttpStatus.SC_CREATED).build()).build());

        assertThat(asyncCloudConvertClient.importUsing().upload(expectedUploadImportRequest, inputStream)).isEqualTo(showTaskResponseAsyncResult);
        verify(asyncRequestExecutor, times(2)).execute(any(HttpUriRequest.class), eq(AbstractResource.TASK_RESPONSE_TYPE_REFERENCE));
        verify(asyncRequestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.VOID_TYPE_REFERENCE));

        final HttpUriRequest httpUriRequest = httpUriRequestArgumentCaptor.getValue();

        assertThat(httpUriRequest).isNotNull();
        assertThat(httpUriRequest.getMethod()).isEqualTo(HttpPost.METHOD_NAME);
        assertThat(httpUriRequest.getURI().toString()).isEqualTo(taskResponse.getResult().getForm().getUrl());
        assertThat(httpUriRequest).isInstanceOf(HttpRequestBase.class);
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_AUTHORIZATION)).hasSize(0).allSatisfy(header ->
            assertThat(VALUE_AUTHORIZATION).isEqualTo(header.getValue()));
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_USER_AGENT)).hasSize(1).allSatisfy(header ->
            assertThat(AbstractResource.VALUE_USER_AGENT).isEqualTo(header.getValue()));
    }

    @Test
    public void import_s3() throws Exception {
        final S3ImportRequest expectedS3ImportRequest = new S3ImportRequest().setBucket("import-s3-bucket");
        final AsyncResult<TaskResponse> TaskResponseAsyncResult = FutureAsyncResult.<TaskResponse>builder().build();
        when(asyncRequestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.TASK_RESPONSE_TYPE_REFERENCE))).thenReturn(TaskResponseAsyncResult);

        assertThat(asyncCloudConvertClient.importUsing().s3(expectedS3ImportRequest)).isEqualTo(TaskResponseAsyncResult);
        verify(asyncRequestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.TASK_RESPONSE_TYPE_REFERENCE));

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
        final AsyncResult<TaskResponse> TaskResponseAsyncResult = FutureAsyncResult.<TaskResponse>builder().build();
        when(asyncRequestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.TASK_RESPONSE_TYPE_REFERENCE))).thenReturn(TaskResponseAsyncResult);

        assertThat(asyncCloudConvertClient.importUsing().azureBlob(expectedAzureBlobImportRequest)).isEqualTo(TaskResponseAsyncResult);
        verify(asyncRequestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.TASK_RESPONSE_TYPE_REFERENCE));

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
        final AsyncResult<TaskResponse> TaskResponseAsyncResult = FutureAsyncResult.<TaskResponse>builder().build();
        when(asyncRequestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.TASK_RESPONSE_TYPE_REFERENCE))).thenReturn(TaskResponseAsyncResult);

        assertThat(asyncCloudConvertClient.importUsing().googleCloudStorage(expectedGoogleCloudStorageImportRequest)).isEqualTo(TaskResponseAsyncResult);
        verify(asyncRequestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.TASK_RESPONSE_TYPE_REFERENCE));

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
        final AsyncResult<TaskResponse> TaskResponseAsyncResult = FutureAsyncResult.<TaskResponse>builder().build();
        when(asyncRequestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.TASK_RESPONSE_TYPE_REFERENCE))).thenReturn(TaskResponseAsyncResult);

        assertThat(asyncCloudConvertClient.importUsing().openStack(expectedOpenStackImportRequest)).isEqualTo(TaskResponseAsyncResult);
        verify(asyncRequestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.TASK_RESPONSE_TYPE_REFERENCE));

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
        final AsyncResult<TaskResponse> TaskResponseAsyncResult = FutureAsyncResult.<TaskResponse>builder().build();
        when(asyncRequestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.TASK_RESPONSE_TYPE_REFERENCE))).thenReturn(TaskResponseAsyncResult);

        assertThat(asyncCloudConvertClient.importUsing().sftp(expectedSftpImportRequest)).isEqualTo(TaskResponseAsyncResult);
        verify(asyncRequestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.TASK_RESPONSE_TYPE_REFERENCE));

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

    @After
    public void after() throws Exception {
        asyncCloudConvertClient.close();
    }
}
