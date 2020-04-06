package com.cloudconvert.resource.sync;

import com.cloudconvert.client.mapper.ObjectMapperProvider;
import com.cloudconvert.client.setttings.SettingsProvider;
import com.cloudconvert.dto.request.*;
import com.cloudconvert.dto.response.TaskResponse;
import com.cloudconvert.dto.response.TaskResponseData;
import com.cloudconvert.dto.result.Result;
import com.cloudconvert.executor.RequestExecutor;
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

@Slf4j
public class ImportFilesResource extends AbstractImportFilesResource<Result<TaskResponseData>> {

    private final RequestExecutor requestExecutor;
    private final TasksResource tasksResource;

    public ImportFilesResource(
        final SettingsProvider settingsProvider,
        final ObjectMapperProvider objectMapperProvider, final RequestExecutor requestExecutor,
        final TasksResource tasksResource
    ) {
        super(settingsProvider, objectMapperProvider);

        this.requestExecutor = requestExecutor;
        this.tasksResource = tasksResource;
    }

    @Override
    public Result<TaskResponseData> url(
        @NotNull final UrlImportRequest urlImportRequest
    ) throws IOException, URISyntaxException {
        final URI uri = getUri(ImmutableList.of(PATH_SEGMENT_IMPORT, PATH_SEGMENT_URL));
        final HttpEntity httpEntity = getHttpEntity(urlImportRequest);
        final HttpUriRequest httpUriRequest = getHttpUriRequest(HttpPost.class, uri, httpEntity);

        return requestExecutor.execute(httpUriRequest, TASK_RESPONSE_DATA_TYPE_REFERENCE);
    }

    @Override
    public Result<TaskResponseData> upload(
        @NotNull final UploadImportRequest uploadImportRequest
    ) throws IOException, URISyntaxException {
        final URI uri = getUri(ImmutableList.of(PATH_SEGMENT_IMPORT, PATH_SEGMENT_UPLOAD));
        final HttpEntity httpEntity = getHttpEntity(uploadImportRequest);
        final HttpUriRequest httpUriRequest = getHttpUriRequest(HttpPost.class, uri, httpEntity);

        return requestExecutor.execute(httpUriRequest, TASK_RESPONSE_DATA_TYPE_REFERENCE);
    }

    @Override
    public Result<TaskResponseData> upload(
        @NotNull final UploadImportRequest uploadImportRequest, @NotNull final File file
    ) throws IOException, URISyntaxException {
        return upload(upload(uploadImportRequest), file);
    }

    @Override
    public Result<TaskResponseData> upload(
        @NotNull final Result<TaskResponseData> taskResponseDataResult, @NotNull final File file
    ) throws IOException, URISyntaxException {
        if (HttpStatus.SC_CREATED == taskResponseDataResult.getStatus()) {
            final TaskResponse taskResponse = taskResponseDataResult.getBody().get().getData();

            return upload(taskResponse.getId(), taskResponse.getResult().getForm(), file);
        } else {
            return Result.<TaskResponseData>builder().status(taskResponseDataResult.getStatus()).message(taskResponseDataResult.getMessage()).build();
        }
    }

    @Override
    public Result<TaskResponseData> upload(
        @NotNull final String taskId, @NotNull final TaskResponse.Result.Form taskResponseResultForm, @NotNull final File file
    ) throws IOException, URISyntaxException {

        final URI multipartUri = new URI(taskResponseResultForm.getUrl());
        final HttpEntity multipartHttpEntity = getMultipartHttpEntity(taskResponseResultForm, file);
        final HttpUriRequest multipartHttpUriRequest = getHttpUriRequest(HttpPost.class, multipartUri, multipartHttpEntity);

        return uploadPostProcess(taskId, requestExecutor.execute(multipartHttpUriRequest, VOID_TYPE_REFERENCE));
    }

    @Override
    public Result<TaskResponseData> upload(
        @NotNull final UploadImportRequest uploadImportRequest, @NotNull final InputStream inputStream
    ) throws IOException, URISyntaxException {
        return upload(upload(uploadImportRequest), inputStream);
    }

    @Override
    public Result<TaskResponseData> upload(
        @NotNull final Result<TaskResponseData> taskResponseDataResult, @NotNull final InputStream inputStream
    ) throws IOException, URISyntaxException {
        if (HttpStatus.SC_CREATED == taskResponseDataResult.getStatus()) {
            final TaskResponse taskResponse = taskResponseDataResult.getBody().get().getData();

            return upload(taskResponse.getId(), taskResponse.getResult().getForm(), inputStream);
        } else {
            return Result.<TaskResponseData>builder().status(taskResponseDataResult.getStatus()).message(taskResponseDataResult.getMessage()).build();
        }
    }

