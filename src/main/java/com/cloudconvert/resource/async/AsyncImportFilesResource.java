package com.cloudconvert.resource.async;

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
import com.cloudconvert.dto.result.AsyncResult;
import com.cloudconvert.dto.result.CompletedAsyncResult;
import com.cloudconvert.dto.result.Result;
import com.cloudconvert.exception.CloudConvertClientException;
import com.cloudconvert.exception.CloudConvertServerException;
import com.cloudconvert.executor.AsyncRequestExecutor;
import com.cloudconvert.resource.AbstractImportFilesResource;
import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

@Slf4j
public class AsyncImportFilesResource extends AbstractImportFilesResource<AsyncResult<TaskResponse>> {

    private final AsyncRequestExecutor asyncRequestExecutor;
    private final AsyncTasksResource asyncTasksResource;

    public AsyncImportFilesResource(
        final SettingsProvider settingsProvider,
        final ObjectMapperProvider objectMapperProvider, final AsyncRequestExecutor asyncRequestExecutor,
        final AsyncTasksResource asyncTasksResource
    ) {
        super(settingsProvider, objectMapperProvider);

        this.asyncRequestExecutor = asyncRequestExecutor;
        this.asyncTasksResource = asyncTasksResource;
    }

    @Override
    public AsyncResult<TaskResponse> url(
        @NotNull final UrlImportRequest urlImportRequest
    ) throws IOException, URISyntaxException {
        return asyncRequestExecutor.execute(getUrlHttpUriRequest(urlImportRequest), TASK_RESPONSE_TYPE_REFERENCE);
    }

    @Override
    public AsyncResult<TaskResponse> upload(
        @NotNull final UploadImportRequest uploadImportRequest
    ) throws IOException, URISyntaxException {
        final URI uri = getUri(ImmutableList.of(PATH_SEGMENT_IMPORT, PATH_SEGMENT_UPLOAD));
        final HttpEntity httpEntity = getHttpEntity(uploadImportRequest);
        final HttpUriRequest httpUriRequest = getHttpUriRequest(HttpPost.class, uri, httpEntity);

        return asyncRequestExecutor.execute(httpUriRequest, TASK_RESPONSE_TYPE_REFERENCE);
    }

    @Override
    public AsyncResult<TaskResponse> upload(
        @NotNull final UploadImportRequest uploadImportRequest, @NotNull final File file
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        return upload(upload(uploadImportRequest), file);
    }

