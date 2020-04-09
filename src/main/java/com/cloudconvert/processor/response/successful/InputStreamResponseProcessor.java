package com.cloudconvert.processor.response.successful;

import com.cloudconvert.dto.result.Result;
import com.cloudconvert.processor.response.ResponseProcessor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.io.ByteSource;
import org.apache.http.Header;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.stream.Collectors;

public class InputStreamResponseProcessor implements ResponseProcessor {

    @Override
    public <T> Result<T> process(
        final int status, final Header[] headers, final InputStream inputStream, final TypeReference<T> typeReference
    ) throws IOException {
        final ByteSource byteSource = new ByteSource() {
            @Override
            public InputStream openStream() throws IOException {
                return inputStream;
            }
        };

        return Result.<T>builder().headers(Arrays.stream(headers).collect(Collectors.toMap(Header::getName, Header::getValue)))
            .message("").body((T) new ByteArrayInputStream(byteSource.read())).status(status).build();
    }
}
