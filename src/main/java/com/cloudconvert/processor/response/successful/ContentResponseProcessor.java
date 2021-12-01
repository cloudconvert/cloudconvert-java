package com.cloudconvert.processor.response.successful;

import com.cloudconvert.client.mapper.ObjectMapperProvider;
import com.cloudconvert.dto.result.Result;
import com.cloudconvert.dto.result.Status;
import com.cloudconvert.processor.content.ContentPreProcessor;
import com.cloudconvert.processor.content.DataExtractingContentPreProcessor;
import com.cloudconvert.processor.content.DefaultContentPreProcessor;
import com.cloudconvert.processor.response.ResponseProcessor;
import com.cloudconvert.resource.AbstractResource;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class ContentResponseProcessor implements ResponseProcessor {

    private final ObjectMapperProvider objectMapperProvider;

    private final ContentPreProcessor defaultContentPreProcessor;
    private final Map<TypeReference<?>, ContentPreProcessor> contentPreProcessors;

    public ContentResponseProcessor(final ObjectMapperProvider objectMapperProvider) {
        this.objectMapperProvider = objectMapperProvider;

        final ContentPreProcessor dataExtractionContentPreProcessor = new DataExtractingContentPreProcessor();

        this.defaultContentPreProcessor = new DefaultContentPreProcessor();
        this.contentPreProcessors = ImmutableMap.<TypeReference<?>, ContentPreProcessor>builder()
            .put(AbstractResource.TASK_RESPONSE_TYPE_REFERENCE, dataExtractionContentPreProcessor)
            .put(AbstractResource.JOB_RESPONSE_TYPE_REFERENCE, dataExtractionContentPreProcessor)
            .put(AbstractResource.USER_RESPONSE_TYPE_REFERENCE, dataExtractionContentPreProcessor)
            .put(AbstractResource.WEBHOOKS_RESPONSE_TYPE_REFERENCE, dataExtractionContentPreProcessor).build();
    }

    @Override
    public <T> Result<T> process(
        final Status status, final Map<String, String> headers, final InputStream inputStream, final TypeReference<T> typeReference
    ) throws IOException {
        final String string = contentPreProcessors.getOrDefault(typeReference, defaultContentPreProcessor).preProcess(inputStream);

        return Result.<T>builder().status(status).headers(headers).body(objectMapperProvider.provide().readValue(string, typeReference)).build();
    }
}
