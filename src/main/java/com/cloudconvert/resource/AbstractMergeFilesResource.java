package com.cloudconvert.resource;

import com.cloudconvert.client.mapper.ObjectMapperProvider;
import com.cloudconvert.client.setttings.SettingsProvider;
import com.cloudconvert.dto.request.MergeFilesTaskRequest;
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

public abstract class AbstractMergeFilesResource<TRAR extends AbstractResult<TaskResponse>> extends AbstractResource {

    public static final String PATH_SEGMENT_MERGE = "merge";

    public AbstractMergeFilesResource(
        final SettingsProvider settingsProvider, final ObjectMapperProvider objectMapperProvider
    ) {
        super(settingsProvider, objectMapperProvider);
    }

    /**
     * Create a task to merge at least two files to one PDF. If input files are not PDFs yet, they are automatically converted to PDF.
     *
     * @param mergeFilesTaskRequest {@link MergeFilesTaskRequest}
     * @return {@link TRAR}
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract TRAR merge(
        @NotNull final MergeFilesTaskRequest mergeFilesTaskRequest
    ) throws IOException, URISyntaxException;

    protected HttpUriRequest getMergeHttpUriRequest(
        @NotNull final MergeFilesTaskRequest mergeFilesTaskRequest
    ) throws IOException, URISyntaxException {
        final URI uri = getUri(ImmutableList.of(PATH_SEGMENT_MERGE));
        final HttpEntity httpEntity = getHttpEntity(mergeFilesTaskRequest);

        return getHttpUriRequest(HttpPost.class, uri, httpEntity);
    }
}
