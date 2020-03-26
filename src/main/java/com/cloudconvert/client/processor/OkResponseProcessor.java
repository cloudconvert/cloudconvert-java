package com.cloudconvert.client.processor;

import static javax.ws.rs.core.Response.Status;

import com.cloudconvert.client.mapper.ObjectMapperProvider;
import com.cloudconvert.resource.Response;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.http.client.methods.CloseableHttpResponse;

import java.io.IOException;

public class OkResponseProcessor implements ResponseProcessor {

    private final ObjectMapperProvider objectMapperProvider;

    public OkResponseProcessor(final ObjectMapperProvider objectMapperProvider) {
        this.objectMapperProvider = objectMapperProvider;
    }

    @Override
    public <T> Response<T> process(
        final Status status, final CloseableHttpResponse closeableHttpResponse, final TypeReference<T> typeReference
    ) throws IOException {
        return Response.<T>builder().status(status)
            .error("").body(objectMapperProvider.provide().readValue(closeableHttpResponse.getEntity().getContent(), typeReference)).build();
    }
}
