package com.cloudconvert.resource;

import static javax.ws.rs.core.Response.Status;

import lombok.Builder;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@Builder
public class Response<T> {

    @Getter
    private Status status;

    @Getter
    private String error;

    @Nullable
    private T body;

    public Optional<T> getBody() {
        return Optional.ofNullable(body);
    }
}
