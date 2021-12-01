package com.cloudconvert.resource.sync;

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
import com.cloudconvert.exception.CloudConvertClientException;
import com.cloudconvert.exception.CloudConvertServerException;
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
public class ImportFilesResource extends AbstractImportFilesResource<Result<TaskResponse>> {

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
    public Result<TaskResponse> url(
        @NotNull final UrlImportRequest urlImportRequest
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        return requestExecutor.execute(getUrlHttpUriRequest(urlImportRequest), TASK_RESPONSE_TYPE_REFERENCE);
    }

    @Override
    public Result<TaskResponse> upload(
        @NotNull final UploadImportRequest uploadImportRequest
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        final URI uri = getUri(ImmutableList.of(PATH_SEGMENT_IMPORT, PATH_SEGMENT_UPLOAD));
        final HttpEntity httpEntity = getHttpEntity(uploadImportRequest);
        final HttpUriRequest httpUriRequest = getHttpUriRequest(HttpPost.class, uri, httpEntity);

        return requestExecutor.execute(httpUriRequest, TASK_RESPONSE_TYPE_REFERENCE);
    }

    @Override
    public Result<TaskResponse> upload(
        @NotNull final UploadImportRequest uploadImportRequest, @NotNull final File file
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        return upload(upload(uploadImportRequest), file);
    }

    @Override
    public Result<TaskResponse> upload(
        @NotNull final Result<TaskResponse> taskResponseResult, @NotNull final File file
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        if (HttpStatus.SC_CREATED == taskResponseResult.getStatus().getCode()) {
            final TaskResponse taskResponse = taskResponseResult.getBody();

            return upload(taskResponse.getId(), taskResponse.getResult().getForm(), file);
        } else {
            return taskResponseResult;
        }
    }

    @Override
    public Result<TaskResponse> upload(
        @NotNull final String taskId, @NotNull final TaskResponse.Result.Form taskResponseResultForm, @NotNull final File file
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        final URI multipartUri = new URI(taskResponseResultForm.getUrl());
        final HttpEntity multipartHttpEntity = getMultipartHttpEntity(taskResponseResultForm, file);
        final HttpUriRequest multipartHttpUriRequest = getHttpUriRequest(HttpPost.class, multipartUri, multipartHttpEntity);
        multipartHttpUriRequest.removeHeaders(HEADER_AUTHORIZATION);
        return uploadPostProcess(taskId, requestExecutor.execute(multipartHttpUriRequest, VOID_TYPE_REFERENCE));
    }

    @Override
    public Result<TaskResponse> upload(
        @NotNull UploadImportRequest uploadImportRequest, @NotNull InputStream inputStream
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        return upload(upload(uploadImportRequest), inputStream);
    }

    @Override
    public Result<TaskResponse> upload(
        @NotNull final UploadImportRequest uploadImportRequest, @NotNull final InputStream inputStream, @NotNull final String filename
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        return upload(upload(uploadImportRequest), inputStream, filename);
    }

    @Override
    public Result<TaskResponse> upload(
        @NotNull Result<TaskResponse> taskResponseResult, @NotNull InputStream inputStream
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        if (HttpStatus.SC_CREATED == taskResponseResult.getStatus().getCode()) {
            final TaskResponse taskResponse = taskResponseResult.getBody();

            return upload(taskResponse.getId(), taskResponse.getResult().getForm(), inputStream);
        } else {
            return taskResponseResult;
        }
    }

    @Override
    public Result<TaskResponse> upload(
        @NotNull final Result<TaskResponse> taskResponseResult, @NotNull final InputStream inputStream, @NotNull final String filename
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        if (HttpStatus.SC_CREATED == taskResponseResult.getStatus().getCode()) {
            final TaskResponse taskResponse = taskResponseResult.getBody();

            return upload(taskResponse.getId(), taskResponse.getResult().getForm(), inputStream, filename);
        } else {
            return taskResponseResult;
        }
    }

    @Override
    public Result<TaskResponse> upload(
        @NotNull String taskId, TaskResponse.Result.@NotNull Form taskResponseResultForm, @NotNull InputStream inputStream
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        final URI multipartUri = new URI(taskResponseResultForm.getUrl());
        final HttpEntity multipartHttpEntity = getMultipartHttpEntity(taskResponseResultForm, inputStream);
        final HttpUriRequest multipartHttpUriRequest = getHttpUriRequest(HttpPost.class, multipartUri, multipartHttpEntity);
        multipartHttpUriRequest.removeHeaders(HEADER_AUTHORIZATION);
        return uploadPostProcess(taskId, requestExecutor.execute(multipartHttpUriRequest, VOID_TYPE_REFERENCE));
    }

