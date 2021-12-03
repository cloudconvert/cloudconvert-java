package com.cloudconvert.extractor;

import com.cloudconvert.client.mapper.ObjectMapperProvider;
import com.cloudconvert.dto.result.Result;
import com.cloudconvert.dto.result.Status;
import com.cloudconvert.exception.CloudConvertClientException;
import com.cloudconvert.exception.CloudConvertServerException;
import com.cloudconvert.processor.response.DefaultResponseProcessor;
import com.cloudconvert.processor.response.ResponseProcessor;
import com.cloudconvert.processor.response.successful.ContentResponseProcessor;
import com.cloudconvert.processor.response.successful.InputStreamResponseProcessor;
import com.cloudconvert.processor.response.successful.NoContentResponseProcessor;
import com.cloudconvert.resource.AbstractResource;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableMap;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.io.EmptyInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ResultExtractor {

    private final ResponseProcessor defaultResponseProcessor;
    private final Map<TypeReference<?>, ResponseProcessor> responseProcessors;

    public ResultExtractor(final ObjectMapperProvider objectMapperProvider) {
        final ContentResponseProcessor contentResponseProcessor = new ContentResponseProcessor(objectMapperProvider);
        final NoContentResponseProcessor noContentResponseProcessor = new NoContentResponseProcessor();
        final InputStreamResponseProcessor inputStreamResponseProcessor = new InputStreamResponseProcessor();

        this.defaultResponseProcessor = new DefaultResponseProcessor();
        this.responseProcessors = ImmutableMap.<TypeReference<?>, ResponseProcessor>builder()
            .put(AbstractResource.VOID_TYPE_REFERENCE, noContentResponseProcessor)
            .put(AbstractResource.INPUT_STREAM_TYPE_REFERENCE, inputStreamResponseProcessor)

            .put(AbstractResource.MAP_STRING_TO_OBJECT_TYPE_REFERENCE, contentResponseProcessor)
            .put(AbstractResource.TASK_RESPONSE_TYPE_REFERENCE, contentResponseProcessor)
            .put(AbstractResource.JOB_RESPONSE_TYPE_REFERENCE, contentResponseProcessor)
            .put(AbstractResource.USER_RESPONSE_TYPE_REFERENCE, contentResponseProcessor)
            .put(AbstractResource.WEBHOOKS_RESPONSE_TYPE_REFERENCE, contentResponseProcessor)

            .put(AbstractResource.OPERATION_RESPONSE_PAGEABLE_TYPE_REFERENCE, contentResponseProcessor)
            .put(AbstractResource.TASK_RESPONSE_PAGEABLE_TYPE_REFERENCE, contentResponseProcessor)
            .put(AbstractResource.JOB_RESPONSE_PAGEABLE_TYPE_REFERENCE, contentResponseProcessor)
            .put(AbstractResource.WEBHOOKS_RESPONSE_PAGEABLE_TYPE_REFERENCE, contentResponseProcessor).build();
    }

    public <T> Result<T> extract(
        final HttpResponse httpResponse, final TypeReference<T> typeReference
    ) throws IOException, CloudConvertClientException, CloudConvertServerException {
        final StatusLine statusLine = httpResponse.getStatusLine();

        final Status status = Status.builder().code(statusLine.getStatusCode()).reason(statusLine.getReasonPhrase()).build();
        final Map<String, String> headers = Arrays.stream(httpResponse.getAllHeaders()).collect(Collectors.toMap(Header::getName, Header::getValue, (v1, v2) -> v1 + ", " + v2));
        final HttpEntity httpEntity = Optional.ofNullable(httpResponse.getEntity()).orElse(new InputStreamEntity(EmptyInputStream.INSTANCE));

        try (final InputStream inputStream = httpEntity.getContent()) {
            if (status.isSuccessful()) {
                return responseProcessors.getOrDefault(typeReference, defaultResponseProcessor).process(status, headers, inputStream, typeReference);
            }
            if (status.isClientError()) {
                throw new CloudConvertClientException(status, headers, inputStream);
            }
            if (status.isServerError()) {
                throw new CloudConvertServerException(status, headers, inputStream);
            }

            return defaultResponseProcessor.process(status, headers, inputStream, typeReference);
        }
    }
}
