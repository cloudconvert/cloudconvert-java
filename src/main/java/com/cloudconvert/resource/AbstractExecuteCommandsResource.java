package com.cloudconvert.resource;

import com.cloudconvert.client.mapper.ObjectMapperProvider;
import com.cloudconvert.client.setttings.SettingsProvider;
import com.cloudconvert.dto.request.ExecuteCommandsTaskRequest;
import com.cloudconvert.dto.response.TaskResponse;
import com.cloudconvert.dto.result.AbstractResult;
import com.google.common.collect.ImmutableList;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public abstract class AbstractExecuteCommandsResource<TRAR extends AbstractResult<TaskResponse>> extends AbstractResource {

    public static final String PATH_SEGMENT_COMMAND = "command";

    public AbstractExecuteCommandsResource(
        final SettingsProvider settingsProvider, final ObjectMapperProvider objectMapperProvider
    ) {
        super(settingsProvider, objectMapperProvider);
    }

    /**
     * Create a task to execute a command. Currently, ffmpeg, imagemagick and graphicsmagick commands re supported.
     * You can access the files from the input task in the /input/{taskName}/ (For example: /input/import-1/) directory.
     * All files that are created in the /output/ directory are available for following tasks (e.g. export tasks).
     *
     * @param executeCommandsTaskRequest {@link ExecuteCommandsTaskRequest}
     * @return {@link TRAR}
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract TRAR command(
        @NotNull final ExecuteCommandsTaskRequest executeCommandsTaskRequest
    ) throws IOException, URISyntaxException;

    protected HttpUriRequest getCommandHttpUriRequest(
        @NotNull final ExecuteCommandsTaskRequest executeCommandsTaskRequest
    ) throws IOException, URISyntaxException {
        final URI uri = getUri(ImmutableList.of(PATH_SEGMENT_COMMAND));
        final HttpEntity httpEntity = getHttpEntity(executeCommandsTaskRequest);

        return getHttpUriRequest(HttpPost.class, uri, httpEntity);
    }
}