    @Override
    public Result<TaskResponse> upload(
        @NotNull final String taskId, @NotNull final TaskResponse.Result.Form taskResponseResultForm, @NotNull final InputStream inputStream, @NotNull final String filename
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        final URI multipartUri = new URI(taskResponseResultForm.getUrl());
        final HttpEntity multipartHttpEntity = getMultipartHttpEntity(taskResponseResultForm, filename, inputStream);
        final HttpUriRequest multipartHttpUriRequest = getHttpUriRequest(HttpPost.class, multipartUri, multipartHttpEntity);
        multipartHttpUriRequest.removeHeaders(HEADER_AUTHORIZATION);
        return uploadPostProcess(taskId, requestExecutor.execute(multipartHttpUriRequest, VOID_TYPE_REFERENCE));
    }

    private Result<TaskResponse> uploadPostProcess(
        final String taskId, final Result<Void> multipartVoidResult
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        if (HttpStatus.SC_CREATED == multipartVoidResult.getStatus().getCode()) {
            return tasksResource.show(taskId);
        } else if (HttpStatus.SC_SEE_OTHER == multipartVoidResult.getStatus().getCode()) {
            final URI redirectUri = new URI(multipartVoidResult.getHeaders().get("Location"));
            final HttpUriRequest redirectHttpUriRequest = getHttpUriRequest(HttpGet.class, redirectUri);

            final Result<Void> redirectVoidResult = requestExecutor.execute(redirectHttpUriRequest, VOID_TYPE_REFERENCE);

            if (HttpStatus.SC_CREATED == redirectVoidResult.getStatus().getCode()) {
                return tasksResource.show(taskId);
            } else {
                return Result.<TaskResponse>builder().status(redirectVoidResult.getStatus()).headers(redirectVoidResult.getHeaders()).build();
            }
        } else {
            return Result.<TaskResponse>builder().status(multipartVoidResult.getStatus()).headers(multipartVoidResult.getHeaders()).build();
        }
    }

    @Override
    public Result<TaskResponse> s3(
        @NotNull final S3ImportRequest s3ImportRequest
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        return requestExecutor.execute(getS3HttpUriRequest(s3ImportRequest), TASK_RESPONSE_TYPE_REFERENCE);
    }

    @Override
    public Result<TaskResponse> azureBlob(
        @NotNull final AzureBlobImportRequest azureBlobImportRequest
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        return requestExecutor.execute(getAzureBlobHttpUriRequest(azureBlobImportRequest), TASK_RESPONSE_TYPE_REFERENCE);
    }

    @Override
    public Result<TaskResponse> googleCloudStorage(
        @NotNull final GoogleCloudStorageImportRequest googleCloudStorageImportRequest
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        return requestExecutor.execute(getGoogleCloudHttpUriRequest(googleCloudStorageImportRequest), TASK_RESPONSE_TYPE_REFERENCE);
    }

    @Override
    public Result<TaskResponse> openStack(
        @NotNull final OpenStackImportRequest openStackImportRequest
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        return requestExecutor.execute(getOpenStackHttpUriRequest(openStackImportRequest), TASK_RESPONSE_TYPE_REFERENCE);
    }

    @Override
    public Result<TaskResponse> sftp(
        @NotNull final SftpImportRequest sftpImportRequest
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        return requestExecutor.execute(getSftpHttpUriRequest(sftpImportRequest), TASK_RESPONSE_TYPE_REFERENCE);
    }

    @Override
    public Result<TaskResponse> base64(
        @NotNull Base64ImportRequest base64ImportRequest
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        return requestExecutor.execute(getBase64HttpUriRequest(base64ImportRequest), TASK_RESPONSE_TYPE_REFERENCE);
    }

    @Override
    public Result<TaskResponse> raw(
        @NotNull RawImportRequest rawImportRequest
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException {
        return requestExecutor.execute(getRawHttpUriRequest(rawImportRequest), TASK_RESPONSE_TYPE_REFERENCE);
    }

    @Override
    public void close() throws IOException {
        requestExecutor.close();
        tasksResource.close();
    }
}