    @Override
    public AsyncResult<TaskResponse> upload(
        @NotNull final AsyncResult<TaskResponse> taskResponseAsyncResult, @NotNull final File file
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        try {
            final Result<TaskResponse> taskResponseResult = taskResponseAsyncResult.get();

            if (HttpStatus.SC_CREATED == taskResponseResult.getStatus().getCode()) {
                final TaskResponse taskResponse = taskResponseResult.getBody();

                return upload(taskResponse.getId(), taskResponse.getResult().getForm(), file);
            } else {
                return CompletedAsyncResult.<TaskResponse>builder().result(taskResponseResult).build();
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new IOException(e);
        }
    }

    @Override
    public AsyncResult<TaskResponse> upload(
        @NotNull final String taskId, @NotNull final TaskResponse.Result.Form taskResponseResultForm, @NotNull final File file
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        try {
            final URI multipartUri = new URI(taskResponseResultForm.getUrl());
            final HttpEntity multipartHttpEntity = getMultipartHttpEntity(taskResponseResultForm, file);
            final HttpUriRequest multipartHttpUriRequest = getHttpUriRequest(HttpPost.class, multipartUri, multipartHttpEntity);
            multipartHttpUriRequest.removeHeaders(HEADER_AUTHORIZATION);
            return uploadPostProcess(taskId, asyncRequestExecutor.execute(multipartHttpUriRequest, VOID_TYPE_REFERENCE));
        } catch (InterruptedException | ExecutionException e) {
            throw new IOException(e);
        }
    }

    @Override
    public AsyncResult<TaskResponse> upload(
        @NotNull final UploadImportRequest uploadImportRequest, @NotNull final InputStream inputStream
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        return upload(upload(uploadImportRequest), inputStream);
    }

    @Override
    public AsyncResult<TaskResponse> upload(
        @NotNull UploadImportRequest uploadImportRequest, @NotNull InputStream inputStream, @NotNull String filename
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        return upload(upload(uploadImportRequest), inputStream, filename);
    }

    @Override
    public AsyncResult<TaskResponse> upload(
        @NotNull final AsyncResult<TaskResponse> taskResponseAsyncResult, @NotNull final InputStream inputStream
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        try {
            final Result<TaskResponse> taskResponseResult = taskResponseAsyncResult.get();

            if (HttpStatus.SC_CREATED == taskResponseResult.getStatus().getCode()) {
                final TaskResponse taskResponse = taskResponseResult.getBody();

                return upload(taskResponse.getId(), taskResponse.getResult().getForm(), inputStream);
            } else {
                return CompletedAsyncResult.<TaskResponse>builder().result(taskResponseResult).build();
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new IOException(e);
        }
    }

    @Override
    public AsyncResult<TaskResponse> upload(
        @NotNull AsyncResult<TaskResponse> taskResponseAsyncResult, @NotNull InputStream inputStream, @NotNull String filename
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        try {
            final Result<TaskResponse> taskResponseResult = taskResponseAsyncResult.get();

            if (HttpStatus.SC_CREATED == taskResponseResult.getStatus().getCode()) {
                final TaskResponse taskResponse = taskResponseResult.getBody();

                return upload(taskResponse.getId(), taskResponse.getResult().getForm(), inputStream, filename);
            } else {
                return CompletedAsyncResult.<TaskResponse>builder().result(taskResponseResult).build();
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new IOException(e);
        }
    }

    @Override
    public AsyncResult<TaskResponse> upload(
        @NotNull final String taskId, @NotNull final TaskResponse.Result.Form taskResponseResultForm, @NotNull final InputStream inputStream
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        try {
            final URI multipartUri = new URI(taskResponseResultForm.getUrl());
            final HttpEntity multipartHttpEntity = getMultipartHttpEntity(taskResponseResultForm, inputStream);
            final HttpUriRequest multipartHttpUriRequest = getHttpUriRequest(HttpPost.class, multipartUri, multipartHttpEntity);
            multipartHttpUriRequest.removeHeaders(HEADER_AUTHORIZATION);
            return uploadPostProcess(taskId, asyncRequestExecutor.execute(multipartHttpUriRequest, VOID_TYPE_REFERENCE));
        } catch (InterruptedException | ExecutionException e) {
            throw new IOException(e);
        }
    }

    @Override
    public AsyncResult<TaskResponse> upload(
        @NotNull String taskId, TaskResponse.Result.@NotNull Form taskResponseResultForm, @NotNull InputStream inputStream, @NotNull String filename
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        try {
            final URI multipartUri = new URI(taskResponseResultForm.getUrl());
            final HttpEntity multipartHttpEntity = getMultipartHttpEntity(taskResponseResultForm, filename, inputStream);
            final HttpUriRequest multipartHttpUriRequest = getHttpUriRequest(HttpPost.class, multipartUri, multipartHttpEntity);
            multipartHttpUriRequest.removeHeaders(HEADER_AUTHORIZATION);
            return uploadPostProcess(taskId, asyncRequestExecutor.execute(multipartHttpUriRequest, VOID_TYPE_REFERENCE));
        } catch (InterruptedException | ExecutionException e) {
            throw new IOException(e);
        }
    }

    private AsyncResult<TaskResponse> uploadPostProcess(
        final String taskId, final AsyncResult<Void> multipartVoidAsyncResult
    ) throws IOException, URISyntaxException, InterruptedException, ExecutionException, CloudConvertClientException, CloudConvertServerException {
        final Result<Void> multipartVoidResult = multipartVoidAsyncResult.get();

        if (HttpStatus.SC_CREATED == multipartVoidResult.getStatus().getCode()) {
            return asyncTasksResource.show(taskId);
        } else if (HttpStatus.SC_SEE_OTHER == multipartVoidResult.getStatus().getCode()) {
            final URI redirectUri = new URI(multipartVoidResult.getHeaders().get("Location"));
            final HttpUriRequest redirectHttpUriRequest = getHttpUriRequest(HttpGet.class, redirectUri);

            final AsyncResult<Void> redirectVoidAsyncResult = asyncRequestExecutor.execute(redirectHttpUriRequest, VOID_TYPE_REFERENCE);
            final Result<Void> redirectVoidResult = redirectVoidAsyncResult.get();

            if (HttpStatus.SC_CREATED == redirectVoidResult.getStatus().getCode()) {
                return asyncTasksResource.show(taskId);
            } else {
                return CompletedAsyncResult.<TaskResponse>builder()
                    .result(Result.<TaskResponse>builder().status(redirectVoidResult.getStatus()).headers(redirectVoidResult.getHeaders()).build()).build();
            }
        } else {
            return CompletedAsyncResult.<TaskResponse>builder()
                .result(Result.<TaskResponse>builder().status(multipartVoidResult.getStatus()).headers(multipartVoidResult.getHeaders()).build()).build();
        }
    }

    @Override
    public AsyncResult<TaskResponse> s3(
        @NotNull final S3ImportRequest s3ImportRequest
    ) throws IOException, URISyntaxException {
        return asyncRequestExecutor.execute(getS3HttpUriRequest(s3ImportRequest), TASK_RESPONSE_TYPE_REFERENCE);
    }

    @Override
    public AsyncResult<TaskResponse> azureBlob(
        @NotNull final AzureBlobImportRequest azureBlobImportRequest
    ) throws IOException, URISyntaxException {
        return asyncRequestExecutor.execute(getAzureBlobHttpUriRequest(azureBlobImportRequest), TASK_RESPONSE_TYPE_REFERENCE);
    }

    @Override
    public AsyncResult<TaskResponse> googleCloudStorage(
        @NotNull final GoogleCloudStorageImportRequest googleCloudStorageImportRequest
    ) throws IOException, URISyntaxException {
        return asyncRequestExecutor.execute(getGoogleCloudHttpUriRequest(googleCloudStorageImportRequest), TASK_RESPONSE_TYPE_REFERENCE);
    }

    @Override
    public AsyncResult<TaskResponse> openStack(
        @NotNull final OpenStackImportRequest openStackImportRequest
    ) throws IOException, URISyntaxException {
        return asyncRequestExecutor.execute(getOpenStackHttpUriRequest(openStackImportRequest), TASK_RESPONSE_TYPE_REFERENCE);
    }

    @Override
    public AsyncResult<TaskResponse> sftp(
        @NotNull final SftpImportRequest sftpImportRequest
    ) throws IOException, URISyntaxException {
        return asyncRequestExecutor.execute(getSftpHttpUriRequest(sftpImportRequest), TASK_RESPONSE_TYPE_REFERENCE);
    }

    @Override
    public AsyncResult<TaskResponse> base64(@NotNull final Base64ImportRequest base64ImportRequest) throws IOException, URISyntaxException {
        return asyncRequestExecutor.execute(getBase64HttpUriRequest(base64ImportRequest), TASK_RESPONSE_TYPE_REFERENCE);
    }

    @Override
    public AsyncResult<TaskResponse> raw(@NotNull final RawImportRequest rawImportRequest) throws IOException, URISyntaxException {
        return asyncRequestExecutor.execute(getRawHttpUriRequest(rawImportRequest), TASK_RESPONSE_TYPE_REFERENCE);
    }

    @Override
    public void close() throws IOException {
        asyncRequestExecutor.close();
        asyncTasksResource.close();
    }
}
