package com.cloudconvert.client;

import com.cloudconvert.client.http.AsyncCloseableHttpClientProvider;
import com.cloudconvert.client.mapper.ObjectMapperProvider;
import com.cloudconvert.client.setttings.PropertyFileSettingsProvider;
import com.cloudconvert.client.setttings.SettingsProvider;
import com.cloudconvert.dto.response.JobResponse;
import com.cloudconvert.dto.response.OperationResponse;
import com.cloudconvert.dto.response.Pageable;
import com.cloudconvert.dto.response.TaskResponse;
import com.cloudconvert.dto.response.UserResponse;
import com.cloudconvert.dto.response.WebhookResponse;
import com.cloudconvert.dto.result.AsyncResult;
import com.cloudconvert.executor.AsyncRequestExecutor;
import com.cloudconvert.extractor.ResultExtractor;
import com.cloudconvert.resource.async.*;

import java.io.IOException;
import java.io.InputStream;

public class AsyncCloudConvertClient extends AbstractCloudConvertClient<AsyncResult<Void>,
    AsyncResult<InputStream>, AsyncResult<TaskResponse>, AsyncResult<Pageable<TaskResponse>>,
    AsyncResult<JobResponse>, AsyncResult<Pageable<JobResponse>>, AsyncResult<WebhookResponse>,
    AsyncResult<Pageable<WebhookResponse>>, AsyncResult<UserResponse>, AsyncResult<Pageable<OperationResponse>>> {

    public AsyncCloudConvertClient() throws IOException {
        this(new PropertyFileSettingsProvider());
    }

    public AsyncCloudConvertClient(
        final SettingsProvider settingsProvider
    ) throws IOException {
        this(settingsProvider, new ObjectMapperProvider());
    }

    public AsyncCloudConvertClient(
        final SettingsProvider settingsProvider, final ObjectMapperProvider objectMapperProvider
    ) throws IOException {
        this(settingsProvider, objectMapperProvider, new AsyncRequestExecutor(new ResultExtractor(objectMapperProvider), new AsyncCloseableHttpClientProvider()));
    }

    public AsyncCloudConvertClient(
        final SettingsProvider settingsProvider, final ObjectMapperProvider objectMapperProvider, final AsyncRequestExecutor asyncRequestExecutor
    ) {
        this(settingsProvider, objectMapperProvider, asyncRequestExecutor,
            new AsyncTasksResource(settingsProvider, objectMapperProvider, asyncRequestExecutor,
                new AsyncConvertFilesResource(settingsProvider, objectMapperProvider, asyncRequestExecutor),
                new AsyncOptimizeFilesResource(settingsProvider, objectMapperProvider, asyncRequestExecutor),
                new AsyncCaptureWebsitesResource(settingsProvider, objectMapperProvider, asyncRequestExecutor),
                new AsyncMergeFilesResource(settingsProvider, objectMapperProvider, asyncRequestExecutor),
                new AsyncCreateArchivesResource(settingsProvider, objectMapperProvider, asyncRequestExecutor),
                new AsyncExecuteCommandsResource(settingsProvider, objectMapperProvider, asyncRequestExecutor),
                new AsyncCreateThumbnailsResource(settingsProvider, objectMapperProvider, asyncRequestExecutor),
                new AsyncGetMetadataResource(settingsProvider, objectMapperProvider, asyncRequestExecutor),
                new AsyncWriteMetadataResource(settingsProvider, objectMapperProvider, asyncRequestExecutor)
            ),
            new AsyncJobsResource(settingsProvider, objectMapperProvider, asyncRequestExecutor)
        );
    }

    public AsyncCloudConvertClient(
        final SettingsProvider settingsProvider, final ObjectMapperProvider objectMapperProvider,
        final AsyncRequestExecutor asyncRequestExecutor, final AsyncTasksResource asyncTasksResource, final AsyncJobsResource asyncJobsResource
    ) {
        super(asyncTasksResource, asyncJobsResource,
            new AsyncImportFilesResource(settingsProvider, objectMapperProvider, asyncRequestExecutor, asyncTasksResource),
            new AsyncExportFilesResource(settingsProvider, objectMapperProvider, asyncRequestExecutor),
            new AsyncUsersResource(settingsProvider, objectMapperProvider, asyncRequestExecutor),
            new AsyncWebhookResource(settingsProvider, objectMapperProvider, asyncRequestExecutor),
            new AsyncFilesResource(settingsProvider, objectMapperProvider, asyncRequestExecutor),
            new AsyncSignedUrlResource(settingsProvider, objectMapperProvider, asyncRequestExecutor)
        );
    }
}
