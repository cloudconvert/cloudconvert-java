package com.cloudconvert.processor.successful;

import com.cloudconvert.client.mapper.ObjectMapperProvider;
import com.cloudconvert.dto.result.Result;
import com.cloudconvert.processor.ResponseProcessor;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.http.Header;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ContentResponseProcessor implements ResponseProcessor {

    private final ObjectMapperProvider objectMapperProvider;

    public ContentResponseProcessor(final ObjectMapperProvider objectMapperProvider) {
        this.objectMapperProvider = objectMapperProvider;
    }

    @Override
    public <T> Result<T> process(
        final int status, final Header[] headers, final InputStream inputStream, final TypeReference<T> typeReference
    ) throws IOException {
        return Result.<T>builder().status(status).headers(Arrays.stream(headers).collect(Collectors.toMap(Header::getName, Header::getValue)))
            .message("").body(objectMapperProvider.provide().readValue(inputStream, typeReference)).build();
    }
}
