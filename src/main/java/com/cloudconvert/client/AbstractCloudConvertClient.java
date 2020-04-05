package com.cloudconvert.client;

import com.cloudconvert.dto.response.JobResponse;
import com.cloudconvert.dto.response.JobResponseData;
import com.cloudconvert.dto.response.OperationResponse;
import com.cloudconvert.dto.response.Pageable;
import com.cloudconvert.dto.response.TaskResponse;
import com.cloudconvert.dto.response.TaskResponseData;
import com.cloudconvert.dto.response.UserResponseData;
import com.cloudconvert.dto.response.WebhookResponse;
import com.cloudconvert.dto.response.WebhookResponseData;
import com.cloudconvert.dto.result.AbstractResult;
import com.cloudconvert.resource.AbstractExportFilesResource;
import com.cloudconvert.resource.AbstractFilesResource;
import com.cloudconvert.resource.AbstractImportFilesResource;
import com.cloudconvert.resource.AbstractJobsResource;
import com.cloudconvert.resource.AbstractTasksResource;
import com.cloudconvert.resource.AbstractUsersResource;
import com.cloudconvert.resource.AbstractWebhooksResource;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public class AbstractCloudConvertClient<
    VAR extends AbstractResult<Void>, ISAR extends AbstractResult<InputStream>,
    TRDAR extends AbstractResult<TaskResponseData>, TRPAR extends AbstractResult<Pageable<TaskResponse>>,
    JRDAR extends AbstractResult<JobResponseData>, JRPAR extends AbstractResult<Pageable<JobResponse>>,
    WRDAR extends AbstractResult<WebhookResponseData>, WRPAR extends AbstractResult<Pageable<WebhookResponse>>,
    URDAR extends AbstractResult<UserResponseData>, ORPAR extends AbstractResult<Pageable<OperationResponse>>> implements Closeable {

    private final AbstractTasksResource<TRDAR, TRPAR, VAR, ORPAR> abstractTasksResource;
    private final AbstractJobsResource<JRDAR, JRPAR, VAR> abstractJobsResource;
    private final AbstractImportFilesResource<TRDAR> abstractImportFilesResource;
    private final AbstractExportFilesResource<TRDAR> abstractExportFilesResource;
    private final AbstractUsersResource<URDAR> abstractUsersResource;
    private final AbstractWebhooksResource<WRDAR, WRPAR, VAR> abstractWebhooksResource;
    private final AbstractFilesResource<ISAR> abstractFilesResource;

    public AbstractCloudConvertClient(
        final AbstractTasksResource<TRDAR, TRPAR, VAR, ORPAR> abstractTasksResource, final AbstractJobsResource<JRDAR, JRPAR, VAR> abstractJobsResource,
        final AbstractImportFilesResource<TRDAR> abstractImportFilesResource, final AbstractExportFilesResource<TRDAR> abstractExportFilesResource,
        final AbstractUsersResource<URDAR> abstractUsersResource, final AbstractWebhooksResource<WRDAR, WRPAR, VAR> abstractWebhooksResource,
        final AbstractFilesResource<ISAR> abstractFilesResource
    ) {
        this.abstractTasksResource = abstractTasksResource;
        this.abstractJobsResource = abstractJobsResource;
        this.abstractImportFilesResource = abstractImportFilesResource;
        this.abstractExportFilesResource = abstractExportFilesResource;
        this.abstractUsersResource = abstractUsersResource;
        this.abstractWebhooksResource = abstractWebhooksResource;
        this.abstractFilesResource = abstractFilesResource;
    }

    public AbstractTasksResource<TRDAR, TRPAR, VAR, ORPAR> tasks() {
        return abstractTasksResource;
    }

    public AbstractJobsResource<JRDAR, JRPAR, VAR> jobs() {
        return abstractJobsResource;
    }

    public AbstractImportFilesResource<TRDAR> importUsing() {
        return abstractImportFilesResource;
    }

    public AbstractExportFilesResource<TRDAR> exportUsing() {
        return abstractExportFilesResource;
    }

    public AbstractUsersResource<URDAR> users() {
        return abstractUsersResource;
    }

    public AbstractWebhooksResource<WRDAR, WRPAR, VAR> webhooks() {
        return abstractWebhooksResource;
    }

    public AbstractFilesResource<ISAR> files() {
        return abstractFilesResource;
    }

    @Override
    public void close() throws IOException {
        abstractTasksResource.close();
        abstractJobsResource.close();
        abstractImportFilesResource.close();
        abstractExportFilesResource.close();
        abstractUsersResource.close();
        abstractWebhooksResource.close();
        abstractFilesResource.close();
    }
}
