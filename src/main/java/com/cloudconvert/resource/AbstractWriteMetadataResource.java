package com.cloudconvert.resource;

import com.cloudconvert.client.mapper.ObjectMapperProvider;
import com.cloudconvert.client.setttings.SettingsProvider;
import com.cloudconvert.dto.request.WriteMetadataTaskRequest;
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

public abstract class AbstractWriteMetadataResource<TRAR extends AbstractResult<TaskResponse>> extends AbstractResource {

    public static final String PATH_SEGMENT_METADATA = "metadata";
    public static final String PATH_SEGMENT_WRITE = "write";

    public AbstractWriteMetadataResource(
        final SettingsProvider settingsProvider, final ObjectMapperProvider objectMapperProvider
    ) {
        super(settingsProvider, objectMapperProvider);
    }

    /**
     * Create a task to write file metadata.
     *
     * @param writeMetadataTaskRequest {@link WriteMetadataTaskRequest}
     * @return {@link TRAR}
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract TRAR writeMetadata(
        @NotNull final WriteMetadataTaskRequest writeMetadataTaskRequest
    ) throws IOException, URISyntaxException;

    protected HttpUriRequest getWriteMetadataHttpUriRequest(
        @NotNull final WriteMetadataTaskRequest writeMetadataTaskRequest
    ) throws IOException, URISyntaxException {
        final URI uri = getUri(ImmutableList.of(PATH_SEGMENT_METADATA, PATH_SEGMENT_WRITE));
        final HttpEntity httpEntity = getHttpEntity(writeMetadataTaskRequest);

        return getHttpUriRequest(HttpPost.class, uri, httpEntity);
    }
}
