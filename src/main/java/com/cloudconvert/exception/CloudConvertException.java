package com.cloudconvert.exception;

import com.cloudconvert.client.mapper.ObjectMapperProvider;
import com.cloudconvert.dto.response.ErrorResponse;
import com.cloudconvert.dto.result.Status;
import com.google.common.base.Charsets;
import com.google.common.io.ByteSource;
import lombok.Getter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class CloudConvertException extends Exception {

    @Getter
    private final Status status;

    @Getter
    private final Map<String, String> headers;

    @Getter
    private final ErrorResponse body;

    public CloudConvertException(
        final Status status, final Map<String, String> headers, final InputStream inputStream, final ObjectMapperProvider objectMapperProvider
    ) throws IOException {
        final ByteSource byteSource = new ByteSource() {
            @Override
            public InputStream openStream() throws IOException {
                return inputStream;
            }
        };

        this.status = status;
        this.headers = headers;
        this.body = objectMapperProvider.provide().readValue(byteSource.asCharSource(Charsets.UTF_8).read(), ErrorResponse.class);
    }
}
