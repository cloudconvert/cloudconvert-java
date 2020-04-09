package com.cloudconvert.client;

import com.cloudconvert.dto.response.JobResponse;
import com.cloudconvert.dto.response.OperationResponse;
import com.cloudconvert.dto.response.Pageable;
import com.cloudconvert.dto.response.TaskResponse;
import com.cloudconvert.dto.response.UserResponse;
import com.cloudconvert.dto.response.WebhookResponse;
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
    TRAR extends AbstractResult<TaskResponse>, TRPAR extends AbstractResult<Pageable<TaskResponse>>,
    JRAR extends AbstractResult<JobResponse>, JRPAR extends AbstractResult<Pageable<JobResponse>>,
    WRAR extends AbstractResult<WebhookResponse>, WRPAR extends AbstractResult<Pageable<WebhookResponse>>,
    URAR extends AbstractResult<UserResponse>, ORPAR extends AbstractResult<Pageable<OperationResponse>>> implements Closeable {

    private final AbstractTasksResource<TRAR, TRPAR, VAR, ORPAR> abstractTasksResource;
    private final AbstractJobsResource<JRAR, JRPAR, VAR> abstractJobsResource;
    private final AbstractImportFilesResource<TRAR> abstractImportFilesResource;
    private final AbstractExportFilesResource<TRAR> abstractExportFilesResource;
    private final AbstractUsersResource<URAR> abstractUsersResource;
    private final AbstractWebhooksResource<WRAR, WRPAR, VAR> abstractWebhooksResource;
    private final AbstractFilesResource<ISAR> abstractFilesResource;

    public AbstractCloudConvertClient(
        final AbstractTasksResource<TRAR, TRPAR, VAR, ORPAR> abstractTasksResource, final AbstractJobsResource<JRAR, JRPAR, VAR> abstractJobsResource,
        final AbstractImportFilesResource<TRAR> abstractImportFilesResource, final AbstractExportFilesResource<TRAR> abstractExportFilesResource,
        final AbstractUsersResource<URAR> abstractUsersResource, final AbstractWebhooksResource<WRAR, WRPAR, VAR> abstractWebhooksResource,
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

    public AbstractTasksResource<TRAR, TRPAR, VAR, ORPAR> tasks() {
        return abstractTasksResource;
    }

    public AbstractJobsResource<JRAR, JRPAR, VAR> jobs() {
        return abstractJobsResource;
    }

    public AbstractImportFilesResource<TRAR> importUsing() {
        return abstractImportFilesResource;
    }

    public AbstractExportFilesResource<TRAR> exportUsing() {
        return abstractExportFilesResource;
    }

    public AbstractUsersResource<URAR> users() {
        return abstractUsersResource;
    }

    public AbstractWebhooksResource<WRAR, WRPAR, VAR> webhooks() {
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
