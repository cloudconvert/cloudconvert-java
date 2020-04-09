package com.cloudconvert.processor.response;

import com.cloudconvert.dto.result.Result;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.http.Header;

import java.io.IOException;
import java.io.InputStream;

public interface ResponseProcessor {

    <T> Result<T> process(
        final int status, final Header[] headers, final InputStream inputStream, final TypeReference<T> typeReference
    ) throws IOException;
}
