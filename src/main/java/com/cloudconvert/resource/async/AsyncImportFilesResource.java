package com.cloudconvert.resource.async;

import com.cloudconvert.client.api.key.ApiKeyProvider;
import com.cloudconvert.client.api.url.ApiUrlProvider;
import com.cloudconvert.client.mapper.ObjectMapperProvider;
import com.cloudconvert.dto.request.AzureBlobImportRequest;
import com.cloudconvert.dto.request.GoogleCloudStorageImportRequest;
import com.cloudconvert.dto.request.OpenStackImportRequest;
import com.cloudconvert.dto.request.S3ImportRequest;
import com.cloudconvert.dto.request.SftpImportRequest;
import com.cloudconvert.dto.request.UploadImportRequest;
import com.cloudconvert.dto.request.UrlImportRequest;
import com.cloudconvert.dto.response.TaskResponse;
import com.cloudconvert.dto.response.TaskResponseData;
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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

@Slf4j
public class AsyncImportFilesResource extends AbstractImportFilesResource<AsyncResult<TaskResponseData>> {

    private final AsyncRequestExecutor asyncRequestExecutor;
    private final AsyncTasksResource asyncTasksResource;

    public AsyncImportFilesResource(
        final ApiUrlProvider apiUrlProvider, final ApiKeyProvider apiKeyProvider,
        final ObjectMapperProvider objectMapperProvider, final AsyncRequestExecutor asyncRequestExecutor,
        final AsyncTasksResource asyncTasksResource
    ) {
        super(apiUrlProvider, apiKeyProvider, objectMapperProvider);

        this.asyncRequestExecutor = asyncRequestExecutor;
        this.asyncTasksResource = asyncTasksResource;
    }

    @Override
    public AsyncResult<TaskResponseData> url(
        @NotNull final UrlImportRequest urlImportRequest
    ) throws IOException, URISyntaxException {
        final URI uri = getUri(ImmutableList.of(PATH_SEGMENT_IMPORT, PATH_SEGMENT_URL));
        final HttpEntity httpEntity = getHttpEntity(urlImportRequest);
        final HttpUriRequest httpUriRequest = getHttpUriRequest(HttpPost.class, uri, httpEntity);

        return asyncRequestExecutor.execute(httpUriRequest, TASK_RESPONSE_DATA_TYPE_REFERENCE);
    }

    @Override
    public AsyncResult<TaskResponseData> upload(
        @NotNull final UploadImportRequest uploadImportRequest
    ) throws IOException, URISyntaxException {
        final URI uri = getUri(ImmutableList.of(PATH_SEGMENT_IMPORT, PATH_SEGMENT_UPLOAD));
        final HttpEntity httpEntity = getHttpEntity(uploadImportRequest);
        final HttpUriRequest httpUriRequest = getHttpUriRequest(HttpPost.class, uri, httpEntity);

        return asyncRequestExecutor.execute(httpUriRequest, TASK_RESPONSE_DATA_TYPE_REFERENCE);
    }

    @Override
    public AsyncResult<TaskResponseData> upload(
        @NotNull final UploadImportRequest uploadImportRequest, @NotNull final InputStream inputStream
    ) throws IOException, URISyntaxException {
        return upload(upload(uploadImportRequest), inputStream);
    }

    @Override
    public AsyncResult<TaskResponseData> upload(
        @NotNull final AsyncResult<TaskResponseData> taskResponseDataAsyncResult, @NotNull final InputStream inputStream
    ) throws IOException, URISyntaxException {
        try {
            final Result<TaskResponseData> taskResponseDataResult = taskResponseDataAsyncResult.get();

            if (HttpStatus.SC_CREATED == taskResponseDataResult.getStatus()) {
                final TaskResponse taskResponse = taskResponseDataResult.getBody().get().getData();

                return upload(taskResponse.getId(), taskResponse.getResult().getForm(), inputStream);
            } else {
                return CompletedAsyncResult.<TaskResponseData>builder().result(Result.<TaskResponseData>builder()
                    .status(taskResponseDataResult.getStatus()).message(taskResponseDataResult.getMessage()).build()).build();
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new IOException(e);
        }
    }

    @Override
    public AsyncResult<TaskResponseData> upload(
        @NotNull final String taskId, @NotNull final TaskResponse.Result.Form taskResponseResultForm, @NotNull final InputStream inputStream
    ) throws IOException, URISyntaxException {
        try {
            final URI multipartUri = new URI(taskResponseResultForm.getUrl());
            final HttpEntity multipartHttpEntity = getMultipartHttpEntity(taskResponseResultForm.getParameters(), inputStream);
            final HttpUriRequest multipartHttpUriRequest = getHttpUriRequest(HttpPost.class, multipartUri, multipartHttpEntity);

            final AsyncResult<Void> multipartVoidAsyncResult = asyncRequestExecutor.execute(multipartHttpUriRequest, VOID_TYPE_REFERENCE);
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
                    return CompletedAsyncResult.<TaskResponseData>builder().result(Result.<TaskResponseData>builder()
                        .status(redirectVoidResult.getStatus()).message(redirectVoidResult.getMessage()).build()).build();
                }
            } else {
                return CompletedAsyncResult.<TaskResponseData>builder().result(Result.<TaskResponseData>builder()
                    .status(multipartVoidResult.getStatus()).message(multipartVoidResult.getMessage()).build()).build();
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new IOException(e);
        }
    }

