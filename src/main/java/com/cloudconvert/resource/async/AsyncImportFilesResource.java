package com.cloudconvert.resource.async;

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
import com.cloudconvert.dto.result.Result;
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
        final URI uri = getUri(ImmutableList.of(PATH_SEGMENT_IMPORT, PATH_SEGMENT_URL));
        final HttpEntity httpEntity = getHttpEntity(urlImportRequest);
        final HttpUriRequest httpUriRequest = getHttpUriRequest(HttpPost.class, uri, httpEntity);

        return asyncRequestExecutor.execute(httpUriRequest, TASK_RESPONSE_TYPE_REFERENCE);
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
    ) throws IOException, URISyntaxException {
        return upload(upload(uploadImportRequest), file);
    }

    @Override
    public AsyncResult<TaskResponse> upload(
        @NotNull final AsyncResult<TaskResponse> TaskResponseAsyncResult, @NotNull final File file
    ) throws IOException, URISyntaxException {
        try {
            final Result<TaskResponse> TaskResponseResult = TaskResponseAsyncResult.get();

            if (HttpStatus.SC_CREATED == TaskResponseResult.getStatus()) {
                final TaskResponse taskResponse = TaskResponseResult.getBody();

                return upload(taskResponse.getId(), taskResponse.getResult().getForm(), file);
            } else {
                return CompletedAsyncResult.<TaskResponse>builder().result(Result.<TaskResponse>builder()
                    .status(TaskResponseResult.getStatus()).message(TaskResponseResult.getMessage()).build()).build();
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new IOException(e);
        }
    }

    @Override
    public AsyncResult<TaskResponse> upload(
        @NotNull final String taskId, @NotNull final TaskResponse.Result.Form taskResponseResultForm, @NotNull final File file
    ) throws IOException, URISyntaxException {
        try {
            final URI multipartUri = new URI(taskResponseResultForm.getUrl());
            final HttpEntity multipartHttpEntity = getMultipartHttpEntity(taskResponseResultForm, file);
            final HttpUriRequest multipartHttpUriRequest = getHttpUriRequest(HttpPost.class, multipartUri, multipartHttpEntity);

            return uploadPostProcess(taskId, asyncRequestExecutor.execute(multipartHttpUriRequest, VOID_TYPE_REFERENCE));
        } catch (InterruptedException | ExecutionException e) {
            throw new IOException(e);
        }
    }

    @Override
    public AsyncResult<TaskResponse> upload(
        @NotNull final UploadImportRequest uploadImportRequest, @NotNull final InputStream inputStream
    ) throws IOException, URISyntaxException {
        return upload(upload(uploadImportRequest), inputStream);
    }

    @Override
    public AsyncResult<TaskResponse> upload(
        @NotNull final AsyncResult<TaskResponse> TaskResponseAsyncResult, @NotNull final InputStream inputStream
    ) throws IOException, URISyntaxException {
        try {
            final Result<TaskResponse> TaskResponseResult = TaskResponseAsyncResult.get();

            if (HttpStatus.SC_CREATED == TaskResponseResult.getStatus()) {
                final TaskResponse taskResponse = TaskResponseResult.getBody();

                return upload(taskResponse.getId(), taskResponse.getResult().getForm(), inputStream);
            } else {
                return CompletedAsyncResult.<TaskResponse>builder().result(Result.<TaskResponse>builder()
                    .status(TaskResponseResult.getStatus()).message(TaskResponseResult.getMessage()).build()).build();
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new IOException(e);
        }
    }

    @Override
    public AsyncResult<TaskResponse> upload(
        @NotNull final String taskId, @NotNull final TaskResponse.Result.Form taskResponseResultForm, @NotNull final InputStream inputStream
    ) throws IOException, URISyntaxException {
        try {
            final URI multipartUri = new URI(taskResponseResultForm.getUrl());
            final HttpEntity multipartHttpEntity = getMultipartHttpEntity(taskResponseResultForm, inputStream);
            final HttpUriRequest multipartHttpUriRequest = getHttpUriRequest(HttpPost.class, multipartUri, multipartHttpEntity);

            return uploadPostProcess(taskId, asyncRequestExecutor.execute(multipartHttpUriRequest, VOID_TYPE_REFERENCE));
        } catch (InterruptedException | ExecutionException e) {
            throw new IOException(e);
        }
    }

    private AsyncResult<TaskResponse> uploadPostProcess(
        final String taskId, final AsyncResult<Void> multipartVoidAsyncResult
    ) throws IOException, URISyntaxException, InterruptedException, ExecutionException {
        final Result<Void> multipartVoidResult = multipartVoidAsyncResult.get();

        if (HttpStatus.SC_CREATED == multipartVoidResult.getStatus()) {
            return asyncTasksResource.show(taskId);
        } else if (HttpStatus.SC_SEE_OTHER == multipartVoidResult.getStatus()) {
            final URI redirectUri = new URI(multipartVoidResult.getHeaders().get("Location"));
            final HttpUriRequest redirectHttpUriRequest = getHttpUriRequest(HttpGet.class, redirectUri);

            final AsyncResult<Void> redirectVoidAsyncResult = asyncRequestExecutor.execute(redirectHttpUriRequest, VOID_TYPE_REFERENCE);
            final Result<Void> redirectVoidResult = redirectVoidAsyncResult.get();

            if (HttpStatus.SC_CREATED == redirectVoidResult.getStatus()) {
                return asyncTasksResource.show(taskId);
            } else {
                return CompletedAsyncResult.<TaskResponse>builder().result(Result.<TaskResponse>builder()
                    .status(redirectVoidResult.getStatus()).message(redirectVoidResult.getMessage()).build()).build();
            }
        } else {
            return CompletedAsyncResult.<TaskResponse>builder().result(Result.<TaskResponse>builder()
                .status(multipartVoidResult.getStatus()).message(multipartVoidResult.getMessage()).build()).build();
        }
    }

    @Override
    public AsyncResult<TaskResponse> s3(
        @NotNull final S3ImportRequest s3ImportRequest
    ) throws IOException, URISyntaxException {
        final URI uri = getUri(ImmutableList.of(PATH_SEGMENT_IMPORT, PATH_SEGMENT_S3));
        final HttpEntity httpEntity = getHttpEntity(s3ImportRequest);
        final HttpUriRequest httpUriRequest = getHttpUriRequest(HttpPost.class, uri, httpEntity);

        return asyncRequestExecutor.execute(httpUriRequest, TASK_RESPONSE_TYPE_REFERENCE);
    }

    @Override
    public AsyncResult<TaskResponse> azureBlob(
        @NotNull final AzureBlobImportRequest azureBlobImportRequest
    ) throws IOException, URISyntaxException {
        final URI uri = getUri(ImmutableList.<String>builder().add(PATH_SEGMENT_IMPORT).addAll(PATH_SEGMENTS_AZURE_BLOB).build());
        final HttpEntity httpEntity = getHttpEntity(azureBlobImportRequest);
        final HttpUriRequest httpUriRequest = getHttpUriRequest(HttpPost.class, uri, httpEntity);

        return asyncRequestExecutor.execute(httpUriRequest, TASK_RESPONSE_TYPE_REFERENCE);
    }

    @Override
    public AsyncResult<TaskResponse> googleCloudStorage(
        @NotNull final GoogleCloudStorageImportRequest googleCloudStorageImportRequest
    ) throws IOException, URISyntaxException {
        final URI uri = getUri(ImmutableList.of(PATH_SEGMENT_IMPORT, PATH_SEGMENT_GOOGLE_CLOUD_STORAGE));
        final HttpEntity httpEntity = getHttpEntity(googleCloudStorageImportRequest);
        final HttpUriRequest httpUriRequest = getHttpUriRequest(HttpPost.class, uri, httpEntity);

        return asyncRequestExecutor.execute(httpUriRequest, TASK_RESPONSE_TYPE_REFERENCE);
    }

    @Override
    public AsyncResult<TaskResponse> openStack(
        @NotNull final OpenStackImportRequest openStackImportRequest
    ) throws IOException, URISyntaxException {
        final URI uri = getUri(ImmutableList.of(PATH_SEGMENT_IMPORT, PATH_SEGMENT_OPENSTACK));
        final HttpEntity httpEntity = getHttpEntity(openStackImportRequest);
        final HttpUriRequest httpUriRequest = getHttpUriRequest(HttpPost.class, uri, httpEntity);

        return asyncRequestExecutor.execute(httpUriRequest, TASK_RESPONSE_TYPE_REFERENCE);
    }

    @Override
    public AsyncResult<TaskResponse> sftp(
        @NotNull final SftpImportRequest sftpImportRequest
    ) throws IOException, URISyntaxException {
        final URI uri = getUri(ImmutableList.of(PATH_SEGMENT_IMPORT, PATH_SEGMENT_SFTP));
        final HttpEntity httpEntity = getHttpEntity(sftpImportRequest);
        final HttpUriRequest httpUriRequest = getHttpUriRequest(HttpPost.class, uri, httpEntity);

        return asyncRequestExecutor.execute(httpUriRequest, TASK_RESPONSE_TYPE_REFERENCE);
    }

    @Override
    public void close() throws IOException {
        asyncRequestExecutor.close();
        asyncTasksResource.close();
    }
}
