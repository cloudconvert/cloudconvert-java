package com.cloudconvert.resource;

import com.cloudconvert.client.mapper.ObjectMapperProvider;
import com.cloudconvert.client.setttings.SettingsProvider;
import com.cloudconvert.dto.request.OptimizeFilesTaskRequest;
import com.cloudconvert.dto.response.TaskResponse;
import com.cloudconvert.dto.result.AbstractResult;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URISyntaxException;

public abstract class AbstractOptimizeFilesResource<TRAR extends AbstractResult<TaskResponse>> extends AbstractResource {

    public static final String PATH_SEGMENT_OPTIMIZE = "optimize";

    public AbstractOptimizeFilesResource(
        final SettingsProvider settingsProvider, final ObjectMapperProvider objectMapperProvider
    ) {
        super(settingsProvider, objectMapperProvider);
    }

    /**
     * Create a task to optimize and compress a file. Currently supported formats are PDF, PNG and JPG.
     *
     * @param optimizeFilesTaskRequest {@link OptimizeFilesTaskRequest}
     * @return {@link TRAR}
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract TRAR optimize(
        @NotNull final OptimizeFilesTaskRequest optimizeFilesTaskRequest
    ) throws IOException, URISyntaxException;
}
