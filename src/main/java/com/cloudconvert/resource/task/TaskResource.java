package com.cloudconvert.resource.task;

import com.cloudconvert.resource.Resource;
import com.cloudconvert.resource.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

public interface TaskResource<T, O> extends Resource {

    Response<T> show(@NotNull final String taskId, @NotNull final Set<SyncTaskResource.Include> includes) throws IOException, URISyntaxException;

    Response<T> wait(@NotNull final String taskId) throws IOException, URISyntaxException;

    Response<List<T>> list() throws IOException, URISyntaxException;

    Response<T> cancel(@NotNull final String taskId) throws IOException, URISyntaxException;

    Response<T> retry(@NotNull final String taskId) throws IOException, URISyntaxException;

    Response<Void> delete(@NotNull final String taskId) throws IOException, URISyntaxException;

    Response<List<O>> operations(@NotNull final Object object) throws IOException, URISyntaxException;

    enum Include {
        RETRIES("retries"),
        DEPENDS_ON_TASKS("depends_on_tasks"),
        PAYLOAD("payload"),
        JOB("job");

        private String value;

        Include(final String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
