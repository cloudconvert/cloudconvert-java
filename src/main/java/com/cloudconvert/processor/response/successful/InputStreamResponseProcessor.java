package com.cloudconvert.processor.response.successful;

import com.cloudconvert.dto.result.Result;
import com.cloudconvert.dto.result.Status;
import com.cloudconvert.processor.response.ResponseProcessor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.io.ByteSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class InputStreamResponseProcessor implements ResponseProcessor {

    @Override
    public <T> Result<T> process(
        final Status status, final Map<String, String> headers, final InputStream inputStream, final TypeReference<T> typeReference
    ) throws IOException {
        final ByteSource byteSource = new ByteSource() {
            @Override
            public InputStream openStream() throws IOException {
                return inputStream;
            }
        };

        return Result.<T>builder().status(status).headers(headers).body((T) new ByteArrayInputStream(byteSource.read())).build();
    }
}
