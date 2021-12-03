package com.cloudconvert.exception;

import com.cloudconvert.client.mapper.ObjectMapperProvider;
import com.cloudconvert.dto.result.Status;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class CloudConvertServerException extends CloudConvertException {

    public CloudConvertServerException(
        final Status status, final Map<String, String> headers, final InputStream inputStream
    ) throws IOException {
        this(status, headers, inputStream, new ObjectMapperProvider());
    }

    public CloudConvertServerException(
        final Status status, final Map<String, String> headers, final InputStream inputStream, final ObjectMapperProvider objectMapperProvider
    ) throws IOException {
        super(status, headers, inputStream, objectMapperProvider);
    }
}
