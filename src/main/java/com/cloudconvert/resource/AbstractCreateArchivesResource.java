package com.cloudconvert.resource;

import com.cloudconvert.client.mapper.ObjectMapperProvider;
import com.cloudconvert.client.setttings.SettingsProvider;
import com.cloudconvert.dto.request.CreateArchivesTaskRequest;
import com.cloudconvert.dto.response.TaskResponse;
import com.cloudconvert.dto.result.AbstractResult;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URISyntaxException;

public abstract class AbstractCreateArchivesResource<TRAR extends AbstractResult<TaskResponse>> extends AbstractResource {

    public static final String PATH_SEGMENT_ARCHIVE = "archive";

    public AbstractCreateArchivesResource(
        final SettingsProvider settingsProvider, final ObjectMapperProvider objectMapperProvider
    ) {
        super(settingsProvider, objectMapperProvider);
    }

    /**
     * Create a task to create a ZIP, RAR, 7Z, TAR, TAR.GZ or TAR.BZ2 archive.
     *
     * @param createArchivesTaskRequest {@link CreateArchivesTaskRequest}
     * @return {@link TRAR}
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract TRAR archive(
        @NotNull final CreateArchivesTaskRequest createArchivesTaskRequest
    ) throws IOException, URISyntaxException;
}
