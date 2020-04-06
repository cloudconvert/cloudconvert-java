package com.cloudconvert.resource;

import com.cloudconvert.client.mapper.ObjectMapperProvider;
import com.cloudconvert.client.setttings.SettingsProvider;
import com.cloudconvert.dto.request.CaptureWebsitesTaskRequest;
import com.cloudconvert.dto.response.TaskResponseData;
import com.cloudconvert.dto.result.AbstractResult;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URISyntaxException;

public abstract class AbstractCaptureWebsitesResource<TRDAR extends AbstractResult<TaskResponseData>> extends AbstractResource {

    public static final String PATH_SEGMENT_CAPTURE_WEBSITE = "capture-website";

    public AbstractCaptureWebsitesResource(
        final SettingsProvider settingsProvider, final ObjectMapperProvider objectMapperProvider
    ) {
        super(settingsProvider, objectMapperProvider);
    }

    /**
     * Create a task to convert a website to PDF or to capture a screenshot of a website (png, jpg).
     *
     * @param captureWebsitesTaskRequest {@link CaptureWebsitesTaskRequest}
     * @return {@link TRDAR}
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract TRDAR capture(
        @NotNull final CaptureWebsitesTaskRequest captureWebsitesTaskRequest
    ) throws IOException, URISyntaxException;
}