    @Override
    public Result<TaskResponseData> upload(
        @NotNull final String taskId, @NotNull final TaskResponse.Result.Form taskResponseResultForm, @NotNull final InputStream inputStream
    ) throws IOException, URISyntaxException {
        final URI multipartUri = new URI(taskResponseResultForm.getUrl());
        final HttpEntity multipartHttpEntity = getMultipartHttpEntity(taskResponseResultForm, inputStream);
        final HttpUriRequest multipartHttpUriRequest = getHttpUriRequest(HttpPost.class, multipartUri, multipartHttpEntity);

        return uploadPostProcess(taskId, requestExecutor.execute(multipartHttpUriRequest, VOID_TYPE_REFERENCE));
    }

    private Result<TaskResponseData> uploadPostProcess(
        final String taskId, final Result<Void> multipartVoidResult
    ) throws IOException, URISyntaxException {
        if (HttpStatus.SC_CREATED == multipartVoidResult.getStatus()) {
            return tasksResource.show(taskId);
        } else if (HttpStatus.SC_SEE_OTHER == multipartVoidResult.getStatus()) {
            final URI redirectUri = new URI(multipartVoidResult.getHeaders().get("Location"));
            final HttpUriRequest redirectHttpUriRequest = getHttpUriRequest(HttpGet.class, redirectUri);

            final Result<Void> redirectVoidResult = requestExecutor.execute(redirectHttpUriRequest, VOID_TYPE_REFERENCE);

            if (HttpStatus.SC_CREATED == redirectVoidResult.getStatus()) {
                return tasksResource.show(taskId);
            } else {
                return Result.<TaskResponseData>builder().status(redirectVoidResult.getStatus()).message(redirectVoidResult.getMessage()).build();
            }
        } else {
            return Result.<TaskResponseData>builder().status(multipartVoidResult.getStatus()).message(multipartVoidResult.getMessage()).build();
        }
    }

    @Override
    public Result<TaskResponseData> s3(
        @NotNull final S3ImportRequest s3ImportRequest
    ) throws IOException, URISyntaxException {
        final URI uri = getUri(ImmutableList.of(PATH_SEGMENT_IMPORT, PATH_SEGMENT_S3));
        final HttpEntity httpEntity = getHttpEntity(s3ImportRequest);
        final HttpUriRequest httpUriRequest = getHttpUriRequest(HttpPost.class, uri, httpEntity);

        return requestExecutor.execute(httpUriRequest, TASK_RESPONSE_DATA_TYPE_REFERENCE);
    }

    @Override
    public Result<TaskResponseData> azureBlob(
        @NotNull final AzureBlobImportRequest azureBlobImportRequest
    ) throws IOException, URISyntaxException {
        final URI uri = getUri(ImmutableList.<String>builder().add(PATH_SEGMENT_IMPORT).addAll(PATH_SEGMENTS_AZURE_BLOB).build());
        final HttpEntity httpEntity = getHttpEntity(azureBlobImportRequest);
        final HttpUriRequest httpUriRequest = getHttpUriRequest(HttpPost.class, uri, httpEntity);

        return requestExecutor.execute(httpUriRequest, TASK_RESPONSE_DATA_TYPE_REFERENCE);
    }

    @Override
    public Result<TaskResponseData> googleCloudStorage(
        @NotNull final GoogleCloudStorageImportRequest googleCloudStorageImportRequest
    ) throws IOException, URISyntaxException {
        final URI uri = getUri(ImmutableList.of(PATH_SEGMENT_IMPORT, PATH_SEGMENT_GOOGLE_CLOUD_STORAGE));
        final HttpEntity httpEntity = getHttpEntity(googleCloudStorageImportRequest);
        final HttpUriRequest httpUriRequest = getHttpUriRequest(HttpPost.class, uri, httpEntity);

        return requestExecutor.execute(httpUriRequest, TASK_RESPONSE_DATA_TYPE_REFERENCE);
    }

    @Override
    public Result<TaskResponseData> openStack(
        @NotNull final OpenStackImportRequest openStackImportRequest
    ) throws IOException, URISyntaxException {
        final URI uri = getUri(ImmutableList.of(PATH_SEGMENT_IMPORT, PATH_SEGMENT_OPENSTACK));
        final HttpEntity httpEntity = getHttpEntity(openStackImportRequest);
        final HttpUriRequest httpUriRequest = getHttpUriRequest(HttpPost.class, uri, httpEntity);

        return requestExecutor.execute(httpUriRequest, TASK_RESPONSE_DATA_TYPE_REFERENCE);
    }

    @Override
    public Result<TaskResponseData> sftp(
        @NotNull final SftpImportRequest sftpImportRequest
    ) throws IOException, URISyntaxException {
        final URI uri = getUri(ImmutableList.of(PATH_SEGMENT_IMPORT, PATH_SEGMENT_SFTP));
        final HttpEntity httpEntity = getHttpEntity(sftpImportRequest);
        final HttpUriRequest httpUriRequest = getHttpUriRequest(HttpPost.class, uri, httpEntity);

        return requestExecutor.execute(httpUriRequest, TASK_RESPONSE_DATA_TYPE_REFERENCE);
    }

    @Override
    public void close() throws IOException {
        requestExecutor.close();
        tasksResource.close();
    }
}
