package com.cloudconvert.resource;

import com.cloudconvert.client.api.key.ApiKeyProvider;
import com.cloudconvert.client.api.url.ApiUrlProvider;
import com.cloudconvert.client.mapper.ObjectMapperProvider;
import com.cloudconvert.dto.request.ExecuteCommandsTaskRequest;
import com.cloudconvert.dto.response.TaskResponseData;
import com.cloudconvert.dto.result.AbstractResult;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URISyntaxException;

public abstract class AbstractExecuteCommandsResource<TRDAR extends AbstractResult<TaskResponseData>> extends AbstractResource {

    public static final String PATH_SEGMENT_COMMAND = "command";

    public AbstractExecuteCommandsResource(
        final ApiUrlProvider apiUrlProvider, final ApiKeyProvider apiKeyProvider, final ObjectMapperProvider objectMapperProvider
    ) {
        super(apiUrlProvider, apiKeyProvider, objectMapperProvider);
    }

    /**
     * Create a task to execute a command. Currently, ffmpeg, imagemagick and graphicsmagick commands re supported.
     * You can access the files from the input task in the /input/{taskName}/ (For example: /input/import-1/) directory.
     * All files that are created in the /output/ directory are available for following tasks (e.g. export tasks).
     *
     * @param executeCommandsTaskRequest {@link ExecuteCommandsTaskRequest}
     * @return {@link TRDAR}
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract TRDAR command(
        @NotNull final ExecuteCommandsTaskRequest executeCommandsTaskRequest
    ) throws IOException, URISyntaxException;
}
