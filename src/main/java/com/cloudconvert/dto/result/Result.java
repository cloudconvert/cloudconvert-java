package com.cloudconvert.dto.result;

import lombok.Builder;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@Builder
public class Result<T> extends AbstractResult<T> {

    @Getter
    private int status;

    @Getter
    private Map<String, String> headers;

    @Getter
    private String message;

    @Getter
    @Nullable
    private T body;
}
