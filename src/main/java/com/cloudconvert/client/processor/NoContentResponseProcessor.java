package com.cloudconvert.client.processor;

import static javax.ws.rs.core.Response.Status;

import com.cloudconvert.resource.Response;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.http.client.methods.CloseableHttpResponse;

import java.io.IOException;

public class NoContentResponseProcessor implements ResponseProcessor {

    @Override
    public <T> Response<T> process(
        final Status status, final CloseableHttpResponse closeableHttpResponse, final TypeReference<T> typeReference
    ) throws IOException {
        return null;
    }
}
