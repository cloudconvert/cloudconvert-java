package com.cloudconvert.processor.response;

import com.cloudconvert.dto.result.Result;
import com.cloudconvert.dto.result.Status;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public interface ResponseProcessor {

    <T> Result<T> process(
        final Status status, final Map<String, String> headers, final InputStream inputStream, final TypeReference<T> typeReference
    ) throws IOException;
}
