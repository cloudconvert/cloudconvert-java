package com.cloudconvert.client;

import com.cloudconvert.client.http.CloseableHttpClientProvider;
import com.cloudconvert.client.mapper.ObjectMapperProvider;
import com.cloudconvert.client.setttings.PropertyFileSettingsProvider;
import com.cloudconvert.client.setttings.SettingsProvider;
import com.cloudconvert.dto.response.JobResponse;
import com.cloudconvert.dto.response.OperationResponse;
import com.cloudconvert.dto.response.Pageable;
import com.cloudconvert.dto.response.TaskResponse;
import com.cloudconvert.dto.response.UserResponse;
import com.cloudconvert.dto.response.WebhookResponse;
import com.cloudconvert.dto.result.Result;
import com.cloudconvert.executor.RequestExecutor;
import com.cloudconvert.extractor.ResultExtractor;
import com.cloudconvert.resource.sync.CaptureWebsitesResource;
import com.cloudconvert.resource.sync.ConvertFilesResource;
import com.cloudconvert.resource.sync.CreateArchivesResource;
import com.cloudconvert.resource.sync.ExecuteCommandsResource;
import com.cloudconvert.resource.sync.ExportFilesResource;
import com.cloudconvert.resource.sync.FilesResource;
import com.cloudconvert.resource.sync.ImportFilesResource;
import com.cloudconvert.resource.sync.JobsResource;
import com.cloudconvert.resource.sync.MergeFilesResource;
import com.cloudconvert.resource.sync.OptimizeFilesResource;
import com.cloudconvert.resource.sync.TasksResource;
import com.cloudconvert.resource.sync.UsersResource;
import com.cloudconvert.resource.sync.WebhookResource;

import java.io.IOException;
import java.io.InputStream;

public class CloudConvertClient extends AbstractCloudConvertClient<Result<Void>,
    Result<InputStream>, Result<TaskResponse>, Result<Pageable<TaskResponse>>,
    Result<JobResponse>, Result<Pageable<JobResponse>>, Result<WebhookResponse>,
    Result<Pageable<WebhookResponse>>, Result<UserResponse>, Result<Pageable<OperationResponse>>> {

    public CloudConvertClient() throws IOException {
        this(new PropertyFileSettingsProvider());
    }

    public CloudConvertClient(
        final SettingsProvider settingsProvider
    ) throws IOException {
        this(settingsProvider, new ObjectMapperProvider());
    }

    public CloudConvertClient(
        final SettingsProvider settingsProvider, final ObjectMapperProvider objectMapperProvider
    ) throws IOException {
        this(settingsProvider, objectMapperProvider, new RequestExecutor(new ResultExtractor(objectMapperProvider), new CloseableHttpClientProvider()));
    }

    public CloudConvertClient(
        final SettingsProvider settingsProvider,
        final ObjectMapperProvider objectMapperProvider, final RequestExecutor requestExecutor
    ) {
        this(settingsProvider, objectMapperProvider, requestExecutor,
            new TasksResource(settingsProvider, objectMapperProvider, requestExecutor,
                new ConvertFilesResource(settingsProvider, objectMapperProvider, requestExecutor),
                new OptimizeFilesResource(settingsProvider, objectMapperProvider, requestExecutor),
                new CaptureWebsitesResource(settingsProvider, objectMapperProvider, requestExecutor),
                new MergeFilesResource(settingsProvider, objectMapperProvider, requestExecutor),
                new CreateArchivesResource(settingsProvider, objectMapperProvider, requestExecutor),
                new ExecuteCommandsResource(settingsProvider, objectMapperProvider, requestExecutor)
            ),
            new JobsResource(settingsProvider, objectMapperProvider, requestExecutor)
        );
    }

    public CloudConvertClient(
        final SettingsProvider settingsProvider,
        final ObjectMapperProvider objectMapperProvider, final RequestExecutor requestExecutor,
        final TasksResource tasksResource, final JobsResource jobsResource
    ) {
        super(tasksResource, jobsResource,
            new ImportFilesResource(settingsProvider, objectMapperProvider, requestExecutor, tasksResource),
            new ExportFilesResource(settingsProvider, objectMapperProvider, requestExecutor),
            new UsersResource(settingsProvider, objectMapperProvider, requestExecutor),
            new WebhookResource(settingsProvider, objectMapperProvider, requestExecutor),
            new FilesResource(settingsProvider, objectMapperProvider, requestExecutor)
        );
    }
}
