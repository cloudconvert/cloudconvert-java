package com.cloudconvert.resource;

import com.cloudconvert.client.mapper.ObjectMapperProvider;
import com.cloudconvert.client.setttings.SettingsProvider;
import com.cloudconvert.dto.request.AddWatermarkTaskRequest;
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

public abstract class AbstractAddWatermarkResource<TRAR extends AbstractResult<TaskResponse>> extends AbstractResource {

    public static final String PATH_SEGMENT_ADD_WATERMARK = "watermark";

    public AbstractAddWatermarkResource(
        final SettingsProvider settingsProvider, final ObjectMapperProvider objectMapperProvider
    ) {
        super(settingsProvider, objectMapperProvider);
    }

    /**
     * Create a task to add a watermark.
     *
     * @param addWatermarkTaskRequest {@link AddWatermarkTaskRequest}
     * @return {@link TRAR}
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract TRAR watermark(
        @NotNull final AddWatermarkTaskRequest addWatermarkTaskRequest
    ) throws IOException, URISyntaxException, CloudConvertClientException, CloudConvertServerException;

    protected HttpUriRequest getWatermarkHttpUriRequest(
        @NotNull final AddWatermarkTaskRequest addWatermarkTaskRequest
    ) throws IOException, URISyntaxException {
        final URI uri = getUri(ImmutableList.of(PATH_SEGMENT_ADD_WATERMARK));
        final HttpEntity httpEntity = getHttpEntity(addWatermarkTaskRequest);

        return getHttpUriRequest(HttpPost.class, uri, httpEntity);
    }
}
