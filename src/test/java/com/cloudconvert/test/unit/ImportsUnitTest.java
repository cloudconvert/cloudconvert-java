package com.cloudconvert.test.unit;

import com.cloudconvert.client.CloudConvertClient;
import com.cloudconvert.client.mapper.ObjectMapperProvider;
import com.cloudconvert.client.setttings.SettingsProvider;
import com.cloudconvert.dto.request.AzureBlobImportRequest;
import com.cloudconvert.dto.request.Base64ImportRequest;
import com.cloudconvert.dto.request.GoogleCloudStorageImportRequest;
import com.cloudconvert.dto.request.OpenStackImportRequest;
import com.cloudconvert.dto.request.RawImportRequest;
import com.cloudconvert.dto.request.S3ImportRequest;
import com.cloudconvert.dto.request.SftpImportRequest;
import com.cloudconvert.dto.request.UploadImportRequest;
import com.cloudconvert.dto.request.UrlImportRequest;
import com.cloudconvert.dto.response.TaskResponse;
import com.cloudconvert.dto.result.Result;
import com.cloudconvert.dto.result.Status;
import com.cloudconvert.executor.RequestExecutor;
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
public class ImportsUnitTest extends AbstractTest {

    @Mock
    private SettingsProvider settingsProvider;

    @Mock
    private RequestExecutor requestExecutor;

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private ObjectMapperProvider objectMapperProvider;

