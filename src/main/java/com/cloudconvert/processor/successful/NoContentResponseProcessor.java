package com.cloudconvert.processor.successful;

import com.cloudconvert.dto.result.Result;
import com.cloudconvert.processor.ResponseProcessor;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.http.Header;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.stream.Collectors;

public class NoContentResponseProcessor implements ResponseProcessor {

    @Override
    public <T> Result<T> process(
        final int status, final Header[] headers, final InputStream inputStream, final TypeReference<T> typeReference
    ) throws IOException {
        return Result.<T>builder().headers(Arrays.stream(headers).collect(Collectors.toMap(Header::getName, Header::getValue)))
            .message("").status(status).build();
    }
}
