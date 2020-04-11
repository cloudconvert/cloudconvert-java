package com.cloudconvert.resource.async;

import com.cloudconvert.client.mapper.ObjectMapperProvider;
import com.cloudconvert.client.setttings.SettingsProvider;
import com.cloudconvert.dto.request.AzureBlobExportRequest;
import com.cloudconvert.dto.request.GoogleCloudStorageExportRequest;
import com.cloudconvert.dto.request.OpenStackExportRequest;
import com.cloudconvert.dto.request.S3ExportRequest;
import com.cloudconvert.dto.request.SftpExportRequest;
import com.cloudconvert.dto.request.UrlExportRequest;
import com.cloudconvert.dto.response.TaskResponse;
import com.cloudconvert.dto.result.AsyncResult;
import com.cloudconvert.executor.AsyncRequestExecutor;
import com.cloudconvert.resource.AbstractExportFilesResource;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URISyntaxException;

@Slf4j
public class AsyncExportFilesResource extends AbstractExportFilesResource<AsyncResult<TaskResponse>> {

    private final AsyncRequestExecutor asyncRequestExecutor;

    public AsyncExportFilesResource(
        final SettingsProvider settingsProvider,
        final ObjectMapperProvider objectMapperProvider, final AsyncRequestExecutor asyncRequestExecutor
    ) {
        super(settingsProvider, objectMapperProvider);

        this.asyncRequestExecutor = asyncRequestExecutor;
    }

    @Override
    public AsyncResult<TaskResponse> url(
        @NotNull final UrlExportRequest urlExportRequest
    ) throws IOException, URISyntaxException {
        return asyncRequestExecutor.execute(getUrlHttpUriRequest(urlExportRequest), TASK_RESPONSE_TYPE_REFERENCE);
    }

    @Override
    public AsyncResult<TaskResponse> s3(
        @NotNull final S3ExportRequest s3ExportRequest
    ) throws IOException, URISyntaxException {
        return asyncRequestExecutor.execute(getS3HttpUriRequest(s3ExportRequest), TASK_RESPONSE_TYPE_REFERENCE);
    }

    @Override
    public AsyncResult<TaskResponse> azureBlob(
        @NotNull final AzureBlobExportRequest azureBlobExportRequest
    ) throws IOException, URISyntaxException {
        return asyncRequestExecutor.execute(getAzureBlobHttpUriRequest(azureBlobExportRequest), TASK_RESPONSE_TYPE_REFERENCE);
    }

    @Override
    public AsyncResult<TaskResponse> googleCloudStorage(
        @NotNull final GoogleCloudStorageExportRequest googleCloudStorageExportRequest
    ) throws IOException, URISyntaxException {
        return asyncRequestExecutor.execute(getGoogleCloudStorageHttpUriRequest(googleCloudStorageExportRequest), TASK_RESPONSE_TYPE_REFERENCE);
    }

    @Override
    public AsyncResult<TaskResponse> openStack(
        @NotNull final OpenStackExportRequest openStackExportRequest
    ) throws IOException, URISyntaxException {
        return asyncRequestExecutor.execute(getOpenStackStorageHttpUriRequest(openStackExportRequest), TASK_RESPONSE_TYPE_REFERENCE);
    }

    @Override
    public AsyncResult<TaskResponse> sftp(
        @NotNull final SftpExportRequest sftpExportRequest
    ) throws IOException, URISyntaxException {
        return asyncRequestExecutor.execute(getSftpStorageHttpUriRequest(sftpExportRequest), TASK_RESPONSE_TYPE_REFERENCE);
    }

    @Override
    public void close() throws IOException {
        asyncRequestExecutor.close();
    }
}