    @Override
    public AsyncResult<TaskResponseData> s3(
        @NotNull final S3ImportRequest s3ImportRequest
    ) throws IOException, URISyntaxException {
        final URI uri = getUri(ImmutableList.of(PATH_SEGMENT_IMPORT, PATH_SEGMENT_S3));
        final HttpEntity httpEntity = getHttpEntity(s3ImportRequest);
        final HttpUriRequest httpUriRequest = getHttpUriRequest(HttpPost.class, uri, httpEntity);

        return asyncRequestExecutor.execute(httpUriRequest, TASK_RESPONSE_DATA_TYPE_REFERENCE);
    }

    @Override
    public AsyncResult<TaskResponseData> azureBlob(
        @NotNull final AzureBlobImportRequest azureBlobImportRequest
    ) throws IOException, URISyntaxException {
        final URI uri = getUri(ImmutableList.<String>builder().add(PATH_SEGMENT_IMPORT).addAll(PATH_SEGMENTS_AZURE_BLOB).build());
        final HttpEntity httpEntity = getHttpEntity(azureBlobImportRequest);
        final HttpUriRequest httpUriRequest = getHttpUriRequest(HttpPost.class, uri, httpEntity);

        return asyncRequestExecutor.execute(httpUriRequest, TASK_RESPONSE_DATA_TYPE_REFERENCE);
    }

    @Override
    public AsyncResult<TaskResponseData> googleCloudStorage(
        @NotNull final GoogleCloudStorageImportRequest googleCloudStorageImportRequest
    ) throws IOException, URISyntaxException {
        final URI uri = getUri(ImmutableList.of(PATH_SEGMENT_IMPORT, PATH_SEGMENT_GOOGLE_CLOUD_STORAGE));
        final HttpEntity httpEntity = getHttpEntity(googleCloudStorageImportRequest);
        final HttpUriRequest httpUriRequest = getHttpUriRequest(HttpPost.class, uri, httpEntity);

        return asyncRequestExecutor.execute(httpUriRequest, TASK_RESPONSE_DATA_TYPE_REFERENCE);
    }

    @Override
    public AsyncResult<TaskResponseData> openStack(
        @NotNull final OpenStackImportRequest openStackImportRequest
    ) throws IOException, URISyntaxException {
        final URI uri = getUri(ImmutableList.of(PATH_SEGMENT_IMPORT, PATH_SEGMENT_OPENSTACK));
        final HttpEntity httpEntity = getHttpEntity(openStackImportRequest);
        final HttpUriRequest httpUriRequest = getHttpUriRequest(HttpPost.class, uri, httpEntity);

        return asyncRequestExecutor.execute(httpUriRequest, TASK_RESPONSE_DATA_TYPE_REFERENCE);
    }

    @Override
    public AsyncResult<TaskResponseData> sftp(
        @NotNull final SftpImportRequest sftpImportRequest
    ) throws IOException, URISyntaxException {
        final URI uri = getUri(ImmutableList.of(PATH_SEGMENT_IMPORT, PATH_SEGMENT_SFTP));
        final HttpEntity httpEntity = getHttpEntity(sftpImportRequest);
        final HttpUriRequest httpUriRequest = getHttpUriRequest(HttpPost.class, uri, httpEntity);

        return asyncRequestExecutor.execute(httpUriRequest, TASK_RESPONSE_DATA_TYPE_REFERENCE);
    }

    @Override
    public void close() throws IOException {
        asyncRequestExecutor.close();
        asyncTasksResource.close();
    }
}
