package com.cloudconvert.resource.task;

import static javax.ws.rs.core.Response.Status;
import static javax.ws.rs.core.Response.Status.Family;

import com.cloudconvert.client.api.key.ApiKeyProvider;
import com.cloudconvert.client.api.url.ApiUrlProvider;
import com.cloudconvert.client.http.SyncHttpClientProvider;
import com.cloudconvert.client.mapper.ObjectMapperProvider;
import com.cloudconvert.client.processor.DefaultResponseProcessor;
import com.cloudconvert.client.processor.NoContentResponseProcessor;
import com.cloudconvert.client.processor.OkResponseProcessor;
import com.cloudconvert.client.processor.ResponseProcessor;
import com.cloudconvert.dto.Operation;
import com.cloudconvert.dto.Task;
import com.cloudconvert.resource.Response;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class SyncTaskResource implements TaskResource<Task, Operation> {

    private static final String BEARER = "Bearer";

    private static final String PATH_SEGMENT_TASKS = "tasks";
    private static final String PATH_SEGMENT_OPERATIONS = "operations";

    private static final String PARAMETER_INCLUDE = "include";

    private static final TypeReference<Void> VOID_TYPE_REFERENCE = new TypeReference<Void>() {};
    private static final TypeReference<Task> TASK_TYPE_REFERENCE = new TypeReference<Task>() {};
    private static final TypeReference<List<Task>> LIST_OF_TASKS_TYPE_REFERENCE = new TypeReference<List<Task>>() {};
    private static final TypeReference<List<Operation>> LIST_OF_OPERATIONS_TYPE_REFERENCE = new TypeReference<List<Operation>>() {};

    private final ApiKeyProvider apiKeyProvider;
    private final ApiUrlProvider apiUrlProvider;
    private final ObjectMapperProvider objectMapperProvider;
    private final SyncHttpClientProvider syncHttpClientProvider;

    private final ResponseProcessor defaultResponseProcessor;
    private final Map<Status, ResponseProcessor> responseProcessors;

    public SyncTaskResource(
        final ApiKeyProvider apiKeyProvider, final ApiUrlProvider apiUrlProvider,
        final ObjectMapperProvider objectMapperProvider, final SyncHttpClientProvider syncHttpClientProvider
    ) {
        this.apiKeyProvider = apiKeyProvider;
        this.apiUrlProvider = apiUrlProvider;
        this.objectMapperProvider = objectMapperProvider;
        this.syncHttpClientProvider = syncHttpClientProvider;

        this.defaultResponseProcessor = new DefaultResponseProcessor();
        this.responseProcessors = ImmutableMap.of(
            Status.OK, new OkResponseProcessor(),
            Status.NO_CONTENT, new NoContentResponseProcessor()
        );
    }

    public Response<Task> show(@NotNull final String taskId, @NotNull final Set<Include> includes) throws IOException, URISyntaxException {
        final URI uri = new URIBuilder().setPath(apiUrlProvider.provide()).setPathSegments(PATH_SEGMENT_TASKS, taskId)
            .setParameter(PARAMETER_INCLUDE, includes.stream().map(Include::getValue).collect(Collectors.joining(","))).build();

        final RequestBuilder requestBuilder = RequestBuilder.get().setUri(uri);

        return execute(requestBuilder, TASK_TYPE_REFERENCE);
    }

    public Response<Task> wait(@NotNull final String taskId) throws IOException, URISyntaxException {
        final URI uri = new URIBuilder().setPath(apiUrlProvider.provide()).setPathSegments(PATH_SEGMENT_TASKS, taskId, "wait").build();

        final RequestBuilder requestBuilder = RequestBuilder.get().setUri(uri);

        return execute(requestBuilder, TASK_TYPE_REFERENCE);
    }

    public Response<List<Task>> list() throws IOException, URISyntaxException {
        final URI uri = new URIBuilder().setPath(apiUrlProvider.provide()).setPathSegments(PATH_SEGMENT_TASKS).build();

        final RequestBuilder requestBuilder = RequestBuilder.get().setUri(uri);

        return execute(requestBuilder, LIST_OF_TASKS_TYPE_REFERENCE);
    }

    public Response<Task> cancel(@NotNull final String taskId) throws IOException, URISyntaxException {
        final URI uri = new URIBuilder().setPath(apiUrlProvider.provide()).setPathSegments(PATH_SEGMENT_TASKS, taskId, "cancel").build();

        final RequestBuilder requestBuilder = RequestBuilder.post().setUri(uri);

        return execute(requestBuilder, TASK_TYPE_REFERENCE);
    }

    public Response<Task> retry(@NotNull final String taskId) throws IOException, URISyntaxException {
        final URI uri = new URIBuilder().setPath(apiUrlProvider.provide()).setPathSegments(PATH_SEGMENT_TASKS, taskId, "retry").build();

        final RequestBuilder requestBuilder = RequestBuilder.post().setUri(uri);

        return execute(requestBuilder, TASK_TYPE_REFERENCE);
    }

    public Response<Void> delete(@NotNull final String taskId) throws IOException, URISyntaxException {
        final URI uri = new URIBuilder().setPath(apiUrlProvider.provide()).setPathSegments(PATH_SEGMENT_TASKS, taskId).build();

        final RequestBuilder requestBuilder = RequestBuilder.delete().setUri(uri);

        return execute(requestBuilder, VOID_TYPE_REFERENCE);
    }

    public Response<List<Operation>> operations(@NotNull final Object object) throws IOException, URISyntaxException {
        final URI uri = new URIBuilder().setPath(apiUrlProvider.provide()).setPathSegments(PATH_SEGMENT_OPERATIONS).build();

        final RequestBuilder requestBuilder = RequestBuilder.get().setUri(uri);

        // TODO Add filters
        return execute(requestBuilder, LIST_OF_OPERATIONS_TYPE_REFERENCE);
    }

    private <T> Response<T> execute(final RequestBuilder requestBuilder, final TypeReference<T> typeReference) throws IOException {
        final HttpUriRequest httpUriRequest = requestBuilder.setHeader(HEADER_AUTHORIZATION, BEARER + " " + apiKeyProvider.provide()).build();

        try (
            final CloseableHttpClient closeableHttpClient = syncHttpClientProvider.provide();
            final CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(httpUriRequest);
        ) {
            final Status status = Status.fromStatusCode(closeableHttpResponse.getStatusLine().getStatusCode());

            return responseProcessors.getOrDefault(status, defaultResponseProcessor).process(status, closeableHttpResponse, typeReference);

            if (Family.SUCCESSFUL.equals(status.getFamily())) {
                return Response.<T>builder().status(status)
                    .error("").body(objectMapperProvider.provide().readValue(closeableHttpResponse.getEntity().getContent(), typeReference)).build();
            } else {
                return Response.<T>builder().status(status)
                    .error(objectMapperProvider.provide().readValue(closeableHttpResponse.getEntity().getContent(), String.class)).build();
            }
        }
    }
}
