package com.cloudconvert.resource;

import com.cloudconvert.client.mapper.ObjectMapperProvider;
import com.cloudconvert.client.setttings.SettingsProvider;
import com.cloudconvert.dto.request.CreateThumbnailsTaskRequest;
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

public abstract class AbstractCreateThumbnailsResource<TRAR extends AbstractResult<TaskResponse>> extends AbstractResource {

    public static final String PATH_SEGMENT_CREATE_THUMBNAIL = "thumbnail";

    public AbstractCreateThumbnailsResource(
        final SettingsProvider settingsProvider, final ObjectMapperProvider objectMapperProvider
    ) {
        super(settingsProvider, objectMapperProvider);
    }

    /**
     * Create a task to create a thumbnail (png, jpg).
     *
     * @param createThumbnailsTaskRequest {@link CreateThumbnailsTaskRequest}
     * @return {@link TRAR}
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract TRAR thumbnail(
        @NotNull final CreateThumbnailsTaskRequest createThumbnailsTaskRequest
    ) throws IOException, URISyntaxException;

    protected HttpUriRequest getThumbnailHttpUriRequest(
        @NotNull final CreateThumbnailsTaskRequest createThumbnailsTaskRequest
    ) throws IOException, URISyntaxException {
        final URI uri = getUri(ImmutableList.of(PATH_SEGMENT_CREATE_THUMBNAIL));
        final HttpEntity httpEntity = getHttpEntity(createThumbnailsTaskRequest);

        return getHttpUriRequest(HttpPost.class, uri, httpEntity);
    }
}
