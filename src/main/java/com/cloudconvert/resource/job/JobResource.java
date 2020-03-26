package com.cloudconvert.resource.job;

import com.cloudconvert.resource.Resource;
import com.cloudconvert.resource.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public interface JobResource<J> extends Resource {

    Response<J> create(@NotNull final List<Object> tasks) throws IOException, URISyntaxException;

    Response<J> show(@NotNull final String jobId) throws IOException, URISyntaxException;

    Response<J> wait() throws IOException, URISyntaxException;

    Response<List<J>> list() throws IOException, URISyntaxException;

    Response<Void> delete(@NotNull final String taskId) throws IOException, URISyntaxException;
}