    @Mock
    private InputStream inputStream;

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
    public void import_url() throws Exception {
        final UrlImportRequest expectedUrlImportRequest = new UrlImportRequest().setFilename("import-url-filename");
        final Result<TaskResponse> taskResponseResult = Result.<TaskResponse>builder().build();
        when(requestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.TASK_RESPONSE_TYPE_REFERENCE))).thenReturn(taskResponseResult);

        assertThat(cloudConvertClient.importUsing().url(expectedUrlImportRequest)).isEqualTo(taskResponseResult);
        verify(requestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.TASK_RESPONSE_TYPE_REFERENCE));

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
        final Result<TaskResponse> taskResponseResult = Result.<TaskResponse>builder().build();
        when(requestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.TASK_RESPONSE_TYPE_REFERENCE))).thenReturn(taskResponseResult);

        assertThat(cloudConvertClient.importUsing().upload(expectedUploadImportRequest)).isEqualTo(taskResponseResult);
        verify(requestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.TASK_RESPONSE_TYPE_REFERENCE));

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
        final Result<TaskResponse> showTaskResponseResult = Result.<TaskResponse>builder().build();
        when(requestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.TASK_RESPONSE_TYPE_REFERENCE)))
            .thenReturn(Result.<TaskResponse>builder().status(Status.builder().code(HttpStatus.SC_CREATED).build()).body(taskResponse).build())
            .thenReturn(showTaskResponseResult);
        when(requestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.VOID_TYPE_REFERENCE)))
            .thenReturn(Result.<Void>builder().status(Status.builder().code(HttpStatus.SC_CREATED).build()).build());

        assertThat(cloudConvertClient.importUsing().upload(expectedUploadImportRequest, inputStream)).isEqualTo(showTaskResponseResult);
        verify(requestExecutor, times(2)).execute(any(HttpUriRequest.class), eq(AbstractResource.TASK_RESPONSE_TYPE_REFERENCE));
        verify(requestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.VOID_TYPE_REFERENCE));

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
        final Result<TaskResponse> taskResponseResult = Result.<TaskResponse>builder().build();
        when(requestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.TASK_RESPONSE_TYPE_REFERENCE))).thenReturn(taskResponseResult);

        assertThat(cloudConvertClient.importUsing().s3(expectedS3ImportRequest)).isEqualTo(taskResponseResult);
        verify(requestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.TASK_RESPONSE_TYPE_REFERENCE));

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
        final Result<TaskResponse> taskResponseResult = Result.<TaskResponse>builder().build();
        when(requestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.TASK_RESPONSE_TYPE_REFERENCE))).thenReturn(taskResponseResult);

        assertThat(cloudConvertClient.importUsing().azureBlob(expectedAzureBlobImportRequest)).isEqualTo(taskResponseResult);
        verify(requestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.TASK_RESPONSE_TYPE_REFERENCE));

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
        final Result<TaskResponse> taskResponseResult = Result.<TaskResponse>builder().build();
        when(requestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.TASK_RESPONSE_TYPE_REFERENCE))).thenReturn(taskResponseResult);

        assertThat(cloudConvertClient.importUsing().googleCloudStorage(expectedGoogleCloudStorageImportRequest)).isEqualTo(taskResponseResult);
        verify(requestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.TASK_RESPONSE_TYPE_REFERENCE));

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
        final Result<TaskResponse> taskResponseResult = Result.<TaskResponse>builder().build();
        when(requestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.TASK_RESPONSE_TYPE_REFERENCE))).thenReturn(taskResponseResult);

        assertThat(cloudConvertClient.importUsing().openStack(expectedOpenStackImportRequest)).isEqualTo(taskResponseResult);
        verify(requestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.TASK_RESPONSE_TYPE_REFERENCE));

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
        final Result<TaskResponse> taskResponseResult = Result.<TaskResponse>builder().build();
        when(requestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.TASK_RESPONSE_TYPE_REFERENCE))).thenReturn(taskResponseResult);

        assertThat(cloudConvertClient.importUsing().sftp(expectedSftpImportRequest)).isEqualTo(taskResponseResult);
        verify(requestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.TASK_RESPONSE_TYPE_REFERENCE));

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
    public void import_base64() throws Exception {
        final Base64ImportRequest expectedBase64ImportRequest = new Base64ImportRequest().setFile("some-file").setFilename("test.txt");
        final Result<TaskResponse> taskResponseResult = Result.<TaskResponse>builder().build();

        when(requestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.TASK_RESPONSE_TYPE_REFERENCE))).thenReturn(taskResponseResult);
        assertThat(cloudConvertClient.importUsing().base64(expectedBase64ImportRequest)).isEqualTo(taskResponseResult);
        verify(requestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.TASK_RESPONSE_TYPE_REFERENCE));

        final HttpUriRequest httpUriRequest = httpUriRequestArgumentCaptor.getValue();

        assertThat(httpUriRequest).isNotNull();
        assertThat(httpUriRequest.getMethod()).isEqualTo(HttpPost.METHOD_NAME);
        assertThat(httpUriRequest.getURI().toString()).isEqualTo(API_URL + "/" + AbstractResource.V2 + "/import/base64");
        assertThat(httpUriRequest).isInstanceOfSatisfying(HttpEntityEnclosingRequestBase.class, httpEntityEnclosingRequestBase -> {
            final Base64ImportRequest actualBase64ImportRequest = ThrowingSupplier.unchecked(() -> objectMapperProvider.provide()
                .readValue(httpEntityEnclosingRequestBase.getEntity().getContent(), Base64ImportRequest.class)).get();

            assertThat(actualBase64ImportRequest.getFilename()).isEqualTo(actualBase64ImportRequest.getFilename());
        });
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_AUTHORIZATION)).hasSize(1).allSatisfy(header ->
            assertThat(VALUE_AUTHORIZATION).isEqualTo(header.getValue()));
        assertThat(httpUriRequest.getHeaders(AbstractResource.HEADER_USER_AGENT)).hasSize(1).allSatisfy(header ->
            assertThat(AbstractResource.VALUE_USER_AGENT).isEqualTo(header.getValue()));
    }

    @Test
    public void import_raw() throws Exception {
        final RawImportRequest expectedRawImportRequest = new RawImportRequest().setFile("content").setFilename("test.txt");
        final Result<TaskResponse> taskResponseResult = Result.<TaskResponse>builder().build();

        when(requestExecutor.execute(any(HttpUriRequest.class), eq(AbstractResource.TASK_RESPONSE_TYPE_REFERENCE))).thenReturn(taskResponseResult);
        assertThat(cloudConvertClient.importUsing().raw(expectedRawImportRequest)).isEqualTo(taskResponseResult);
        verify(requestExecutor, times(1)).execute(httpUriRequestArgumentCaptor.capture(), eq(AbstractResource.TASK_RESPONSE_TYPE_REFERENCE));

        final HttpUriRequest httpUriRequest = httpUriRequestArgumentCaptor.getValue();

        assertThat(httpUriRequest).isNotNull();
        assertThat(httpUriRequest.getMethod()).isEqualTo(HttpPost.METHOD_NAME);
        assertThat(httpUriRequest.getURI().toString()).isEqualTo(API_URL + "/" + AbstractResource.V2 + "/import/raw");
        assertThat(httpUriRequest).isInstanceOfSatisfying(HttpEntityEnclosingRequestBase.class, httpEntityEnclosingRequestBase -> {
            final RawImportRequest actualRawImportRequest = ThrowingSupplier.unchecked(() -> objectMapperProvider.provide()
                .readValue(httpEntityEnclosingRequestBase.getEntity().getContent(), RawImportRequest.class)).get();

            assertThat(actualRawImportRequest.getFilename()).isEqualTo(actualRawImportRequest.getFilename());
        });
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
