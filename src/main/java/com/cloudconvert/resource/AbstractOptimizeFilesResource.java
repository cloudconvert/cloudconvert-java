package com.cloudconvert.resource;

import com.cloudconvert.client.mapper.ObjectMapperProvider;
import com.cloudconvert.client.setttings.SettingsProvider;
import com.cloudconvert.dto.request.OptimizeFilesTaskRequest;
import com.cloudconvert.dto.response.TaskResponseData;
import com.cloudconvert.dto.result.AbstractResult;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URISyntaxException;

public abstract class AbstractOptimizeFilesResource<TRDAR extends AbstractResult<TaskResponseData>> extends AbstractResource {

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
     * @return {@link TRDAR}
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract TRDAR optimize(
        @NotNull final OptimizeFilesTaskRequest optimizeFilesTaskRequest
    ) throws IOException, URISyntaxException;
}
