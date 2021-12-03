package com.cloudconvert.processor.response.successful;

import com.cloudconvert.dto.result.Result;
import com.cloudconvert.dto.result.Status;
import com.cloudconvert.processor.response.ResponseProcessor;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.InputStream;
import java.util.Map;

public class NoContentResponseProcessor implements ResponseProcessor {

    @Override
    public <T> Result<T> process(
        final Status status, final Map<String, String> headers, final InputStream inputStream, final TypeReference<T> typeReference
    ) {
        return Result.<T>builder().status(status).headers(headers).build();
    }
}
