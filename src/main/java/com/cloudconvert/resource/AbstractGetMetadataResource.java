package com.cloudconvert.resource;

import com.cloudconvert.client.mapper.ObjectMapperProvider;
import com.cloudconvert.client.setttings.SettingsProvider;
import com.cloudconvert.dto.request.GetMetadataTaskRequest;
import com.cloudconvert.dto.response.TaskResponse;
import com.cloudconvert.dto.result.AbstractResult;
import com.cloudconvert.exception.CloudConvertClientException;
import com.cloudconvert.exception.CloudConvertServerException;
import com.google.common.collect.ImmutableList;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public abstract class AbstractGetMetadataResource<TRAR extends AbstractResult<TaskResponse>> extends AbstractResource {

    public static final String PATH_SEGMENT_METADATA = "metadata";

    public AbstractGetMetadataResource(
        final SettingsProvider settingsProvider, final ObjectMapperProvider objectMapperProvider
    ) {
        super(settingsProvider, objectMapperProvider);
    }

    /**
     * Create a task to optimize and compress a file. Currently supported formats are PDF, PNG and JPG.
     *
     * @param getMetadataTaskRequest {@link GetMetadataTaskRequest}
     * @return {@link TRAR}
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract TRAR metadata(
        @NotNull final GetMetadataTaskRequest getMetadataTaskRequest
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException;

    protected HttpUriRequest getMetadataHttpUriRequest(
        @NotNull final GetMetadataTaskRequest getMetadataTaskRequest
    ) throws IOException, URISyntaxException {
        final URI uri = getUri(ImmutableList.of(PATH_SEGMENT_METADATA));
        final HttpEntity httpEntity = getHttpEntity(getMetadataTaskRequest);

        return getHttpUriRequest(HttpPost.class, uri, httpEntity);
    }
}
