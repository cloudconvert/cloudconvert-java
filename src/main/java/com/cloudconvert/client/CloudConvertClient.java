package com.cloudconvert.client;

import com.cloudconvert.client.api.key.ApiKeyProvider;
import com.cloudconvert.client.api.key.PropertiesFileApiKeyProvider;
import com.cloudconvert.client.api.url.ApiUrlProvider;
import com.cloudconvert.client.api.url.PropertiesFileApiUrlProvider;
import com.cloudconvert.client.http.CloseableHttpClientProvider;
import com.cloudconvert.client.mapper.ObjectMapperProvider;
import com.cloudconvert.dto.response.JobResponse;
import com.cloudconvert.dto.response.JobResponseData;
import com.cloudconvert.dto.response.OperationResponse;
import com.cloudconvert.dto.response.Pageable;
import com.cloudconvert.dto.response.TaskResponse;
import com.cloudconvert.dto.response.TaskResponseData;
import com.cloudconvert.dto.response.UserResponseData;
import com.cloudconvert.dto.response.WebhookResponse;
import com.cloudconvert.dto.response.WebhookResponseData;
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
    Result<InputStream>, Result<TaskResponseData>, Result<Pageable<TaskResponse>>,
    Result<JobResponseData>, Result<Pageable<JobResponse>>, Result<WebhookResponseData>,
    Result<Pageable<WebhookResponse>>, Result<UserResponseData>, Result<Pageable<OperationResponse>>> {

    public CloudConvertClient() throws IOException {
        this(new PropertiesFileApiUrlProvider(), new PropertiesFileApiKeyProvider());
    }

    public CloudConvertClient(
        final ApiUrlProvider apiUrlProvider, final ApiKeyProvider apiKeyProvider
    ) throws IOException {
        this(apiUrlProvider, apiKeyProvider, new ObjectMapperProvider());
    }

    public CloudConvertClient(
        final ApiUrlProvider apiUrlProvider, final ApiKeyProvider apiKeyProvider, final ObjectMapperProvider objectMapperProvider
    ) throws IOException {
        this(apiUrlProvider, apiKeyProvider, objectMapperProvider,
            new RequestExecutor(new ResultExtractor(objectMapperProvider), new CloseableHttpClientProvider())
        );
    }

    public CloudConvertClient(
        final ApiUrlProvider apiUrlProvider, final ApiKeyProvider apiKeyProvider,
        final ObjectMapperProvider objectMapperProvider, final RequestExecutor requestExecutor
    ) {
        this(apiUrlProvider, apiKeyProvider, objectMapperProvider, requestExecutor,
            new TasksResource(apiUrlProvider, apiKeyProvider, objectMapperProvider, requestExecutor,
                new ConvertFilesResource(apiUrlProvider, apiKeyProvider, objectMapperProvider, requestExecutor),
                new OptimizeFilesResource(apiUrlProvider, apiKeyProvider, objectMapperProvider, requestExecutor),
                new CaptureWebsitesResource(apiUrlProvider, apiKeyProvider, objectMapperProvider, requestExecutor),
                new MergeFilesResource(apiUrlProvider, apiKeyProvider, objectMapperProvider, requestExecutor),
                new CreateArchivesResource(apiUrlProvider, apiKeyProvider, objectMapperProvider, requestExecutor),
                new ExecuteCommandsResource(apiUrlProvider, apiKeyProvider, objectMapperProvider, requestExecutor)
            ),
            new JobsResource(apiUrlProvider, apiKeyProvider, objectMapperProvider, requestExecutor)
        );
    }

    public CloudConvertClient(
        final ApiUrlProvider apiUrlProvider, final ApiKeyProvider apiKeyProvider,
        final ObjectMapperProvider objectMapperProvider, final RequestExecutor requestExecutor,
        final TasksResource tasksResource, final JobsResource jobsResource
    ) {
        super(tasksResource, jobsResource,
            new ImportFilesResource(apiUrlProvider, apiKeyProvider, objectMapperProvider, requestExecutor, tasksResource),
            new ExportFilesResource(apiUrlProvider, apiKeyProvider, objectMapperProvider, requestExecutor),
            new UsersResource(apiUrlProvider, apiKeyProvider, objectMapperProvider, requestExecutor),
            new WebhookResource(apiUrlProvider, apiKeyProvider, objectMapperProvider, requestExecutor),
            new FilesResource(apiUrlProvider, apiKeyProvider, objectMapperProvider, requestExecutor)
        );
    }
}
